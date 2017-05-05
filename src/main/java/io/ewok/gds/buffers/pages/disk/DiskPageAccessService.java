package io.ewok.gds.buffers.pages.disk;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.AbstractService;

import io.ewok.gds.buffers.CorruptPageException;
import io.ewok.gds.buffers.InvalidPageRefException;
import io.ewok.gds.buffers.pages.PageAccessService;
import io.ewok.gds.buffers.pages.PageId;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;

/**
 * Performs background IO to local disk.
 *
 * We cache the open files to avoid too much overhead, and a object mapper is
 * used for mapping object IDs & pages to filesystem files and the offsets.
 *
 * A checksum of the page is performed as they are retrieved and written here.
 *
 * @author theo
 *
 */

public class DiskPageAccessService extends AbstractService implements PageAccessService {

	/**
	 * A validator for page checksumming.
	 */

	private final PageValidationProvider checksummer = new GuavaPageValidationProvider(Hashing.crc32c());

	/**
	 * Options passed to the open() call.
	 */

	private static final Set<? extends OpenOption> OPEN_OPTIONS = Sets.newHashSet(
			StandardOpenOption.READ,
			StandardOpenOption.WRITE,
			StandardOpenOption.CREATE);

	/**
	 * Maps the objid and page numbers to filenames and offsets.
	 */

	private final DiskAccessMapper mapper;

	/**
	 * Thread pool which does the IO work.
	 */

	// thread pool for disk IO. this manages the ioq depth.
	// TODO(tpz): make configurable
	private final ExecutorService service = Executors.newFixedThreadPool(8);

	/**
	 *
	 * @param mapper
	 */

	public DiskPageAccessService(DiskAccessMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 *
	 */

	@Override
	protected void doStart() {
		super.notifyStarted();
	}

	/**
	 *
	 */

	@Override
	protected void doStop() {
		this.service.shutdown();
		try {
			this.service.awaitTermination(1, TimeUnit.MINUTES);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.notifyStopped();
	}

	/**
	 * Keep a cache of file descriptors we have open.
	 */

	private final LoadingCache<Path, AsynchronousFileChannel> channels = CacheBuilder.newBuilder()
			.maximumSize(1024)
			.expireAfterAccess(10, TimeUnit.SECONDS)
			.removalListener((RemovalNotification<Path, AsynchronousFileChannel> notification) -> {
				final AsynchronousFileChannel channel = notification.getValue();
				try {
					channel.close();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			})
			.build(new CacheLoader<Path, AsynchronousFileChannel>() {
				@Override
				public AsynchronousFileChannel load(Path path) throws IOException {
					return AsynchronousFileChannel.open(
							path,
							OPEN_OPTIONS,
							DiskPageAccessService.this.service,
							new FileAttribute[0]);
				}
			});

	/**
	 * Perform a fetch from disk.
	 *
	 * @param buffer
	 *            The buffer that the file content should be placed in.
	 * @param path
	 *            The path to the file that needs to be loaded.
	 * @param offset
	 *            Offset in the file to load.
	 *
	 * @return A future which will complete when the operation has completed.
	 */

	@Override
	@SneakyThrows
	public CompletableFuture<ByteBuf> read(ByteBuf buffer, PageId pageId) {

		// calculate the path for this page.
		final Path path = this.mapper.map(pageId);

		// the result holder
		final CompletableFuture<ByteBuf> future = new CompletableFuture<>();

		// fetch the path, then read.
		this.channels.get(path).read(
				buffer.nioBuffer(buffer.writerIndex(), 8192), this.mapper.offset(pageId), buffer,
				new CompletionHandler<Integer, ByteBuf>() {

					@Override
					public void completed(Integer result, ByteBuf attachment) {

						if (result.intValue() != 8192) {
							this.failed(new InvalidPageRefException(pageId), attachment);
							return;
						}

						try {
							PageUtils.checkIntegrity(buffer, pageId);
							if (!DiskPageAccessService.this.checksummer.validate(buffer, pageId)) {
								this.failed(new IllegalStateException("Invalid Checksum"), attachment);
								return;
							}
						} catch (final Exception ex) {
							this.failed(new CorruptPageException(pageId), attachment);
							return;
						}

						future.complete(buffer);

					}

					@Override
					public void failed(Throwable exc, ByteBuf attachment) {
						future.completeExceptionally(exc);
					}

				});

		return future;

	}

	/**
	 * Perform a write from disk.
	 *
	 * @param buffer
	 *            The buffer that the file content should be placed in.
	 * @param path
	 *            The path to the file that needs to be loaded.
	 * @param offset
	 *            Offset in the file to load.
	 *
	 * @return A future which will complete when the operation has completed.
	 */

	@Override
	@SneakyThrows
	public CompletableFuture<?> write(ByteBuf buffer, PageId pageId) {

		PageUtils.checkIntegrity(buffer, pageId);

		// update the checksum.
		this.checksummer.update(buffer, pageId);

		// calculate the path for this page.
		final Path path = this.mapper.map(pageId);

		// the result holder
		final CompletableFuture<ByteBuf> future = new CompletableFuture<>();

		final AsynchronousFileChannel channel = this.channels.get(path);

		// fetch the path, then write.
		channel.write(
				buffer.nioBuffer(buffer.readerIndex(), 8192), this.mapper.offset(pageId), buffer,
				new CompletionHandler<Integer, ByteBuf>() {

					@Override
					public void completed(Integer result, ByteBuf attachment) {
						future.complete(buffer);
					}

					@Override
					public void failed(Throwable exc, ByteBuf attachment) {
						future.completeExceptionally(exc);
					}
				});

		return future;

	}

	public static DiskPageAccessService create(Path base) {
		final DiskPageAccessService i = new DiskPageAccessService(new DefaultDiskAccessMapper(base));
		i.startAsync();
		return i;
	}

}

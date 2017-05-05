package io.ewok.gds.storage;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.Sets;

import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;

public class AsyncFileIO {

	private static final ExecutorService io = Executors.newFixedThreadPool(8);

	private final Path path;

	private AsynchronousFileChannel channel;

	public AsyncFileIO(Path path) {
		this.path = path;
	}

	/**
	 * Opens the file.
	 *
	 * @param path
	 * @return
	 */

	@SneakyThrows
	public CompletableFuture<AsyncFileIO> open() {
		this.channel = AsynchronousFileChannel.open(
				this.path,
				Sets.newHashSet(StandardOpenOption.READ, StandardOpenOption.WRITE),
				io,
				new FileAttribute[0]);
		return CompletableFuture.completedFuture(this);
	}

	@SneakyThrows
	public CompletableFuture<AsyncFileIO> create(int prealloc) {

		this.channel = AsynchronousFileChannel.open(
				this.path,
				Sets.newHashSet(StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE),
				io,
				new FileAttribute[0]);

		return CompletableFuture.completedFuture(this);

	}

	public CompletableFuture<?> close() {
		try {
			this.channel.close();
			this.channel = null;
		} catch (final IOException e) {
			final CompletableFuture<?> fut = new CompletableFuture<>();
			fut.completeExceptionally(e);
			return fut;
		}
		return CompletableFuture.completedFuture(null);
	}

	public static CompletableFuture<?> unlink(Path file) {
		try {
			Files.delete(file);
		} catch (final IOException e) {
			final CompletableFuture<?> fut = new CompletableFuture<>();
			fut.completeExceptionally(e);
			return fut;
		}
		return CompletableFuture.completedFuture(null);
	}

	public static CompletableFuture<Long> size(Path path) {
		try {
			return CompletableFuture.completedFuture(Files.size(path));
		} catch (final IOException e) {
			final CompletableFuture<Long> fut = new CompletableFuture<>();
			fut.completeExceptionally(e);
			return fut;
		}
	}

	public CompletableFuture<Integer> read(ByteBuf buffer, long offset, int length) {

		// the result holder
		final CompletableFuture<Integer> future = new CompletableFuture<>();

		// fetch the path, then read.
		this.channel.read(
				buffer.nioBuffer(buffer.writerIndex(), length),
				offset,
				buffer,
				new CompletionHandler<Integer, ByteBuf>() {

					@Override
					public void completed(Integer result, ByteBuf attachment) {
						future.complete(result);
					}

					@Override
					public void failed(Throwable exc, ByteBuf attachment) {
						future.completeExceptionally(exc);
					}

				});

		return future;

	}

	/**
	 *
	 * @param buffer
	 * @param offset
	 * @param length
	 * @return
	 */

	public CompletableFuture<Integer> write(ByteBuf buffer, long offset, int length) {

		// the result holder
		final CompletableFuture<Integer> future = new CompletableFuture<>();

		// fetch the path, then read.
		this.channel.write(
				buffer.nioBuffer(buffer.writerIndex(), length),
				offset,
				buffer,
				new CompletionHandler<Integer, ByteBuf>() {

					@Override
					public void completed(Integer result, ByteBuf attachment) {
						future.complete(result);
					}

					@Override
					public void failed(Throwable exc, ByteBuf attachment) {
						future.completeExceptionally(exc);
					}

				});

		return future;

	}

	/**
	 *
	 */

}

package io.ewok.gds.storage;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
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

	public CompletableFuture<AsyncFileIO> open(Path path) {
		return CompletableFuture.completedFuture(this);
	}

	@SneakyThrows
	public CompletableFuture<AsyncFileIO> create() {

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

	public CompletableFuture<ByteBuf> read(ByteBuf buffer, long offset, int length) {
		return CompletableFuture.completedFuture(buffer);
	}

	public CompletableFuture<ByteBuf> write(ByteBuf buffer, long offset, int length) {
		return CompletableFuture.completedFuture(buffer);
	}

	/**
	 *
	 */

}

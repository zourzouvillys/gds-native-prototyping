package io.ewok.gds.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import io.ewok.gds.buffers.pages.PageFork;
import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * A relation that is disk backed.
 *
 * @author theo
 *
 */

public final class DiskStorageRelation implements StorageRelation {

	private final long objid;
	private final Path folder;
	private final long pagesPerFile = 250_000;

	// each of the forks for this relation
	private final Fork[] forks = new Fork[PageFork.values().length];

	/**
	 * a fork can have multiple segments.
	 */

	private class Fork {

		private final AsyncFileIO fd[] = new AsyncFileIO[1];
		private final PageFork fork;

		Fork(PageFork fork) {
			this.fork = fork;
		}

		public CompletableFuture<?> create() {
			this.fd[0] = new AsyncFileIO(DiskStorageRelation.this.path(this.fork, 0));
			return this.fd[0].create();
		}

		private AsyncFileIO fd(long pageno) {
			return this.fd[0];
		}

		private int offset(long pageno) {
			return (int) (pageno * 8192);
		}

		public CompletableFuture<ByteBuf> read(ByteBuf buffer, long pageno) {
			return this.fd(pageno).read(buffer, this.offset(pageno), 8192);
		}

		public CompletableFuture<Boolean> exists() {
			return CompletableFuture.completedFuture(Files.exists(DiskStorageRelation.this.path(this.fork, 0)));
		}

		public CompletableFuture<?> extend(long pageno, ByteBuf page) {
			return CompletableFuture.completedFuture(DiskStorageRelation.this);
		}

		public CompletableFuture<ByteBuf> write(ByteBuf buffer, long pageno, boolean fsync) {
			return this.fd(pageno).write(buffer, this.offset(pageno), 8192);

		}

		public CompletableFuture<?> writeback(long pageno, int numpages) {
			// TODO: NO-OP for now.
			return CompletableFuture.completedFuture(null);
		}

		public CompletableFuture<Integer> nblocks() {
			return AsyncFileIO.size(DiskStorageRelation.this.path(this.fork, 0)).thenApply(val -> (int) (val / 8192));
		}

		public CompletableFuture<?> unlink() {
			return this.fd[0].close().thenApply(x -> AsyncFileIO.unlink(DiskStorageRelation.this.path(this.fork, 0)));
		}

		/**
		 *
		 * @param pageno
		 */

		public void prefetch(long pageno) {
			// TODO NO-OP for now.
		}

		/**
		 *
		 * @return
		 */

		public CompletableFuture<?> sync() {
			return CompletableFuture.completedFuture(this);
		}

		/**
		 * truncate to the given size.
		 *
		 * @param npages
		 * @return
		 */

		public CompletableFuture<?> truncate(long npages) {
			return CompletableFuture.completedFuture(null);
		}

		// close any open file descriptors.

		public CompletableFuture<?> close() {
			if (this.fd[0] != null) {
				return this.fd[0].close();
			}
			return CompletableFuture.completedFuture(null);
		}

	}

	/**
	 *
	 */

	public DiskStorageRelation(Path folder, long objid) {
		this.folder = folder;
		this.objid = objid;
	}

	/**
	 *
	 * @param fork
	 * @return
	 */

	private String ext(@NonNull PageFork fork) {
		switch (fork) {
			case FreeSpace:
				return "fs";
			case Main:
				return "db";
			case VisibilityMap:
				return "vm";
			default:
				throw new IllegalArgumentException(fork.toString());
		}
	}

	/**
	 * calculate the segment number from the page number.
	 *
	 * @param pageno
	 * @return
	 */

	private int segment(long pageno) {
		return (int) Math.floor(pageno / this.pagesPerFile);
	}

	/**
	 * provides the file path for a specific fork and page number.
	 */

	private Path path(PageFork fork, long pageno) {
		return this.folder.resolve(String.format("%08X.%X.%s", this.objid, this.segment(pageno), this.ext(fork)));
	}

	/**
	 * fetch the given fork, creating if needed.
	 *
	 * @param fork
	 * @return
	 */

	private Fork fork(PageFork fork) {
		final Fork f = this.forks[fork.ordinal()];
		if (f == null) {
			this.forks[fork.ordinal()] = new Fork(fork);
		}
		return this.forks[fork.ordinal()];
	}

	/**
	 * Create the given fork for this relation.
	 *
	 * This results in a zero byte segment 0.
	 *
	 */

	@SneakyThrows
	@Override
	public CompletableFuture<?> create(PageFork fork) {
		return this.fork(fork).create();
	}

	/**
	 * tests if the given fork exists.
	 */

	@Override
	public CompletableFuture<Boolean> exists(PageFork fork) {
		return this.fork(fork).exists();

	}

	/**
	 * extend the fork to contain the given page number.
	 *
	 * if the page already exists, then this fails.
	 *
	 */

	@Override
	public CompletableFuture<?> extend(PageFork fork, long pageno, ByteBuf page) {
		return this.fork(fork).extend(pageno, page);
	}

	/**
	 * read a page from the given fork.
	 */

	@Override
	public CompletableFuture<ByteBuf> read(PageFork fork, long pageno, ByteBuf buffer) {
		return this.fork(fork).read(buffer, pageno);
	}

	/**
	 * write a page to the given fork, optionally performing a sync.
	 */

	@Override
	public CompletableFuture<ByteBuf> write(PageFork fork, long pageno, ByteBuf buffer, boolean fsync) {
		return this.fork(fork).write(buffer, pageno, fsync);
	}

	/**
	 * advise any dirty pages in the segment to be written back to disk.
	 */

	@Override
	public CompletableFuture<?> writeback(PageFork fork, long pageno, int numpages) {
		return this.fork(fork).writeback(pageno, numpages);
	}

	/**
	 * the number of blocks in the fork.
	 */

	@Override
	public CompletableFuture<Integer> nblocks(PageFork fork) {
		return this.fork(fork).nblocks();
	}

	/**
	 * truncate down to the given number of pages.
	 */

	@Override
	public CompletableFuture<?> truncate(PageFork fork, long npages) {
		return this.fork(fork).truncate(npages);
	}

	/**
	 * sync all segments of the fork.
	 */

	@Override
	public CompletableFuture<?> sync(PageFork fork) {
		return this.fork(fork).sync();
	}

	/**
	 * unlink the given fork.
	 */

	@Override
	public CompletableFuture<?> unlink(PageFork fork) {
		return this.fork(fork).unlink();
	}

	/**
	 * close all of the open forks.
	 */

	@Override
	public CompletableFuture<?> close() {
		return CompletableFuture.allOf(Arrays
				.stream(this.forks)
				.filter(fork -> fork != null)
				.map(fork -> fork.close())
				.toArray(CompletableFuture[]::new));
	}

	@Override
	public void prefetch(PageFork fork, long pageno) {
		this.fork(fork).prefetch(pageno);
	}

}

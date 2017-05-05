package io.ewok.gds.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import io.ewok.gds.buffers.InvalidPageRefException;
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

	// 1 GB.
	private static final int MAX_SEGMENT_BYES = (1024 * 1000 * 1000);

	// max page count in a fork
	private static final int MAX_PAGES_IN_FORK = (2 ^ 31 - 1);

	private final long objid;
	private final Path folder;

	// each of the forks for this relation. lazy initiated.
	private final Fork[] forks = new Fork[PageFork.values().length];

	private final int pageSize = 8192;

	/**
	 * a fork can have multiple segments.
	 */

	private class Fork implements StorageRelationFork {

		private AsyncFileIO fds[];
		private final PageFork fork;
		private int segments;

		Fork(PageFork fork) {
			this.fork = fork;
			this.segments = this.segments();
			this.fds = new AsyncFileIO[this.segments];
		}

		/**
		 * calculate how many segments we have by looking at the filesystem.
		 */

		public int segments() {
			for (int i = 0; i < this.maxSegments() + 1; ++i) {
				if (!Files.exists(this.path(i))) {
					return i;
				}
			}
			return 0;
		}

		/**
		 * The maximum number of segments that this fork may have.
		 */

		private int maxSegments() {
			return (MAX_PAGES_IN_FORK / DiskStorageRelation.this.pagesPerSegment());
		}

		/**
		 * The page size for this fork.
		 */

		private int pagesize() {
			return DiskStorageRelation.this.pageSize;
		}

		/**
		 * fetch the FD for the given segment, opening if needed.
		 */

		@SneakyThrows
		private AsyncFileIO openfd(int pageno) {
			final int sgid = this.segment(pageno);
			if (this.fds[sgid] == null) {
				this.fds[sgid] = new AsyncFileIO(this.path(pageno));
				this.fds[sgid].open().get();
			}
			return this.fds[sgid];
		}

		/**
		 * The offset within the segment for the given page number.
		 */

		private long offset(long pageno) {
			return (pageno * 8192);
		}

		/**
		 *
		 */

		private int segment(long pageno) {
			return 0;
		}

		/**
		 * file path for the specified page number.
		 */

		private Path path(long pageno) {
			return DiskStorageRelation.this.path(this.fork, 0);
		}

		/**
		 * create the first segment for this fork.
		 */

		@Override
		public CompletableFuture<?> create(int numpagealloc) {
			Preconditions.checkState(this.segments == 0, this.segments);
			this.fds = new AsyncFileIO[1];
			this.fds[0] = new AsyncFileIO(this.path(0));
			this.segments = 1;
			return this.fds[0].create(numpagealloc * this.pagesize());
		}

		/**
		 * tests if this fork has a segment zero.
		 */

		@Override
		public CompletableFuture<Boolean> exists() {
			return CompletableFuture.completedFuture(Files.exists(this.path(0)));
		}

		@Override
		public CompletableFuture<ByteBuf> read(int pageno, ByteBuf buffer) {
			return this.openfd(pageno).read(buffer, this.offset(pageno), this.pagesize()).thenApply((Integer len) -> {
				if (len != this.pagesize()) {
					throw new InvalidPageRefException(null);
				}
				return buffer;
			});
		}

		/**
		 * A write can only be for an existing page within the bounds of the
		 * object. To write a new page, use {@link #extend(int, ByteBuf)}.
		 */

		@Override
		public CompletableFuture<ByteBuf> write(int pageno, ByteBuf buffer, boolean fsync) {
			return this.openfd(pageno).write(buffer, this.offset(pageno), this.pagesize()).thenApply((Integer len) -> {
				if (len != this.pagesize()) {
					throw new InvalidPageRefException(null);
				}
				return buffer;
			});
		}

		@Override
		public CompletableFuture<Integer> pagecount() {
			return AsyncFileIO.size(this.path(0)).thenApply(val -> (int) (val / this.pagesize()));
		}

		/**
		 * Extend the object by writing this page. May create a new segment in
		 * the process.
		 */

		@Override
		public CompletableFuture<?> extend(int pageno, int numpages, ByteBuf page) {
			return CompletableFuture.completedFuture(this);
		}

		@Override
		public CompletableFuture<?> unlink() {
			Arrays.stream(this.fds).filter(fd -> fd != null).forEach(fd -> fd.close());
			while (this.segments > 0) {
				AsyncFileIO.unlink(this.path(--this.segments));
			}
			this.fds = new AsyncFileIO[0];
			return CompletableFuture.completedFuture(null);
		}

		/**
		 * Advisory.
		 */

		@Override
		public CompletableFuture<?> writeback(int pageno, int numpages) {
			// TODO: NO-OP for now.
			return CompletableFuture.completedFuture(null);
		}

		/**
		 *
		 * @param pageno
		 */

		@Override
		public void prefetch(int pageno) {
			// TODO NO-OP for now.
		}

		/**
		 *
		 * @return
		 */

		@Override
		public CompletableFuture<?> sync() {
			return CompletableFuture.completedFuture(this);
		}

		/**
		 * truncate to the given size.
		 *
		 * @param npages
		 * @return
		 */

		@Override
		public CompletableFuture<?> truncate(int npages) {
			return CompletableFuture.completedFuture(null);
		}

		// close any open file descriptors.

		@Override
		public CompletableFuture<?> close() {
			final AsyncFileIO[] fds = this.fds;
			this.fds = new AsyncFileIO[this.segments];
			return CompletableFuture.allOf(Arrays.stream(fds)
					.filter(fd -> fd != null)
					.map(fd -> fd.close())
					.toArray(CompletableFuture[]::new));
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
	 * Calculates the extension for the fork type.
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
	 */

	private int segment(long pageno) {
		return (int) Math.floor(pageno / this.pagesPerSegment());
	}

	/**
	 * How many pages are in each segment.
	 */

	private int pagesPerSegment() {
		return (MAX_SEGMENT_BYES / this.pageSize);
	}

	/**
	 * provides the file path for a specific fork and page number.
	 */

	private Path path(PageFork fork, long pageno) {
		return this.folder.resolve(String.format("%08X.%X.%s", this.objid, this.segment(pageno), this.ext(fork)));
	}

	/**
	 * fetch the given fork, allocating object if needed.
	 */

	@Override
	public StorageRelationFork fork(PageFork fork) {
		final Fork f = this.forks[fork.ordinal()];
		if (f == null) {
			this.forks[fork.ordinal()] = new Fork(fork);
		}
		return this.forks[fork.ordinal()];
	}

	/**
	 * sync all segments which are open.
	 */

	@Override
	public CompletableFuture<?> sync() {
		return CompletableFuture
				.allOf(this.forks().stream().map(fork -> fork.sync()).toArray(CompletableFuture[]::new));
	}

	/**
	 * unlink the given fork.
	 */

	@Override
	public CompletableFuture<?> unlink() {
		return CompletableFuture
				.allOf(this.forks().stream()
						.map(fork -> fork.exists().thenApplyAsync(
								exists -> exists ? fork.unlink() : CompletableFuture.completedFuture(null)))
						.toArray(CompletableFuture[]::new));
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

	private Set<Fork> forks() {
		return Arrays.stream(this.forks).filter(fork -> fork != null).collect(Collectors.toSet());
	}

}

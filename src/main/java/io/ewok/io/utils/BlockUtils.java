package io.ewok.io.utils;

import com.google.common.base.Preconditions;

import io.ewok.io.BlockAccessCallback;
import io.ewok.io.BlockAccessResult;

public final class BlockUtils {

	public static final BlockAccessCallback<Runnable> BLOCK_ACCESS_RUNNABLE_STUB = new BlockAccessCallback<Runnable>() {
		@Override
		public void complete(BlockAccessResult result, Runnable run) {
			run.run();
		}
	};
	public static final BlockAccessCallback<Void> BLOCK_ACCESS_EMPTY_STUB = new BlockAccessCallback<Void>() {
		@Override
		public void complete(BlockAccessResult result, Void unused) {
		}
	};

	/**
	 * returns the page that the given offset would fall on.
	 *
	 * @param offset
	 * @param pageSize
	 * @return
	 */

	public static final int pageNumberForOffset(long offset, long pageSize) {
		return (int) (offset - (offset % pageSize));
	}

	/**
	 * calculates how many pages are needed for the given size, throwing if not
	 * on a boundary.
	 *
	 *
	 *
	 * @param pageSize
	 * @param size
	 * @param unit
	 * @return
	 */

	public static int pagesForSize(long pageSize, long size, BlockSizeUnits unit) {
		final long bytes = unit.bytes(size);
		Preconditions.checkArgument((bytes % pageSize) == 0, "not aligned");
		return (int) (bytes / pageSize);
	}

	/**
	 * given an offset and page size, returns the position in a page that would
	 * be used.
	 *
	 * @param offset
	 * @param pageSize
	 * @return
	 */

	public static int offsetInPage(long offset, long pageSize) {
		return (int) (offset % pageSize);
	}

	/**
	 * given a file offset, returns the offset for the start of the page this
	 * offset is in.
	 *
	 * @param offset
	 * @param pageSize
	 * @return
	 */

	public static long offsetOfPageFor(long offset, long pageSize) {
		return pageNumberForOffset(offset, pageSize) * pageSize;
	}

}

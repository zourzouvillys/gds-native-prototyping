package io.ewok.linux.io;

import com.google.common.base.Preconditions;

import io.ewok.io.PageBuffer;
import io.ewok.io.PageBufferPool;
import io.ewok.io.utils.BlockSizeUnits;
import io.ewok.io.utils.BlockUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;

/**
 * A pool of {@link ByteBuf} instances which are always aligned to a specific
 * boundary.
 *
 * Fetch buffers using {@link #allocate(int)}, and release them once done.
 *
 * @author theo
 *
 */

public class AlignedByteBufPool implements PageBufferPool {

	private final int pageSize;

	@Getter
	private final int numpages;
	private final ByteBuf pool;
	private int slot;

	public AlignedByteBufPool(long pageSize, int numpages) {
		this(pageSize, numpages, pageSize);
	}

	public AlignedByteBufPool(long pageSize, int numpages, long align) {

		Preconditions.checkArgument(pageSize > 0, pageSize);
		Preconditions.checkArgument(numpages > 0, numpages);
		Preconditions.checkArgument(align > 0, align);

		if (Long.bitCount(align) != 1) {
			throw new IllegalArgumentException("Alignment must be a power of 2");
		}

		this.pageSize = (int) pageSize;
		this.numpages = numpages;

		final int capacity = (this.pageSize * numpages);
		final int alloc = capacity + (int) align;
		final ByteBuf buffy = ByteBufAllocator.DEFAULT.directBuffer(alloc, alloc);

		final long address = buffy.memoryAddress();

		if ((address & (pageSize - 1)) == 0) {
			buffy.capacity(capacity);
			this.pool = buffy.slice(0, capacity);
		} else {
			// need to align to the correct position
			final int newPosition = (int) (pageSize - (address & (align - 1)));
			this.pool = buffy.slice(newPosition, capacity);
		}

	}

	/**
	 * Allocate a buffer.
	 *
	 * @param numPages
	 * @return
	 */

	public ByteBuf allocate(int numPages) {
		final int alloc = (numPages * this.pageSize);
		final ByteBuf res = this.pool.slice((this.pageSize * this.slot), alloc);
		this.slot += numPages;
		return res;
	}

	@Override
	public PageBuffer allocate() {
		final ByteBuf res = this.pool.slice((this.pageSize * this.slot), this.pageSize);
		this.slot += 1;
		return new ByteBufPageBuffer(res);
	}

	public void reset() {
		this.slot = 0;
	}

	@Override
	public int available() {
		return this.numpages - this.slot;
	}

	/**
	 * Create a new pool of pages, aligned to the page size.
	 *
	 * @param pageSize
	 * @param size
	 * @param unit
	 * @return
	 */

	public static PageBufferPool createAligned(long pageSize, long size, BlockSizeUnits unit) {
		return new AlignedByteBufPool(pageSize, BlockUtils.pagesForSize(pageSize, size, unit));
	}

}

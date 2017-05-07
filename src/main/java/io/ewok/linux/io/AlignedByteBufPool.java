package io.ewok.linux.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class AlignedByteBufPool {

	private final int pageSize;
	private final int numpages;
	private final ByteBuf pool;
	private int slot;

	public AlignedByteBufPool(int pageSize, int numpages, int align) {

		if (Long.bitCount(align) != 1) {
			throw new IllegalArgumentException("Alignment must be a power of 2");
		}

		this.pageSize = pageSize;
		this.numpages = numpages;

		final int capacity = (pageSize * numpages);
		final int alloc = capacity + align;
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

		System.err.println(this.pool);

	}

	public ByteBuf allocate(int numPages) {
		final int alloc = (numPages * this.pageSize);
		final ByteBuf res = this.pool.slice((this.pageSize * this.slot), alloc);
		this.slot += numPages;
		return res;
	}

}

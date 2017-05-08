package io.ewok.io;

public interface PagePointer {

	/**
	 *
	 */

	long memoryAddress();

	/**
	 * number of bytes in this page pointer.
	 */

	long pageSize();

	/**
	 * Transfers part of this page to another page.
	 *
	 * @param dest
	 *            The page to copy the data into.
	 * @param dest_offset
	 *            The offset in the target to copy the bytes to.
	 * @param source_offset
	 *            The offset in the source to copy the bytes from.
	 * @param length
	 *            The number of bytes to copy.
	 */

	void transferTo(PageBuffer dest, int dest_offset, int source_offset, int length);

}

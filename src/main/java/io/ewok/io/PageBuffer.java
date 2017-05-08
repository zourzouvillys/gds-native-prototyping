package io.ewok.io;

/**
 * Interface to a chunk of memory.
 *
 * @author theo
 *
 */

public interface PageBuffer extends PagePointer {

	/**
	 * When the buffer is no longer needed, it should be returned to the pool.
	 */

	void release();

	/**
	 *
	 * @param i
	 * @param b
	 */

	void setByte(long offset, byte value);

	/**
	 * zero out the memory at the given offset for number of bytes.
	 */

	void zero(long offset, long length);

}

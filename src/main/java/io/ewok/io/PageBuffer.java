package io.ewok.io;

/**
 * Interface to a chunk of memory.
 *
 * @author theo
 *
 */

public interface PageBuffer {

	/**
	 * When the buffer is no longer needed, it should be returned to the pool.
	 */

	void release();

}

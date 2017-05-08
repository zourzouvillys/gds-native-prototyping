package io.ewok.io;

public interface PageBufferAllocator {

	/**
	 * allocate a page buffer.
	 *
	 * it must be returned using {@link PageBuffer#release()} once it is no
	 * longer needed.
	 *
	 */

	PageBuffer allocate();

}

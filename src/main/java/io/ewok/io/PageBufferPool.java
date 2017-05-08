package io.ewok.io;

public interface PageBufferPool extends PageBufferAllocator {

	/**
	 * how many pages are currently available.
	 */

	int available();

}

package io.ewok.io;

public interface BlockAccessResult {

	/**
	 * The page that was written.
	 */

	PagePointer page();

	/**
	 * The length of the result.
	 */

	long length();

	/**
	 * If an error occurred.
	 */

	Throwable exception();

}

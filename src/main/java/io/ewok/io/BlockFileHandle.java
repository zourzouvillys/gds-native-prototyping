package io.ewok.io;

public interface BlockFileHandle extends AutoCloseable {

	/**
	 * The page size for this file.
	 */

	long pageSize();

	@Override
	void close();

}

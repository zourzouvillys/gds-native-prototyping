package io.ewok.io;

import java.nio.file.Path;

public interface BlockFileHandle extends AutoCloseable {

	/**
	 * the current length of this file.
	 *
	 * this uses stat().size.
	 *
	 */

	long size();

	/**
	 * The page size for this file.
	 */

	long pageSize();

	/**
	 *
	 * @param file
	 */

	void linkat(Path file);

	@Override
	void close();


}

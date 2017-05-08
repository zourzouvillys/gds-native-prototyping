package io.ewok.io;

import io.ewok.linux.io.AsyncResult;
import io.netty.buffer.ByteBuf;

/**
 * API for accessing pages of a file (more or less) asynchronously.
 *
 * @author theo
 *
 */

public interface BlockAccessService extends AutoCloseable {

	/**
	 * Read a buffer from a block file.
	 *
	 * @param fd
	 * @param buf
	 * @param offset
	 * @param length
	 * @param attachment
	 */

	<T> void read(BlockFileHandle fd, ByteBuf buf, long offset, long length, T attachment);

	/**
	 * Write a buffer to a block file.
	 *
	 * @param fd
	 * @param buf
	 * @param offset
	 * @param length
	 * @param attachment
	 */

	<T> void write(BlockFileHandle fd, ByteBuf buf, long offset, long length, T attachment);

	/**
	 * Flush any IO pending commands.
	 *
	 * This must be called after enqueing a batch of IO commands to submit to
	 * submit them if they have not already been submitted.
	 *
	 */

	void flush();

	/**
	 * Poll for events.
	 *
	 * @param results
	 *
	 * @return
	 */

	int events(AsyncResult[] results);

}

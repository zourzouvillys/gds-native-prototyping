package io.ewok.io;

import io.ewok.io.utils.BlockUtils;
import io.ewok.linux.io.AsyncBlockResult;

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

	<T> void read(ReadBlockFileHandle fd, PagePointer buf, long offset, long length, BlockAccessCallback<T> callback,
			T attachment);

	/**
	 *
	 * @param fd
	 * @param buf
	 * @param offset
	 * @param length
	 * @param callback
	 * @param attachment
	 */

	default void read(ReadBlockFileHandle fd, PagePointer buf, long offset, long length, Runnable callback) {
		read(fd, buf, offset, length, BlockUtils.BLOCK_ACCESS_RUNNABLE_STUB, callback);
	}

	default void read(ReadBlockFileHandle fd, PagePointer buf, long offset, long length) {
		read(fd, buf, offset, length, BlockUtils.BLOCK_ACCESS_EMPTY_STUB, null);
	}

	/**
	 * Write a buffer to a block file.
	 *
	 * @param fd
	 * @param buf
	 * @param offset
	 * @param length
	 * @param attachment
	 */

	<T> void write(WriteBlockFileHandle fd, PagePointer buf, long offset, long length, BlockAccessCallback<T> callback,
			T attachment);

	/**
	 *
	 * @param fd
	 * @param buf
	 * @param offset
	 * @param length
	 * @param callback
	 * @param attachment
	 */

	default void write(WriteBlockFileHandle fd, PagePointer buf, long offset, long length, Runnable callback) {
		write(fd, buf, offset, length, BlockUtils.BLOCK_ACCESS_RUNNABLE_STUB, callback);
	}

	default void write(WriteBlockFileHandle fd, PagePointer buf, long offset, long length) {
		write(fd, buf, offset, length, BlockUtils.BLOCK_ACCESS_EMPTY_STUB, null);
	}

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

	int events(AsyncBlockResult[] results);

}

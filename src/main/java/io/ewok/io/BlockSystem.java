package io.ewok.io;

import java.nio.file.Path;

import io.ewok.linux.LinuxStat;
import io.ewok.linux.io.LinuxBlockFileHandle;

/**
 * API for interacting with a filesystem in an interactive manner.
 *
 * @author theo
 *
 */

public interface BlockSystem {

	/**
	 * safely create a new file and open it for using with async io, using
	 * O_DIRECT.
	 *
	 * this can block on io because there is no async mechanism for opening a
	 * file, we do this in a thread from the caching thread pool.
	 *
	 * if this file already exists, the call with fail, and no trace will be
	 * left of the file.
	 *
	 * @param prealloc_bytes
	 *            The number of bytes to preallocate space for. Ideally, this
	 *            should be set to the final size of the file, if it is known in
	 *            advance. Appending small chunks of data at a time to enlarge a
	 *            file will have significant performance penalties when writing.
	 *
	 */

	ReadWriteBlockFileHandle createFile(Path file, long prealloc_bytes);

	/**
	 * Create a new file without preallocting any space, failing if the file
	 * already exists.
	 *
	 * @param file
	 *            The new file to create.
	 * @return
	 */

	default BlockFileHandle createFile(Path file) {
		return createFile(file, 0);
	}

	/**
	 * Create a temporary file on the filesystem without a name in the provided
	 * directory.
	 *
	 * The returned file handle will not have a filename, and will be deleted
	 * when the process exits. Any attempt to use
	 * {@link LinuxBlockFileHandle#linkat(Path)} will fail.
	 *
	 * @param prealloc_bytes
	 *            How many bytes should be pre allocated for this file, if any.
	 *
	 * @return
	 */

	public ReadWriteBlockFileHandle createTempFile(Path dir, long prealloc_bytes);

	/**
	 * Create a temporary file without any preallocated space.
	 */

	public default ReadWriteBlockFileHandle createTempFile(Path dir) {
		return createTempFile(dir, 0);
	}

	/**
	 * open an existing file for reading using async io, using O_DIRECT.
	 *
	 * can block on io: because there is no async mechanism for opening a file,
	 * we do this in a thread from the caching thread pool.
	 *
	 * The priority indicates how urgent the open operation itself is.
	 *
	 * note that a single file can be opened multiple times.
	 *
	 */

	public ReadWriteBlockFileHandle openExisting(Path file);

	/**
	 *
	 */

	public ReadBlockFileHandle openExistingForRead(Path file);

	/**
	 *
	 */

	public WriteBlockFileHandle openExistingForWrite(Path file);

	/**
	 * Unlink this file from the filesystem asynchronously.
	 */

	public void unlink(Path file);

	/**
	 * Fetches statistics about the given path asynchronously.
	 *
	 * @param file
	 * @return
	 */

	public LinuxStat stat(Path file);

}

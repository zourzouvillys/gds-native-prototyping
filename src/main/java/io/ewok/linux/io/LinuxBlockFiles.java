package io.ewok.linux.io;

import java.nio.file.Path;
import java.util.Objects;

import com.google.common.base.Preconditions;

import io.ewok.io.BlockFileMode;
import io.ewok.io.BlockSystem;
import io.ewok.linux.JLinux;
import io.ewok.linux.Stat;

/**
 * Wrapper around linux filesystem operation.
 *
 * Most of the filesystem operations have the potential block.
 *
 * @author theo
 *
 */

public final class LinuxBlockFiles implements BlockSystem {

	/**
	 * safely create a new file and open it for using with async io, using
	 * O_DIRECT.
	 *
	 * this can block on io: because there is no async mechanism for opening a
	 * file, we do this in a thread from the caching thread pool.
	 *
	 * The priority indicates how urgent the open operation itself is.
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

	@Override
	public LinuxBlockFileHandle createFile(Path file, long prealloc_bytes) {

		Objects.requireNonNull(file);
		Objects.requireNonNull(file.getParent());

		Preconditions.checkArgument(prealloc_bytes >= 0, prealloc_bytes);

		// create as a temp file in the same folder, will linkat shortly to the
		// right place. open using writeonly, as we don't need to read from it.
		final int fd = JLinux.open(file.getParent(),
				JLinux.O_TMPFILE | JLinux.O_DIRECT | JLinux.O_DSYNC | JLinux.O_WRONLY,
				JLinux.S_IWUSR | JLinux.S_IRUSR);
		;

		try (final LinuxBlockFileHandle handle = new LinuxBlockFileHandle(fd)) {

			if (prealloc_bytes > 0) {
				// preallocate the space, if requested.
				handle.preallocate(prealloc_bytes);
			}

			// move to the actual destination. fails if already a file there.
			handle.linkat(file);

			// now open the file at its new path using standard openExisting.
			return this.openExisting(file, BlockFileMode.ReadWrite);

		}

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

	@Override
	public LinuxBlockFileHandle createTempFile(Path dir, long prealloc_bytes) {

		Preconditions.checkNotNull(dir, "dir");
		Preconditions.checkArgument(prealloc_bytes >= 0, prealloc_bytes);

		// create as a temp file in the same folder, will linkat shortly to the
		// right place. open using writeonly, as we don't need to read from it.
		final int fd = JLinux.open(dir,
				JLinux.O_TMPFILE | JLinux.O_EXCL | JLinux.O_DIRECT | JLinux.O_DSYNC | JLinux.O_RDWR,
				JLinux.S_IWUSR | JLinux.S_IRUSR);
		;

		final LinuxBlockFileHandle handle = new LinuxBlockFileHandle(fd);

		if (prealloc_bytes > 0) {
			// preallocate the space, if requested.
			handle.preallocate(prealloc_bytes);
		}

		return handle;

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

	@Override
	public LinuxBlockFileHandle openExisting(Path file, BlockFileMode mode) {

		int flags = JLinux.O_DIRECT | JLinux.O_DSYNC;

		switch (mode) {
			case ReadOnly:
				flags |= JLinux.O_RDONLY;
				break;
			case ReadWrite:
				flags |= JLinux.O_RDWR;
				break;
			case WriteOnly:
				flags |= JLinux.O_WRONLY;
				break;
			default:
				break;

		}

		final int fd = JLinux.open(file, flags, 0);
		return new LinuxBlockFileHandle(fd);
	}

	/**
	 * Unlink this file from the filesystem.
	 */

	@Override
	public void unlink(Path file) {
		JLinux.unlink(file);
	}

	@Override
	public Stat stat(Path file) {
		return JLinux.stat(file);
	}

}

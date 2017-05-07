package io.ewok.linux.io;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.CompletableFuture;

import io.ewok.linux.JLinux;

public class AsyncDiskContext implements AutoCloseable {

	/**
	 * The context, 0 if not open.
	 */

	private long ctx = 0;

	/**
	 * Initialise the disk context.
	 *
	 * @param nr_events
	 *            number of concurrent io operations to be supported by this
	 *            context.
	 */

	public void init(int nr_events) {
		this.ctx = JLinux.io_setup(nr_events);
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

	public CompletableFuture<AsyncFile> open(Path file, AsyncDiskPriority priority) {
		return null;
	}

	/**
	 * create a new file and open it for using with async io, using O_DIRECT.
	 *
	 * this can block on io: because there is no async mechanism for opening a
	 * file, we do this in a thread from the caching thread pool.
	 *
	 * The priority indicates how urgent the open operation itself is.
	 *
	 * if this file already exists, the call with fail.
	 *
	 * @param prealloc_bytes
	 *            The number of bytes to preallocate space for. Ideally, this
	 *            should be set to the final size of the file, if it is known in
	 *            advance. Appending small chunks of data at a time to enlarge a
	 *            file will have significant performance penalties when writing.
	 *
	 */

	public CompletableFuture<AsyncFile> create(
			Path file,
			long prealloc_bytes,
			AsyncDiskPriority priority,
			FileAttribute<?>... attrs) {

		// open as a temp file in the same folder
		final int fd = JLinux.open(file.getParent(),
				JLinux.O_TMPFILE | JLinux.O_DIRECT | JLinux.O_DSYNC | JLinux.O_RDWR,
				JLinux.S_IWUSR | JLinux.S_IRUSR);

		// preallocate the space
		JLinux.fallocate(fd, JLinux.FALLOC_FL_KEEP_SIZE, 0, prealloc_bytes);

		// move to the actual destination
		JLinux.linkat(
				Paths.get("/proc/self/fd/").resolve(Integer.toString(fd)),
				file,
				JLinux.AT_SYMLINK_FOLLOW);

		return CompletableFuture.completedFuture(null);

	}

	/**
	 * close the context.
	 */

	@Override
	public void close() {
		JLinux.io_destroy(this.ctx);
	}

	/**
	 *
	 * @param nr_events
	 * @return
	 */

	public static AsyncDiskContext open(int nr_events) {
		final AsyncDiskContext io = new AsyncDiskContext();
		io.init(nr_events);
		return io;
	}

}

/**
 * Copyright (C) 2014 Stephen Macke (smacke@cs.stanford.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.smacke.jaydio;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import lombok.extern.slf4j.Slf4j;
import net.smacke.jaydio.buffer.AlignedDirectByteBuffer;

/**
 * Class containing native hooks and utility methods for performing direct I/O,
 * using the Linux <tt>O_DIRECT</tt> flag.
 * </p>
 *
 * <p>
 * This class is initialized at class load time, by registering JNA hooks into
 * native methods. It also calculates Linux kernel version-dependent alignment
 * amount (in bytes) for use with the <tt>O_DIRECT</tt> flag, when given a
 * string for a file or directory.
 * </p>
 *
 * @author smacke
 *
 */

@Slf4j
public class DirectIoLib {
	private static boolean binit;

	static {
		binit = false;
		try {
			if (!Platform.isLinux()) { // TODO (smacke): test on other *nix
				// variants
				log.warn("Not running Linux, jaydio support disabled");
			} else { // now check to see if we have O_DIRECT...

				final int linuxVersion = 0;
				final int majorRev = 1;
				final int minorRev = 2;

				final List<Integer> versionNumbers = new ArrayList<>();
				for (final String v : System.getProperty("os.version").split("\\.|-")) {
					if (v.matches("\\d")) {
						versionNumbers.add(Integer.parseInt(v));
					}
				}

				/*
				 * From "man 2 open":
				 *
				 * O_DIRECT support was added under Linux in kernel version
				 * 2.4.10. Older Linux kernels simply ignore this flag. Some
				 * file systems may not implement the flag and open() will fail
				 * with EINVAL if it is used.
				 */

				// test to see whether kernel version >= 2.4.10
				if (versionNumbers.get(linuxVersion) > 2) {
					binit = true;
				} else if (versionNumbers.get(linuxVersion) == 2) {
					if (versionNumbers.get(majorRev) > 4) {
						binit = true;
					} else if (versionNumbers.get(majorRev) == 4 && versionNumbers.get(minorRev) >= 10) {
						binit = true;
					}
				}

				if (binit) {
					Native.register(Platform.C_LIBRARY_NAME); // get access to
					// open(),
					// pread(), etc
				} else {
					log.warn(String.format("O_DIRECT not supported on your version of Linux: %d.%d.%d", linuxVersion,
							majorRev, minorRev));
				}
			}
		} catch (final Throwable e) {
			log.warn("Unable to register libc at class load time: " + e.getMessage(), e);
		}
	}

	private final int fsBlockSize;
	private final long fsBlockNotMask;

	// protected for tests
	protected DirectIoLib(int fsBlockSize) {
		this.fsBlockSize = fsBlockSize;
		this.fsBlockNotMask = ~((long) fsBlockSize - 1);
	}

	/**
	 * Static method to register JNA hooks for doing direct I/O
	 * </p>
	 *
	 * @param workingDir
	 *            A directory within the mounted file system on which we'll be
	 *            working Should preferably BE the directory in which we'll be
	 *            working.
	 */
	public static DirectIoLib getLibForPath(String workingDir) {
		final int fsBlockSize = initilizeSoftBlockSize(workingDir);
		if (fsBlockSize == -1) {
			log.warn("O_DIRECT support non available on your version of Linux (" + System.getProperty("os.version")
			+ "), " +
					"please upgrade your kernel in order to use jaydio.");
			return null;
		}
		return new DirectIoLib(fsBlockSize);
	}

	/**
	 * Finds a block size for use with O_DIRECT. Choose it in the most paranoid
	 * way possible to maximize probability that things work.
	 *
	 * @param fileOrDir
	 *            A file or directory within which O_DIRECT access will be
	 *            performed.
	 */
	private static int initilizeSoftBlockSize(String fileOrDir) {

		int fsBlockSize = -1;

		if (binit) {
			// get file system block size for use with workingDir
			// see "man 3 posix_memalign" for why we do this
			final int _PC_REC_XFER_ALIGN = 0x11;

			fsBlockSize = pathconf(fileOrDir, _PC_REC_XFER_ALIGN);
			/*
			 * conservative for version >= 2.6 "man 2 open":
			 *
			 * Under Linux 2.6, alignment to 512-byte boundaries suffices.
			 */

			// Since O_DIRECT requires pages to be memory aligned with the file
			// system block size,
			// we will do this too in case the page size and the block size are
			// different for
			// whatever reason. By taking the least common multiple, everything
			// should be happy:
			final int pageSize = getpagesize();
			fsBlockSize = lcm(fsBlockSize, pageSize);

			// just being completely paranoid:
			// (512 is the rule for 2.6+ kernels as mentioned before)
			fsBlockSize = lcm(fsBlockSize, 512);

			// lastly, a sanity check
			if (fsBlockSize <= 0 || ((fsBlockSize & (fsBlockSize - 1)) != 0)) {
				log.warn("file system block size should be a power of two, was found to be " + fsBlockSize);
				log.warn("Disabling O_DIRECT support");
				return -1;
			}
		}

		return fsBlockSize;
	}

	// -- Java interfaces to native methods

	/**
	 * Interface into native pread function. Always reads an entire buffer,
	 * unlike {@link #pwrite(int, AlignedDirectByteBuffer, long) pwrite()} which
	 * uses buffer state to determine how much of buffer to write.
	 * </p>
	 *
	 * @param fd
	 *            A file discriptor to pass to native pread
	 *
	 * @param buf
	 *            The buffer into which to record the file read
	 *
	 * @param offset
	 *            The file offset at which to read
	 *
	 * @return The number of bytes successfully read from the file
	 *
	 * @throws IOException
	 */

	public int pread(int fd, AlignedDirectByteBuffer buf, long offset) throws IOException {
		buf.clear(); // so that we read an entire buffer
		final int n = pread(fd, buf.pointer(), new NativeLong(buf.capacity()), new NativeLong(offset)).intValue();
		if (n == 0) {
			throw new EOFException("Tried to read past EOF at offset " + offset + " into ByteBuffer " + buf);
		}
		if (n < 0) {
			throw new IOException("error reading file at offset " + offset + ": " + getLastError());
		}
		return n;
	}

	/**
	 * Interface into native pwrite function. Writes bytes corresponding to the
	 * nearest file system block boundaries between <tt>buf.position()</tt> and
	 * <tt>buf.limit()</tt>.
	 * </p>
	 *
	 * @param fd
	 *            A file descriptor to pass to native pwrite
	 *
	 * @param buf
	 *            The buffer from which to write
	 *
	 * @param offset
	 *            The file offset at which to write
	 *
	 * @return The number of bytes successfully written to the file
	 *
	 * @throws IOException
	 */
	public int pwrite(int fd, AlignedDirectByteBuffer buf, long offset) throws IOException {

		// must always write to end of current block
		// To handle writes past the logical file size,
		// we will later truncate.
		final int start = buf.position();
		assert start == this.blockStart(start);
		final int toWrite = this.blockEnd(buf.limit()) - start;

		final int n = pwrite(fd, buf.pointer().share(start), new NativeLong(toWrite), new NativeLong(offset))
				.intValue();
		if (n < 0) {
			throw new IOException("error writing file at offset " + offset + ": " + getLastError());
		}
		return n;
	}

	/**
	 * Use the <tt>open</tt> Linux system call and pass in the <tt>O_DIRECT</tt>
	 * flag. Currently the only other flags passed in are <tt>O_RDONLY</tt> if
	 * <tt>readOnly</tt> is <tt>true</tt>, and (if not) <tt>O_RDWR</tt> and
	 * <tt>O_CREAT</tt>.
	 *
	 * @param pathname
	 *            The path to the file to open. If file does not exist and we
	 *            are opening with <tt>readOnly</tt>, this will throw an error.
	 *            Otherwise, if it does not exist but we have <tt>readOnly</tt>
	 *            set to false, create the file.
	 *
	 * @param readOnly
	 *            Whether to pass in <tt>O_RDONLY</tt>
	 *
	 * @return An integer file descriptor for the opened file
	 *
	 * @throws IOException
	 */
	public int oDirectOpen(String pathname, boolean readOnly) throws IOException {
		int flags = OpenFlags.O_DIRECT;
		if (readOnly) {
			flags |= OpenFlags.O_RDONLY;
		} else {
			flags |= OpenFlags.O_RDWR | OpenFlags.O_CREAT;
		}
		final int fd = open(pathname, flags, 00644);
		if (fd < 0) {
			throw new IOException("Error opening " + pathname + ", got " + getLastError());
		}
		return fd;
	}

	/**
	 * Hooks into errno using Native.getLastError(), and parses it with native
	 * strerror function.
	 *
	 * @return An error message corresponding to the last <tt>errno</tt>
	 */
	public static String getLastError() {
		return strerror(Native.getLastError());
	}

	// -- alignment logic utility methods

	/**
	 * @return The soft block size for use with transfer multiples and memory
	 *         alignment multiples
	 */
	public int blockSize() {
		return this.fsBlockSize;
	}

	/**
	 * Returns the default buffer size for file channels doing O_DIRECT I/O. By
	 * default this is equal to the block size.
	 *
	 * @return The default buffer size
	 */
	public int defaultBufferSize() {
		return this.fsBlockSize;
	}

	/**
	 * Given <tt>value</tt>, find the largest number less than or equal to
	 * <tt>value</tt> which is a multiple of the fs block size.
	 *
	 * @param value
	 * @return The largest number less than or equal to <tt>value</tt> which is
	 *         a multiple of the soft block size
	 */
	public long blockStart(long value) {
		return value & this.fsBlockNotMask;
	}

	/**
	 * @see #blockStart(long)
	 */
	public int blockStart(int value) {
		return (int) (value & this.fsBlockNotMask);
	}

	/**
	 * Given <tt>value</tt>, find the smallest number greater than or equal to
	 * <tt>value</tt> which is a multiple of the fs block size.
	 *
	 * @param value
	 * @return The smallest number greater than or equal to <tt>value</tt> which
	 *         is a multiple of the soft block size
	 */
	public long blockEnd(long value) {
		return (value + this.fsBlockSize - 1) & this.fsBlockNotMask;
	}

	/**
	 * @see #blockEnd(long)
	 */
	public int blockEnd(int value) {
		return (int) ((value + this.fsBlockSize - 1) & this.fsBlockNotMask);
	}

	/**
	 * Static variant of {@link #blockEnd(int)}.
	 *
	 * @param blockSize
	 * @param position
	 * @return The smallest number greater than or equal to <tt>position</tt>
	 *         which is a multiple of the <tt>blockSize</tt>
	 */
	public static long blockEnd(int blockSize, long position) {
		final long ceil = (position + blockSize - 1) / blockSize;
		return ceil * blockSize;
	}

	/**
	 * @param x
	 * @param y
	 * @return The least common multiple of <tt>x</tt> and <tt>y</tt>
	 */
	// Euclid's algo for gcd is more general than we need
	// since we only have powers of 2, but w/e
	public static int lcm(long x, long y) {
		long g = x; // will hold gcd
		long yc = y;

		// get the gcd first
		while (yc != 0) {
			final long t = g;
			g = yc;
			yc = t % yc;
		}

		return (int) (x * y / g);
	}

	/**
	 * Given a pointer-to-pointer <tt>memptr</tt>, sets the dereferenced value
	 * to point to the start of an allocated block of <tt>size</tt> bytes, where
	 * the starting address is a multiple of <tt>alignment</tt>. It is
	 * guaranteed that the block may be freed by calling @{link
	 * {@link #free(Pointer)} on the starting address. See "man 3
	 * posix_memalign".
	 *
	 * @param memptr
	 *            The pointer-to-pointer which will point to the address of the
	 *            allocated aligned block
	 *
	 * @param alignment
	 *            The alignment multiple of the starting address of the
	 *            allocated block
	 *
	 * @param size
	 *            The number of bytes to allocate
	 *
	 * @return 0 on success, one of the C error codes on failure.
	 */
	public static native int posix_memalign(PointerByReference memptr, NativeLong alignment, NativeLong size);

	/**
	 * See "man 3 free".
	 *
	 * @param ptr
	 *            The pointer to the hunk of memory which needs freeing
	 */
	public static native void free(Pointer ptr);

	/**
	 * See "man 2 close"
	 *
	 * @param fd
	 *            The file descriptor of the file to close
	 *
	 * @return 0 on success, -1 on error
	 */
	public native int close(int fd); // musn't forget to do this

	// -- more native function hooks --

	public static int SEEK_SET = 0;
	public static int SEEK_CUR = 0;
	public static int SEEK_END = 0;
	public static int SEEK_DATA = 0;
	public static int SEEK_HOLE = 0;

	public static native long lseek64(int fd, long offset, int whence);

	//

	public static int SYNC_FILE_RANGE_WAIT_BEFORE = 0;
	public static int SYNC_FILE_RANGE_WRITE = 0;
	public static int SYNC_FILE_RANGE_WAIT_AFTER = 0;

	public static native int sync_file_range(int fd, long offset, long nbytes, int flags);

	//

	public static native int fsync(int fd);

	public static native int fdatasync(int fd);

	public static native int ftruncate(int fd, long length);

	// ---

	/*
	 * Don't extend size of file even if offset + len is greater than file size.
	 */
	public static int FALLOC_FL_KEEP_SIZE = 1;

	/* Create a hole in the file. */

	public static int FALLOC_FL_PUNCH_HOLE = 2;

	/*
	 * Remove a range of a file without leaving a hole.
	 */

	public static int FALLOC_FL_COLLAPSE_RANGE = 8;

	/*
	 * Convert a range of a file to zeros.
	 */

	public static int FALLOC_FL_ZERO_RANGE = 16;

	public static native int fallocate(int fd, int mode, long offset, long length);

	private static native NativeLong pwrite(int fd, Pointer buf, NativeLong count, NativeLong offset);

	private static native NativeLong pread(int fd, Pointer buf, NativeLong count, NativeLong offset);

	private static native int open(String pathname, int flags);

	private static native int open(String pathname, int flags, int mode);

	private static native int getpagesize();

	private static native int pathconf(String path, int name);

	private static native String strerror(int errnum);

	// ---

}
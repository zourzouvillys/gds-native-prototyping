package io.ewok.linux.io;

import java.nio.file.Path;

import com.google.common.base.Preconditions;

import io.ewok.io.BlockFileHandle;
import io.ewok.linux.JLinux;
import io.netty.buffer.ByteBuf;

/**
 * An opened file handle which is backed by a disk file.
 *
 * @author theo
 *
 */

public final class LinuxBlockFileHandle implements BlockFileHandle {

	/**
	 * the provided kernel FD.
	 */

	int fd;

	/**
	 *
	 */

	LinuxBlockFileHandle(int fd) {
		Preconditions.checkArgument(fd >= 0);
		this.fd = fd;
	}

	/**
	 * Allocate bytes at the start of this file. It will not update the file
	 * size itself, just issue the bytes on the disk.
	 *
	 * @param bytes
	 */

	public void preallocate(long bytes) {
		JLinux.fallocate(this.fd, JLinux.FALLOC_FL_KEEP_SIZE, 0, bytes);
	}

	/**
	 * Link this file descriptor to a path on the filesystem.
	 */

	public void linkat(Path file) {
		JLinux.linkat(this.fd, null, JLinux.AT_FDCWD, file, JLinux.AT_EMPTY_PATH);
	}

	@Override
	public void close() {
		Preconditions.checkArgument(this.fd >= 0);
		JLinux.close(this.fd);
		this.fd = -1;
	}

	public LinuxBlockFileHandle flush() {
		return this;
	}

	public LinuxBlockFileHandle write(ByteBuf buf) {
		return this;
	}

	public <T> void write(LinuxBlockFileHandle fd, ByteBuf buf, long offset, long length, T attachment) {

	}

}

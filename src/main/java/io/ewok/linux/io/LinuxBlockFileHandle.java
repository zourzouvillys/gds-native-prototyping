package io.ewok.linux.io;

import java.nio.file.Path;

import com.google.common.base.Preconditions;

import io.ewok.io.ReadWriteBlockFileHandle;
import io.ewok.linux.JLinux;
import io.ewok.linux.LinuxStat;

/**
 * An opened file handle which is backed by a disk file.
 *
 * @author theo
 *
 */

public final class LinuxBlockFileHandle implements ReadWriteBlockFileHandle {

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

	@Override
	public LinuxBlockFileHandle preallocate(long bytes) {
		JLinux.fallocate(this.fd, JLinux.FALLOC_FL_KEEP_SIZE, 0, bytes);
		return this;
	}

	/**
	 *
	 */

	@Override
	public LinuxBlockFileHandle truncate(long length) {
		JLinux.ftruncate(this.fd, length);
		return this;
	}

	/**
	 * Link this file descriptor to a path on the filesystem.
	 */

	@Override
	public void linkat(Path file) {
		JLinux.linkat(this.fd, null, JLinux.AT_FDCWD, file, JLinux.AT_EMPTY_PATH);
	}

	/**
	 *
	 */

	@Override
	public void close() {
		Preconditions.checkArgument(this.fd >= 0);
		JLinux.close(this.fd);
		this.fd = -1;
	}

	/**
	 *
	 */

	@Override
	public LinuxBlockFileHandle flush() {
		return this;
	}

	/**
	 *
	 */

	@Override
	public long pageSize() {
		return this.stat().getBlockSize();
	}

	/**
	 *
	 */

	@Override
	public long size() {
		return this.stat().getSize();
	}

	/**
	 * return the stat struct. may block!
	 */

	public LinuxStat stat() {
		return JLinux.fstat(this.fd);
	}

}

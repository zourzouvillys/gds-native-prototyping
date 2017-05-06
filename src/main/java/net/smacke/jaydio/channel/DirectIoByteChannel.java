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
package net.smacke.jaydio.channel;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;

import lombok.Getter;
import net.smacke.jaydio.DirectIoLib;
import net.smacke.jaydio.align.ByteChannelAligner;
import net.smacke.jaydio.buffer.AlignedDirectByteBuffer;
import net.smacke.jaydio.buffer.JavaHeapByteBuffer;

/**
 * An {@link BufferedChannel} implementation which uses {@link DirectIoLib} for
 * JNA hooks to native Linux methods. Particular, the O_DIRECT flag is used.
 * </p>
 *
 * <p>
 * One might wonder why the functionality in this class is not directly subsumed
 * by {@link ByteChannelAligner}. For testing purposes, it made sense to
 * separate out the alignment logic from the actual I/O logic. For example, it
 * is possible to test the alignment logic completely in-memory using
 * {@link MockByteChannel}s and {@link JavaHeapByteBuffer}s thanks to this
 * abstraction.
 * </p>
 *
 * @author smacke
 *
 */
public final class DirectIoByteChannel implements BufferedChannel<AlignedDirectByteBuffer> {

	@Getter
	private final DirectIoLib lib;
	private final int fd;
	private boolean isOpen;
	private long fileLength;
	private final boolean isReadOnly;

	public static DirectIoByteChannel getChannel(File file, boolean readOnly) throws IOException {
		final DirectIoLib lib = DirectIoLib.getLibForPath(file.toString());
		return getChannel(lib, file, readOnly);
	}

	public static DirectIoByteChannel getChannel(DirectIoLib lib, File file, boolean readOnly) throws IOException {
		final int fd = lib.oDirectOpen(file.toString(), readOnly);
		final long length = file.length();
		return new DirectIoByteChannel(lib, fd, length, readOnly);
	}

	private DirectIoByteChannel(DirectIoLib lib, int fd, long fileLength, boolean readOnly) {
		this.lib = lib;
		this.fd = fd;
		this.isOpen = true;
		this.isReadOnly = readOnly;
		this.fileLength = fileLength;
	}

	private void ensureOpen() throws ClosedChannelException {
		if (!this.isOpen()) {
			throw new ClosedChannelException();
		}
	}

	private void ensureWritable() {
		if (this.isReadOnly()) {
			throw new NonWritableChannelException();
		}
	}

	@Override
	public int read(AlignedDirectByteBuffer dst, long position) throws IOException {
		this.ensureOpen();
		return this.lib.pread(this.fd, dst, position);
	}

	@Override
	public int write(AlignedDirectByteBuffer src, long position) throws IOException {
		this.ensureOpen();
		this.ensureWritable();
		assert src.position() == this.lib.blockStart(src.position());

		final int written = this.lib.pwrite(this.fd, src, position);

		// update file length if we wrote past it
		this.fileLength = Math.max(position + written, this.fileLength);
		return written;
	}

	@Override
	public DirectIoByteChannel allocate(int mode, final long offset, final long length) throws IOException {
		this.ensureOpen();
		this.ensureWritable();
		if (DirectIoLib.fallocate(this.fd, mode, offset, length) < 0) {
			throw new IOException("Error during allocate on descriptor " + this.fd + ": " +
					DirectIoLib.getLastError());
		}
		this.fileLength = length;
		return this;
	}

	@Override
	public DirectIoByteChannel truncate(final long length) throws IOException {
		this.ensureOpen();
		this.ensureWritable();
		if (DirectIoLib.ftruncate(this.fd, length) < 0) {
			throw new IOException("Error during truncate on descriptor " + this.fd + ": " +
					DirectIoLib.getLastError());
		}
		this.fileLength = length;
		return this;
	}

	@Override
	public DirectIoByteChannel sync() throws IOException {
		this.ensureOpen();
		this.ensureWritable();
		if (DirectIoLib.fsync(this.fd) < 0) {
			throw new IOException("Error during sync on descriptor " + this.fd + ": " +
					DirectIoLib.getLastError());
		}
		return this;
	}

	@Override
	public DirectIoByteChannel datasync() throws IOException {
		this.ensureOpen();
		this.ensureWritable();
		if (DirectIoLib.fdatasync(this.fd) < 0) {
			throw new IOException("Error during datasync on descriptor " + this.fd + ": " +
					DirectIoLib.getLastError());
		}
		return this;
	}

	@Override
	public long size() {
		return this.fileLength;
	}

	@Override
	public int getFD() {
		return this.fd;
	}

	@Override
	public boolean isOpen() {
		return this.isOpen;
	}

	@Override
	public boolean isReadOnly() {
		return this.isReadOnly;
	}

	@Override
	public void close() throws IOException {
		if (!this.isOpen()) {
			return;
		}
		try {
			if (!this.isReadOnly()) {
				this.truncate(this.fileLength);
			}
		} finally {
			this.isOpen = false;
			if (this.lib.close(this.fd) < 0) {
				throw new IOException("Error closing file with descriptor " + this.fd + ": " +
						DirectIoLib.getLastError());
			}
		}
	}

	@Override
	public BufferedChannel<AlignedDirectByteBuffer> sync(int offset, int nbytes) throws IOException {
		this.ensureOpen();
		this.ensureWritable();
		if (DirectIoLib.sync_file_range(this.fd, offset, nbytes, 0) < 0) {
			throw new IOException("Error during sync_file_range on descriptor " + this.fd + ": " +
					DirectIoLib.getLastError());
		}
		return this;
	}

	@Override
	public BufferedChannel<AlignedDirectByteBuffer> seek(int offset, int whence) throws IOException {
		this.ensureOpen();
		this.ensureWritable();
		if (DirectIoLib.lseek64(this.fd, offset, whence) < 0) {
			throw new IOException("Error during lseek64 on descriptor " + this.fd + ": " +
					DirectIoLib.getLastError());
		}
		return this;
	}
}

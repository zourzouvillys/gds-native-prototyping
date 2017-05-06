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

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;

import net.smacke.jaydio.DirectIoLib;
import net.smacke.jaydio.buffer.JaydioByteBuffer;

/**
 * A mock implementation of {@link BufferedChannel}, to be used for testing. The
 * mock channel is backed {@link #file}, located on java heap.
 * </p>
 *
 * @author smacke
 *
 */
public final class MockByteChannel implements BufferedChannel<JaydioByteBuffer> {

	private long fileLength;
	private boolean isOpen;
	private final boolean isReadOnly;

	// keep the underlying "file" in-memory
	// allows for lightweight mock testing
	private final byte[] file;

	/**
	 * Factory method returning a new {@link MockByteChannel}.
	 * </p>
	 *
	 * @param length
	 *            The maximum length that the "file" will grow to -- this is how
	 *            many bytes will be allocated on Java heap
	 *
	 * @param blockSize
	 *            The block size of the fake file system for this fake file
	 *
	 * @param readOnly
	 *            Whether this "channel" is in readOnly mode
	 *
	 * @return A new {@link MockByteChannel} with the corresponding parameters
	 */
	public static MockByteChannel getChannel(long length, int blockSize, boolean readOnly) {
		return new MockByteChannel(DirectIoLib.blockEnd(blockSize, length), readOnly);
	}

	protected MockByteChannel(long fileLength, boolean readOnly) {
		this.isReadOnly = readOnly;
		this.isOpen = true;
		this.file = new byte[(int) fileLength];
		this.fileLength = readOnly ? fileLength : 0;
	}

	@Override
	public int read(JaydioByteBuffer dst, long position) throws ClosedChannelException {
		this.ensureOpen();
		dst.clear();
		final int endPos = Math.min((int) position + dst.limit(), (int) this.size());
		for (int i = (int) position; i < endPos; i++) {
			dst.put(this.file[i]);
		}
		dst.clear();
		return (int) (endPos - position);
	}

	@Override
	public int write(JaydioByteBuffer src, long position) throws IOException {
		this.ensureOpen();
		this.ensureWritable();
		final int oldPos = src.position();
		src.position(0);

		for (int i = (int) position; i < position + src.limit(); i++) {
			this.file[i] = src.get();
		}

		// If write past current EOF, update the file length.
		this.fileLength = Math.max(position + src.limit(), this.fileLength);
		src.position(oldPos);
		return src.limit();
	}

	private void ensureOpen() throws ClosedChannelException {
		if (!this.isOpen) {
			throw new ClosedChannelException();
		}
	}

	private void ensureWritable() throws NonWritableChannelException {
		if (this.isReadOnly) {
			throw new NonWritableChannelException();
		}
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
	public long size() {
		return this.fileLength;
	}

	@Override
	public MockByteChannel truncate(long fileLength)
			throws IOException {
		this.ensureOpen();
		this.ensureWritable();
		this.fileLength = fileLength;
		return this;
	}

	@Override
	public MockByteChannel allocate(int mode, long offset, long length)
			throws IOException {
		this.ensureOpen();
		this.ensureWritable();
		return this;
	}

	@Override
	public int getFD() {
		throw new UnsupportedOperationException("mock channel is not backed by file");
	}

	@Override
	public void close() throws IOException {
		this.isOpen = false;
	}

	@Override
	public BufferedChannel<JaydioByteBuffer> sync() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedChannel<JaydioByteBuffer> datasync() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedChannel<JaydioByteBuffer> sync(int offset, int len) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedChannel<JaydioByteBuffer> seek(int offset, int whence) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}

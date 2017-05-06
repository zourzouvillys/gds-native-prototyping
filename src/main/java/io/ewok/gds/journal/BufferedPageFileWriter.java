package io.ewok.gds.journal;

import java.util.concurrent.CompletableFuture;

import io.ewok.gds.storage.AsyncFileIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * A writer which has a local buffer that is written to. It can be flushed at
 * any time, and will write pages out when they are full.
 *
 * @author theo
 *
 */

public class BufferedPageFileWriter {

	private final int pageSize;
	private final AsyncFileIO file;

	private long nextPosition = -1;

	// the offset where the head of the page currently is.
	private int head = 0;
	private final ByteBuf buffer;

	/**
	 *
	 * @param pageSize
	 * @param file
	 */

	public BufferedPageFileWriter(int pageSize, AsyncFileIO file) {
		this.pageSize = pageSize;
		this.file = file;
		this.buffer = ByteBufAllocator.DEFAULT.directBuffer(pageSize * 128, pageSize * 128);
	}

	/**
	 * calculate the page offset for the given position.
	 */

	public int pageOffset(int position) {
		return position - (position % this.pageSize);
	}

	/**
	 * seek to the given position
	 */

	public CompletableFuture<?> seek(int position) {
		// load the page
		return this.file.read(this.buffer, this.pageOffset(position), this.pageSize).thenRun(() -> {
			this.head = position;
			this.buffer.writerIndex(position % this.pageSize);
			this.nextPosition = position;
		});
	}

	/**
	 * append the data
	 */

	public void writeBytes(ByteBuf data) {
		this.buffer.writeBytes(data);
	}

	/**
	 * perform a flush of all dirty pages.
	 */

	public CompletableFuture<?> flush(long until) {
		return this.file
				.write(this.buffer, this.head, this.buffer.readableBytes())
				.thenAccept(len -> this.buffer.discardReadBytes())
				.thenApplyAsync((Object val) -> this.file.flush());
	}

	public void writeShort(int i) {
		this.buffer.writeShort(i);
	}

	public void writeInt(int i) {
		this.buffer.writeInt(i);
	}

	public int writeVarUInt32(int value) {
		int len = 0;
		while (true) {
			if ((value & ~0x7F) == 0) {
				len++;
				this.buffer.writeByte((byte) value);
				return len;
			} else {
				this.buffer.writeByte((byte) ((value & 0x7F) | 0x80));
				value >>>= 7;
				len++;
			}
		}
	}

	public void writeBytes(byte[] record) {
		this.buffer.writeBytes(record);
	}

}

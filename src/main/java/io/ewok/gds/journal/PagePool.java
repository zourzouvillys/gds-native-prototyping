package io.ewok.gds.journal;

import java.util.concurrent.CompletableFuture;

import io.ewok.gds.storage.AsyncFileIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PagePool {

	private final int pageSize;
	private final AsyncFileIO file;

	private long nextPosition = -1;

	private final ByteBuf page = Unpooled.directBuffer(8192 * 16, 8192 * 16);

	// the offset where the head of the page currently is.
	private int head = 0;

	public PagePool(int pageSize, AsyncFileIO file) {
		this.pageSize = pageSize;
		this.file = file;
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
		return this.file.read(this.page, this.pageOffset(position), this.pageSize).thenRun(() -> {
			this.head = position;
			this.page.writerIndex(position % this.pageSize);
			this.nextPosition = position;
		});
	}

	/**
	 * append the data
	 */

	public void writeBytes(ByteBuf data) {
		this.page.writeBytes(data);
	}

	/**
	 * perform a flush of all dirty pages.
	 */

	public CompletableFuture<?> flush() {
		return this.file
				.write(this.page, this.head, this.page.readableBytes())
				.thenApplyAsync(val -> this.file.flush());
	}

	public void writeShort(int i) {
		this.page.writeShort(i);
	}

	public void writeInt(int i) {
		this.page.writeInt(i);
	}

	public int writeVarUInt32(int value) {
		int len = 0;
		while (true) {
			if ((value & ~0x7F) == 0) {
				len++;
				this.page.writeByte((byte) value);
				return len;
			} else {
				this.page.writeByte((byte) ((value & 0x7F) | 0x80));
				value >>>= 7;
				len++;
			}
		}
	}

	public void writeBytes(byte[] record) {
		this.page.writeBytes(record);
	}

}

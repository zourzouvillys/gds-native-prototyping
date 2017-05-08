package io.ewok.linux.io;

import io.ewok.io.PageBuffer;
import io.ewok.linux.NativeLinux;
import io.netty.buffer.ByteBuf;
import lombok.NonNull;

public class ByteBufPageBuffer implements PageBuffer {

	private final ByteBuf buffer;

	ByteBufPageBuffer(ByteBuf res) {
		this.buffer = res;
	}

	@Override
	public void release() {
	}

	@Override
	public long memoryAddress() {
		return this.buffer.memoryAddress();
	}

	@Override
	public long pageSize() {
		return this.buffer.capacity();
	}

	// TODO: I/O AT offload?

	@Override
	public void transferTo(@NonNull PageBuffer target, int target_offset, int source_offset, int length) {
		NativeLinux.memcpy(target.memoryAddress() + target_offset, this.memoryAddress() + source_offset, length);
	}

	@Override
	public void setByte(long offset, byte value) {
		this.buffer.setByte((int)offset, value);
	}

	@Override
	public void zero(long offset, long length) {
		this.buffer.setZero((int)offset, (int)length);
	}

}

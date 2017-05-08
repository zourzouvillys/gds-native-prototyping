package io.ewok.io;

import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;

/**
 * A writer which is used for appending data.
 */

public interface MemoryWriter extends AutoCloseable {

	MemoryWriter writeBytes(byte[] bytes);

	MemoryWriter writeBytes(ByteBuffer bytes);

	MemoryWriter writeBytes(ByteBuf bytes);

	@Override
	void close();

}

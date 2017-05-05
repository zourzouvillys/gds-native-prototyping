package io.ewok.gds.heap;

import java.util.concurrent.CompletableFuture;

import io.ewok.gds.buffers.PageConsumer;
import io.netty.buffer.ByteBuf;

/**
 * A heap relation provides a set of FIFO records that are expensive to reorder.
 * When a record is inserted, is is given a unique ID for the object which can
 * not be changed.
 *
 * @author theo
 *
 */

public interface HeapRelation {

	/**
	 * Append a new record to this heap.
	 *
	 * @param data
	 *            The record data.
	 *
	 */

	CompletableFuture<Integer> append(ByteBuf data);

	/**
	 * Read the given record.
	 */

	void read(int record, PageConsumer consumer);

}

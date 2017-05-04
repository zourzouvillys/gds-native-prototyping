package io.ewok.gds.buffers;

import java.util.Optional;

import io.netty.buffer.ByteBuf;

/**
 * A page contains a header, footer, a list of tuples, and the tuples
 * themselves.
 *
 * Indexes, Stream Heaps, and TOAST all use the same page format.
 *
 * All operations on a {@link PageRef} page are immediate, because they are
 * backed by an in memory copy.
 *
 * @author theo
 *
 */

public interface PageRef {

	/**
	 * the WAL LSN for the last change to this record.
	 */

	long lsn();

	/**
	 * The object that this page is part of.
	 */

	int objid();

	/**
	 * The page number.
	 */

	int pageno();

	/**
	 * the largest tuple that can be appended to this page.
	 */

	int freespace();

	/**
	 * Append a new tuple to this page. There must be enough space left in the
	 * page to contain it (e.g, at least {@link #freespace()} bytes).
	 *
	 * @param tuple
	 *            The data for this new tuple, which will be copied.
	 *
	 * @return The tuple index of the newly appended tuple.
	 *
	 */

	int append(ByteBuf tuple);

	/**
	 * fetch a single tuple (by index) from this page by copying it. It will be
	 * retained, so the caller must release it once it is no longer needed.
	 */

	ByteBuf tuple(int index);

	/**
	 * fetches the special data for this page if there is any.
	 *
	 * the buffer must not be modified, and it will be retained so the caller
	 * must release it when finished.
	 *
	 */

	Optional<ByteBuf> special();

	/**
	 * Replaces the special value of this page. The size must remain the same.
	 * It will be copied.
	 */

	void special(ByteBuf special);

}

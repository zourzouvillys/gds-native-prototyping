package io.ewok.io.utils;

import io.ewok.io.EwokPlatform;
import io.ewok.io.MemoryWriter;
import io.ewok.io.PageBufferPool;
import io.ewok.io.ReadWriteBlockFileHandle;

/**
 * Supports chunking of a stream of data backed by block file pages.
 *
 * @author theo
 *
 */

public class ChunkedPageBlockFileWriter {

	/**
	 * various strategy settings.
	 */

	private final ChunkedFileStrategy strategy;

	/**
	 * the pool we will use pages from.
	 */

	private final PageBufferPool pool;

	/**
	 * the current handle we are appending to.
	 */

	private ReadWriteBlockFileHandle current;

	/**
	 *
	 */

	public ChunkedPageBlockFileWriter(ChunkedFileStrategy strategy, PageBufferPool pool) {
		this.strategy = strategy;
		this.pool = pool;
	}

	/**
	 * fetch a writer that can be used to write to this writer.
	 *
	 * note that this stream will interleave any writes using multiple instances
	 * of this. The caller MUST ensure that no one else can write to it at the
	 * same time, and must also close it once finished.
	 *
	 */

	MemoryWriter openWriter() {
		return null;
	}

	/**
	 * open this writer.
	 *
	 * to open, we need to read the page we will be appending to, if it exists.
	 *
	 */

	public void open() {
		this.current = EwokPlatform.fs()
				.openExisting(this.strategy.pathForChunk(this.strategy.chunkForOffset(this.strategy.initialOffset())));
	}

}

package io.ewok.io.utils;

import com.google.common.base.Preconditions;

import io.ewok.io.BlockAccessResult;
import io.ewok.io.BlockAccessService;
import io.ewok.io.MemoryWriter;
import io.ewok.io.PageBuffer;
import io.ewok.io.PageBufferPool;
import io.ewok.io.ReadWriteBlockFileHandle;
import lombok.ToString;

/**
 * Provides a writer which maps to a file on the filesystem, and enables writing
 * of pages out.
 *
 * @author theo
 *
 */

public class PageBufferedBlockFileWriter {

	private final ReadWriteBlockFileHandle file;
	private final long nextWritePosition;
	private final PageBufferPool pool;

	@ToString
	static final class LinkedPage {
		// the page number
		int pageno = -1;
		PageBuffer page = null;
		LinkedPage next = null;
		int length = 0;
	}

	private LinkedPage head = null;
	private LinkedPage tail = null;
	private final long pageSize;
	private final BlockAccessService io;

	/**
	 * Open for writing.
	 *
	 * @param pool
	 *            The pool to allocate pages from
	 * @param file
	 *            The file handle we are writing to. must be read-write if
	 *            nextWritePosition > 0.
	 * @param io
	 *            The {@link BlockAccessService} to use for IO
	 * @param nextWritePosition
	 *            the offset to start appending.
	 */

	public PageBufferedBlockFileWriter(
			PageBufferPool pool,
			ReadWriteBlockFileHandle file,
			BlockAccessService io,
			long nextWritePosition) {

		this.pool = pool;
		this.file = file;
		this.io = io;
		this.nextWritePosition = nextWritePosition;
		this.pageSize = file.pageSize();
	}

	/**
	 * Initialise by loading a page (so we can do a full page write back once
	 * appended), if we need to.
	 */

	public void init() {

		if (BlockUtils.offsetInPage(this.nextWritePosition, this.pageSize) != 0) {

			final LinkedPage page = new LinkedPage();

			page.page = this.pool.allocate();

			page.length = BlockUtils.offsetInPage(this.nextWritePosition, this.pageSize);
			page.pageno = BlockUtils.pageNumberForOffset(this.nextWritePosition, this.pageSize);

			// perform a read of the page, aligned to the page.
			this.io.read(
					this.file,
					page.page,
					BlockUtils.offsetOfPageFor(this.nextWritePosition, this.pageSize),
					this.pageSize,
					this::synced,
					page);

		}

	}

	/*
	 * callback when the initial read has finished.
	 */

	private void synced(BlockAccessResult result, LinkedPage page) {

		// merge the data from the read into this one, and replace it.

		Preconditions.checkState(page.length <= result.length(), "invalid read length", page.length, result.length());

		if (this.head != null) {

			result.page().transferTo(this.head.page, 0, 0, page.length);

		} else {

			page.page.zero(page.length, this.file.pageSize() - page.length);
			this.head = page;

		}

	}

	/**
	 * flush any dirty pages out to disk, and release them back to the pool.
	 */

	public void flush() {
		System.err.println(this.head);
		this.io.write(this.file, this.head.page, (this.head.pageno * this.file.pageSize()), this.file.pageSize(),
				System.err::println);
	}

	/*
	 * append a new page to the tail
	 */

	private LinkedPage appendPage() {
		final LinkedPage page = new LinkedPage();
		page.page = this.pool.allocate();
		if (this.tail != null) {
			this.tail.next = page;
		}
		this.tail = page;
		return page;
	}

	/**
	 * fetch a MemoryWriter to append, which rotates and flushes the page if
	 * needed.
	 */

	public MemoryWriter openWriter() {
		this.head = this.appendPage();
		return null;
	}

}

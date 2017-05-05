package io.ewok.gds.storage;

import java.util.concurrent.CompletableFuture;

import io.ewok.gds.buffers.pages.PageFork;
import io.netty.buffer.ByteBuf;

/**
 * Each storage relation can have multiple "forks" - different areas that
 * contain types of data for pages to be allocated.
 *
 * Each fork is accessed using this interface.
 *
 * @author theo
 *
 */

public interface StorageRelationFork extends StorageEntity {

	/**
	 * create this fork and open it, failing if it already exists.
	 *
	 * @param numpagealloc
	 *            The number of pages to preallocate space for. The pages will
	 *            not actually be created, just space reserved on the filesystem
	 *            for them if the underlying storage supports this.
	 *
	 */

	CompletableFuture<?> create(int numpagealloc);

	/**
	 * checks if this fork exists without opening.
	 */

	CompletableFuture<Boolean> exists();

	/**
	 * How many pages are in this fork.
	 */

	CompletableFuture<Integer> pagecount();

	/**
	 * Extend this {@link PageFork} by appending one or more pages to the end of
	 * this object.
	 *
	 * Rejected if the page number is not exactly one more than the current size
	 * of this fork.
	 *
	 * @param pageno
	 *            The page number of the first new page to add.
	 *
	 * @param numpages
	 *            The number of pages to add.
	 *
	 * @param buffer
	 *            The buffer that must have at least (pageSize() * pagecount)
	 *            bytes readable. The page starts from the reader index of the
	 *            buffer.
	 *
	 */

	CompletableFuture<?> extend(int pageno, int numpages, ByteBuf buffer);

	/**
	 * Reads the given page number from storage into the provided buffer.
	 *
	 * @param pageno
	 *            The page number to read.
	 * @param buffer
	 *            A {@link ByteBuf} of the page size.
	 */

	CompletableFuture<ByteBuf> read(int pageno, ByteBuf buffer);

	/**
	 * Writes the given buffer out to storage as the specified page number,
	 * optionally flushing to disk.
	 *
	 * This can not be used for writing out a page that extends the size of the
	 * fork - the page number must already exist. instead,
	 * {@link #extend(int, int, ByteBuf)} must be used for that.
	 *
	 * @param pageno
	 *            The page number to write.
	 * @param buffer
	 *            The buffer content for this page.
	 * @param fsync
	 *            If the page should be synchronised to storage.
	 */

	CompletableFuture<ByteBuf> write(int pageno, ByteBuf buffer, boolean fsync);

	/**
	 * Shrinks the fork so that it consists of the specified number of pages.
	 *
	 * @param npages
	 *            The number of pages that should be in this fork after
	 *            completing. Any pages after this number will be removed.
	 */

	CompletableFuture<?> truncate(int npages);

	/**
	 * Advisory indication to the storage layer that the given pages should be
	 * flushed to disk.
	 *
	 * @param pageno
	 *            The first page number to flush.
	 * @param numpages
	 *            The number of pages to flush.
	 */

	CompletableFuture<?> writeback(int pageno, int numpages);

	/**
	 * Advisory indication to the storage layer that the given page number will
	 * most likely soon be needed, but not immediately.
	 */

	void prefetch(int pageno);

}

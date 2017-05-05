package io.ewok.gds.storage;

import java.util.concurrent.CompletableFuture;

import io.ewok.gds.buffers.pages.PageFork;
import io.netty.buffer.ByteBuf;

/**
 * Direct storage access.
 *
 * Instances of this interface do not use the buffer manager. Consumers should
 * always use the buffer manager for accessing pages (except for recovery
 * tools).
 *
 * @author theo
 *
 */

public interface StorageRelation {

	/**
	 *
	 * @param fork
	 */

	CompletableFuture<?> create(PageFork fork);

	/**
	 *
	 * @param fork
	 * @return
	 */

	CompletableFuture<Boolean> exists(PageFork fork);

	/**
	 * Extend the {@link PageFork} by adding the given page number.
	 *
	 * @param fork
	 * @param pageno
	 * @param page
	 */

	CompletableFuture<?> extend(PageFork fork, long pageno, ByteBuf page);

	/**
	 *
	 * @param fork
	 * @param pageno
	 */

	void prefetch(PageFork fork, long pageno);

	/**
	 *
	 * @param fork
	 * @param pageno
	 * @param buffer
	 */

	CompletableFuture<ByteBuf> read(PageFork fork, long pageno, ByteBuf buffer);

	/**
	 *
	 * @param fork
	 * @param pageno
	 * @param buffer
	 * @param fsync
	 */

	CompletableFuture<ByteBuf> write(PageFork fork, long pageno, ByteBuf buffer, boolean fsync);

	/**
	 *
	 * @param fork
	 * @param pageno
	 * @param numpages
	 */

	CompletableFuture<?> writeback(PageFork fork, long pageno, int numpages);

	/**
	 *
	 * @param fork
	 */

	CompletableFuture<Integer> nblocks(PageFork fork);

	/**
	 *
	 * @param fork
	 * @param npages
	 */

	CompletableFuture<?> truncate(PageFork fork, long npages);

	/**
	 *
	 * @param fork
	 */

	CompletableFuture<?> sync(PageFork fork);

	/**
	 *
	 */

	CompletableFuture<?> unlink(PageFork fork);

	/**
	 *
	 */

	CompletableFuture<?> close();

}

package io.ewok.gds.buffers.pages;

import java.util.concurrent.CompletableFuture;

import io.ewok.gds.buffers.InvalidPageRefException;
import io.netty.buffer.ByteBuf;

/**
 * Provides physical page access.
 *
 * "physical" could be local disk, network, or shared memory.
 *
 */

public interface PageAccessService {

	/**
	 * Perform a fetch from disk.
	 *
	 * @param buffer
	 *            The buffer that the file content should be placed in.
	 * @param path
	 *            The path to the file that needs to be loaded.
	 * @param offset
	 *            Offset in the file to load.
	 *
	 * @return A future which will complete when the operation has completed.
	 *
	 * @throws InvalidPageRefException
	 *             The referenced page does not exist. Retrying the operation
	 *             will not succeed.
	 *
	 * @throws CorruptPageException
	 *             The requested page was corrupted, and could not be recovered.
	 *             Retryign the operation without a recovery operaiton will
	 *             cause the same error.
	 *
	 * @throws PageUnavailableException
	 *             The page is currently unavailable, but may be available
	 *             later.
	 *
	 */

	public CompletableFuture<ByteBuf> read(ByteBuf buffer, PageId pageId);

	/**
	 * Perform a write from disk.
	 *
	 * @param buffer
	 *            The buffer that the file content should be placed in.
	 * @param path
	 *            The path to the file that needs to be loaded.
	 * @param offset
	 *            Offset in the file to load.
	 *
	 * @return A future which will complete when the operation has completed.
	 */

	public CompletableFuture<?> write(ByteBuf buffer, PageId pageId);

}

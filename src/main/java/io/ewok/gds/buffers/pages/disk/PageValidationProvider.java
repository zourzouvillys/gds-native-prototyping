package io.ewok.gds.buffers.pages.disk;

import io.ewok.gds.buffers.pages.PageId;
import io.netty.buffer.ByteBuf;

/**
 * Performs checksum validation and calculation on pages.
 *
 * @author theo
 *
 */

public interface PageValidationProvider {

	/**
	 * Calculate the checksum for this buffer.
	 *
	 * The buffer may not be modified while this is being called. It may be
	 * read, although the checksum field will be modified.
	 *
	 * @param page
	 *            The full page.
	 * @param pageId
	 *            The {@link PageId} of this page.
	 *
	 */

	void update(ByteBuf page, PageId pageId);

	/**
	 * Validate the page checksum.
	 *
	 * The buffer will not be modified while the checksumming is performed,
	 * however it may not be modified while it is occurring.
	 *
	 * @param page
	 *            The page.
	 *
	 * @param pageId
	 *            The {@link PageId} of this page.
	 */

	boolean validate(ByteBuf page, PageId pageId);

}

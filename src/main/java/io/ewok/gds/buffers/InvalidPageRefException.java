package io.ewok.gds.buffers;

import io.ewok.gds.buffers.pages.PageId;

/**
 * indicates a reference to an invalid page.
 *
 * @author theo
 *
 */

public class InvalidPageRefException extends RuntimeException {

	/**
	 *
	 */

	private static final long serialVersionUID = 1L;

	private final PageId pageId;

	/**
	 *
	 * @param pageId
	 */

	public InvalidPageRefException(PageId pageId) {
		super("Invalid PageId " + pageId);
		this.pageId = pageId;
	}

}

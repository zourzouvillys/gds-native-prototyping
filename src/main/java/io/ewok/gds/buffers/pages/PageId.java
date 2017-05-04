package io.ewok.gds.buffers.pages;

import lombok.Value;

/**
 *
 */

@Value
public class PageId {

	/** the object identifier, e.g stream or index. */
	final long objectId;

	/** the fork type */
	final PageFork forkId;

	/** the page number */
	final long pageId;

	@Override
	public String toString() {
		return String.format("[%s:%s:%s]", this.objectId, this.forkId, this.pageId);
	}

}
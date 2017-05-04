package io.ewok.gds.buffers.pages;

/**
 * An object has a number of different forks (types of data).
 */

public enum PageFork {

	/**
	 * the main set of pages.
	 */

	Main,

	/**
	 * keeps track of which pages have freespace.
	 */

	FreeSpace,

	/**
	 * tracks which pages have empty space.
	 */

	VisibilityMap,

}

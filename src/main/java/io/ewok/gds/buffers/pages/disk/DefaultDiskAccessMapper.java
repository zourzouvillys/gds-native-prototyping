package io.ewok.gds.buffers.pages.disk;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.ewok.gds.buffers.pages.PageFork;
import io.ewok.gds.buffers.pages.PageId;

/**
 * When a page needs to be retrieves from disk, it is requested using its object
 * id, page type, and page number. These map to physical files using an instance
 * of this interface.
 *
 * At first glance it would seem to make most sense to have a separate file for
 * each object ID. However many use cases create very small streams with just a
 * handful of events. For example, a stream for a web session or authentication
 * key. These streams may live forever, but rarely accessed.
 *
 * These streams also don't fill up a whole page. Often, they will have just the
 * page header, and a couple of small tuples. Keeping the whole page on disk
 * (8k) just for a few bytes of data can use significant amounts of storage.
 *
 * So, we have a mechanism which coalesces objects with a small amount of pages
 * together, and pages which are not full compressed.
 *
 * The index keeps track of the objects and their status.
 *
 *
 *
 * @author theo
 *
 */

public class DefaultDiskAccessMapper implements DiskAccessMapper {

	// 2GB per file of pages, so 250,000 pages.
	private final int pagesPerFile = 250_000;
	private final int pageSize = 8192;

	private final Path base;

	public DefaultDiskAccessMapper(Path base) {
		this.base = base;
	}

	/**
	 * Given a {@link PageId}, returns the filesystem path for this.
	 *
	 * @param page
	 *
	 * @return
	 */

	@Override
	public Path map(PageId page) {
		final String file = String.format("%08X", page.getObjectId());
		final String part = String.format("%d", (int) Math.floor(page.getPageId() / this.pagesPerFile));
		return this.base.resolve(Paths.get(file + "." + part + "." + this.getExtension(page.getForkId())));
	}

	/**
	 * Calculate the filesystem extension for a given {@link PageFork}.
	 *
	 * @param forkId
	 * @return
	 */

	private String getExtension(PageFork forkId) {
		switch (forkId) {
			case Main:
				return "db";
			case FreeSpace:
				return "fs";
			case VisibilityMap:
				return "vm";
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public long offset(PageId pageId) {
		return (pageId.getPageId() % this.pagesPerFile) * this.pageSize;
	}

}

package io.ewok.gds.buffers.pages.disk;

import java.nio.file.Path;

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
 * @author theo
 *
 */

public interface DiskAccessMapper {

	public Path map(PageId page);

	public long offset(PageId pageId);

}

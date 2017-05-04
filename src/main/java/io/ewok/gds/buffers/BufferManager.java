package io.ewok.gds.buffers;

import java.util.function.Consumer;

/**
 * Keeps track of all loaded pages, requests, and flushing.
 *
 * It ensures that we keep working memory to a limited size, expiring old pages,
 * etc.
 *
 * At a high level, consumers request pages. Pages are buffered from their
 * source (network or disk) into a circular buffer. For standard processing, a
 * buffer is shared among all readers. For sequence scans and background workers
 * (such as scavanging/vacuuming), a unique buffer is used for loading of pages
 * to ensure they are expired faster and don't affect the main work buffer pool.
 *
 * If a page is not in the main buffer pool and it is requested, then it is
 * moved into it.
 *
 * Each page access request increments the "use count" by one. As we look for a
 * page to expire, we traverse the circular buffer and decrement the use count
 * by one. This ensures that the most unused pages are expired the quickest,
 * without the overhead of LRU.
 *
 * The {@link BufferManager} ensures that operations can always proceed, even if
 * it requires blocking on I/O if there is not enough buffer memory. A buffer
 * pool can operate with just a single page in theory. If there is not enough
 * buffer space, then the request is enqueued until a page can be evicted.
 *
 * The number of buffers must be at least the size of the number of active pages
 * needed to be locked at the same time. Other than that, it is recommended that
 * the buffer size be as large as possible - the larger it is, the less i/o is
 * performed.
 *
 * For work which does not need durability (e.g, temporary buffers), page
 * consistency and/or WAL functionality can be disabled. In these scenarios,
 * buffers are only written to disk as needed, and removed when the buffer
 * manager is closed.
 *
 * @author theo
 *
 */

public interface BufferManager {

	/**
	 * Request a page is fetched from the backing store and a consumer
	 * dispatched to process it.
	 *
	 * When the page is loaded, the consumer will be called, and passed the
	 * page. This may be immediate if it is already available in the buffer
	 * pool. It may take some time if there is a large backlog of work.
	 *
	 * The page will only be dispatched to the consumer when it has been loaded
	 * and a lock acquired. The lock will remain valid until the consumer
	 * returns from handling the page, unless it is extended. Consumers must be
	 * very careful about extending locks, and is not recommended.
	 *
	 * Changes made to a loaded page will be written to the write ahead log, and
	 * persisted based on the configuration of the buffer manager and backing
	 * store.
	 *
	 * @param objid
	 *            The object ID to buffer.
	 *
	 * @param pageno
	 *            The page number of the object to buffer.
	 *
	 * @param consumer
	 *            The handler which will receive the page when it is available.
	 *
	 */

	void load(long objid, long pageno, Consumer<PageRef> consumer);

}

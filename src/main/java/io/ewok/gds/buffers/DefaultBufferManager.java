package io.ewok.gds.buffers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.ewok.gds.buffers.pages.PageAccessService;
import io.ewok.gds.buffers.pages.PageFork;
import io.ewok.gds.buffers.pages.PageId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultBufferManager implements BufferManager {

	/**
	 * Each page which needs to be loaded (or is in the process of being loaded)
	 * is issued with one of these. If the page is already loaded, then it is
	 * skipped.
	 */

	@Value
	private static class PendingPageLoad {

		/**
		 * The {@link PageId} that this pending load is for.
		 */

		private PageId pageId;

		/**
		 * The consumers which are waiting for this page. We limit the number of
		 * consumers that can wait on a page.
		 */

		private final List<Consumer<PageRef>> consumers = new ArrayList<>();

		/**
		 * the target slot ID for this page when it is loaded. -1 if one has not
		 * yet been allocated.
		 */

		private final int targetSlotId = -1;

	}

	/**
	 * A single pool of buffers.
	 */

	private class BufferClockPool {

		/**
		 * The direct (off-heap) memory pool that pages are loaded into. We use
		 * a single contiguous block of memory, to avoid GC pressure.
		 */

		private final ByteBuf buffer;

		/**
		 * the number of slots in this pool.
		 */

		private final int slots;

		/**
		 * The java objects which are wrappers around a single page buffer.
		 */

		private final MemoryPageRef[] pages;

		/**
		 * a counter of the number of times this slot has been requested. it is
		 * zeroed when a page is evicted and new one loaded.
		 */

		private final int[] usecount;

		/**
		 * the number of pins this buffer has. A slot can not be expired while
		 * it is pinned.
		 */

		private final short[] pincount;

		/**
		 * if the page in this slot is dirty. Changed to true when the page is
		 * modified, and changed back to false once it is flushed to disk.
		 *
		 * TODO: use a bitmap instead?
		 *
		 */

		private final boolean[] dirty;

		/**
		 * The page allocation pointer, which indicates where in the clock we
		 * are currently allocating. It starts at offset zero and continues
		 * until it wraps around. Once it has been wrapped, we decrement the
		 * usecnt until one drops to zero, which then triggers it to be released
		 * and can be reused.
		 */

		private volatile int pageAllocationPointer = 0;

		/**
		 * an index for looking up which pages are currently allocated and in
		 * the pool.
		 */

		private final Map<PageId, Integer> index;

		/**
		 * the index of all pending page requests.
		 */

		private final PendingPageLoad[] pendingLoads;

		/**
		 * pointer to the pending load head.
		 */

		private volatile int pendingLoadPointer = 0;

		/**
		 * Initialise the pool.
		 */

		BufferClockPool(int pages, int backlog) {

			this.slots = pages;

			this.pages = new MemoryPageRef[this.slots];
			this.index = new HashMap<>(this.slots);

			this.usecount = new int[this.slots];
			this.pincount = new short[this.slots];
			this.dirty = new boolean[this.slots];

			this.pendingLoads = new PendingPageLoad[backlog];

			// allocate the memory.
			final int bytes = pages * 8192;
			this.buffer = Unpooled.directBuffer(bytes, bytes);

			log.info("Created page buffer pool of {} pages ({} bytes), queue depth of {} loads.", this.slots, bytes,
					backlog);

		}

		/**
		 * Indicates if this page is currently in the cache. If it is, it is
		 * pinned and returned.
		 *
		 * This must be called with a lock.
		 *
		 */

		MemoryPageRef peek(PageId pageId) {

			final Integer slotId = this.index.get(pageId);

			if (slotId == null) {
				// this page is not currently in the cache. it will need to be
				// loaded.
				return null;
			}

			log.info("Fast-path load of page {}", pageId);

			// increment the pincount.
			this.pincount[slotId]++;

			// increment the load count.
			this.usecount[slotId]++;

			return this.pages[slotId];

		}

		/**
		 * when a page is not found in the buffers and needs to be loaded.
		 *
		 * we firstly need to find a slot that the buffer can be placed in. Once
		 * we have a slot, the page is requested from the disk. Finally, once it
		 * is loaded we dispatch to the consumer requesting it.
		 *
		 * @param consumer
		 *
		 */

		private void load(PageId pageId, Consumer<PageRef> consumer) {

			log.info("Loading {} for consumer access", pageId);

			// allocate a slot to load a buffer into into by looping until we
			// are no longer decrementing a use count (meaning all pages are
			// pinned or dirty and need to be flushed). In that case we are
			// backlogged and must wait until a slot becomes free either due to
			// becoming unpinned or because it has been flushed.

			final int slot = this.freeSlot();

			if (slot == -1) {
				// no slot was found. we need to wait. perhaps we can hurry it
				// up by flushing?
				log.warn("No empty slots, waiting");
				return;
			}

			log.info("Slot {} free", slot);

			final MemoryPageRef ref = this.fetch(pageId, slot);

			consumer.accept(ref);

			this.unpin(slot);

		}

		private MemoryPageRef fetch(PageId pageId, int slot) {

			final ByteBuf page = this.buffer.retainedSlice(slot * 8192, 8192).clear();

			final MemoryPageRef ref = new MemoryPageRef(page, 0, 0, Optional.empty());

			DefaultBufferManager.this.storage.read(page, pageId);

			this.pages[slot] = ref;
			this.pincount[slot] = 1;
			this.usecount[slot] = 1;
			this.dirty[slot] = false;

			return ref;

		}

		/**
		 * runs through each slot, trying to find a free one either because it
		 * is empty or because we decrement the usecount.
		 *
		 * it returns a slot which available for use. it potentially removes an
		 * existing clean unpinned pageref from the buffers (but never flushes).
		 * If a slot is returned, then the allocator is left at the next slot
		 * ready to continue allocating.
		 *
		 */

		private int freeSlot() {

			// continue to loop while we are decrementing usecnt of a slot.
			boolean flushed = true;

			while (flushed) {

				flushed = false;

				for (int i = 0; i < this.slots; ++i) {

					final int slot = (this.pageAllocationPointer + i) % this.slots;

					if (this.pages[slot] == null) {
						// this slot is free, we can use it straight away.
						this.pageAllocationPointer = slot + 1;
						return slot;
					}

					if (this.usecount[slot] > 0) {
						// decrement, even if dirty or pinned as long as it has
						// usecnt.
						--this.usecount[slot];
						flushed = true;
					}

					if (this.dirty[slot]) {
						// page is dirty, need to skip.
						continue;
					}

					if (this.pincount[slot] > 0) {
						// page is currently pinned, skip.
						continue;
					}

					if (this.usecount[slot] == 0) {
						// slot can be reused.
						this.unlink(slot);
						this.pageAllocationPointer = slot + 1;
						return slot;
					}

				}

			}

			return -1;

		}

		/**
		 * unlink the slot, prepare it to be used for a new buffer.
		 */

		private void unlink(int slot) {

			log.info("Unlinking slot {}", slot);

			Objects.requireNonNull(this.pages[slot]);

			this.pages[slot].unlink();

			this.pages[slot] = null;
			this.pincount[slot] = 0;
			this.usecount[slot] = 0;
			this.dirty[slot] = false;

		}

		/**
		 * unpin the given pageId, which must be pinned.
		 */

		void unpin(PageId pageId) {
			this.unpin(this.index.get(pageId));
		}

		/**
		 * unpin the page at the given slot.
		 */

		void unpin(int slot) {
			this.pincount[slot]--;
		}

	}

	/**
	 * The default buffer pool which is used for standard runtime operations,
	 * excluding scans and maintenance.
	 *
	 * If there is empty space in the buffer, a scan will populate it, but with
	 * a use count of zero. This avoids loading the same page multiple times
	 * while we do have empty buffer space, but doesn't avoid it being evicted
	 * as soon as it is needed.
	 *
	 */

	private final BufferClockPool defaultPool = new BufferClockPool(8, 16);

	/**
	 * The WAL writer, which is the sink for any page changes. Each write has a
	 * position, which is periodically flushed. Dirty pages can not be written
	 * to disk until the most recent WAL write for that page has been flushed.
	 *
	 * If the backend is direct disk, the WAL also receives backup page copies
	 * the first time they are modified after a checkpoint. For network and
	 * external process storage, this is handled by the storage layer.
	 *
	 */

	private final PageAccessService storage;

	/**
	 *
	 */

	public DefaultBufferManager(PageAccessService storage) {
		this.storage = storage;
	}

	/**
	 * Signal from the consumer that it wishes to access a page.
	 *
	 * We have a fastpath which is used when the page is loaded into memory
	 * already and not locked. This will cause the page to be immediately
	 * dispatched.
	 *
	 */

	@Override
	public void load(long objid, long pageno, Consumer<PageRef> consumer) {

		final PageId pageId = new PageId(objid, PageFork.Main, pageno);

		final MemoryPageRef page = this.defaultPool.peek(pageId);

		if (page != null) {
			consumer.accept(page);
			this.defaultPool.unpin(pageId);
			return;
		}

		// we need to fetch this page from storage.
		this.defaultPool.load(pageId, consumer);

	}

}

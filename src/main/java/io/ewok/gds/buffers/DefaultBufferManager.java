package io.ewok.gds.buffers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Preconditions;

import io.ewok.gds.buffers.pages.PageAccessService;
import io.ewok.gds.buffers.pages.PageFork;
import io.ewok.gds.buffers.pages.PageId;
import io.ewok.gds.buffers.pages.disk.DiskPageAccessService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultBufferManager implements BufferManager {

	/**
	 * A single pool of buffers.
	 */

	private class BufferClockPool {

		/**
		 * An instance of this is allocated when a page load is requested, and
		 * lasts until the page is evicted.
		 */

		private class BufferPageLoad {

			/**
			 * the page being loaded.
			 */

			PageId pageId = null;

			/**
			 * the consumers which are waiting for this page.
			 */

			List<PageConsumer> consumers = new ArrayList<>(8);

			/**
			 * While retrieving a page, the future which is provided by the page
			 * access service.
			 */

			CompletableFuture<ByteBuf> future;

			/**
			 * The page reference.
			 */

			MemoryPageRef pageref;

			/**
			 * How many times the page in this slot has been requested.
			 */

			int usecount = 0;

			/**
			 * how many currently active references are using this slot.
			 */

			int pincount = 0;

			/**
			 * If the page is dirty and needs to be flushed.
			 */

			boolean dirty = false;

			/**
			 * Adds a consumer that wants this page. Dispatches when it becomes
			 * available.
			 */

			public void enqueue(PageConsumer consumer) {

				this.usecount++;
				this.pincount++;

				// if the page is already loaded, we can dispatch straight away.
				if (this.pageref != null) {
					throw new RuntimeException();
				} else {
					// oterwise we need to enqueue.
					this.consumers.add(consumer);
				}

			}

			/**
			 * when this pending load is assigned a slot, and should perform a
			 * lot.
			 *
			 * @param storage
			 */

			public void load(PageAccessService storage, ByteBuf buffer) {

				if (this.pageId.getPageId() == -1) {

					// we are creating a new page, so don't need to load
					// anything.

					this.pageref = new MemoryPageRef(buffer, (int) this.pageId.getObjectId(), 9999, Optional.empty());

					while (!this.consumers.isEmpty()) {

						final PageConsumer consumer = this.consumers.remove(0);

						try {
							consumer.accept(null, this.pageref);
						} catch (final Throwable t) {
							t.printStackTrace();
						} finally {
							this.pincount--;
						}

					}

				} else {

					// request the page from storage.
					this.future = storage.read(buffer, this.pageId).whenComplete(this::loaded);

				}

			}

			/**
			 * called back when the disk access service has the page (or an
			 * error).
			 *
			 * this may be called on a disk access thread, so we need to migrate
			 * it over before dispatching.
			 *
			 * @param buf
			 *            The buffer which has the page in it. The page will be
			 *            verified and checked.
			 *
			 * @param ex
			 *            The exception, if one occurred.
			 *
			 */

			private void loaded(ByteBuf buffer, Throwable ex) {

				this.future = null;

				if (ex != null) {

					// an error occurred while loading this page.
					// remove ourselves from the slot and dispatch to consumers.

					while (!this.consumers.isEmpty()) {

						final PageConsumer consumer = this.consumers.remove(0);

						try {
							consumer.accept(ex, null);
						} catch (final Throwable t) {
							t.printStackTrace();
						} finally {
							this.pincount--;
						}

					}

					return;

				}

				this.pageref = new MemoryPageRef(
						buffer,
						(int) this.pageId.getObjectId(),
						(int) this.pageId.getPageId(),
						Optional.empty());

				while (!this.consumers.isEmpty()) {

					final PageConsumer consumer = this.consumers.remove(0);

					try {
						consumer.accept(null, this.pageref);
					} catch (final Throwable t) {
						t.printStackTrace();
					} finally {
						this.pincount--;
					}

				}

			}

			public void unlink() {
				Preconditions.checkState(this.pincount == 0);
				Preconditions.checkState(this.usecount == 0);
			}

		}

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
		 * Each loaded buffer in the buffer pool.
		 */

		private final BufferPageLoad[] buffers;

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
		 * the pool as well as pending.
		 */

		private final Map<PageId, BufferPageLoad> pageIndex;

		/**
		 * A clock of pending page loads. Already preallocated.
		 *
		 * We cycle through and dispatch as buffer space becomes available in a
		 * FIFO order. The {@link #pendingLoadPointer} indicates where in the
		 * array wheel we are currently processing.
		 *
		 */

		private final Queue<BufferPageLoad> pendingLoads = new LinkedList<>();

		/**
		 * The number of allowed backlog work.
		 */

		private final int backlog;

		/**
		 * Initialise the pool.
		 */

		BufferClockPool(int pages, int backlog) {

			this.slots = pages;

			this.buffers = new BufferPageLoad[this.slots];
			this.pageIndex = new HashMap<>(this.slots + backlog);

			this.backlog = backlog;

			// allocate the memory for the buffers.
			final int bytes = pages * 8192;
			this.buffer = Unpooled.directBuffer(bytes, bytes);

			log.info("Created page buffer pool of {} pages ({} bytes), queue depth of {} loads.", this.slots, bytes,
					backlog);

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

					if (this.buffers[slot] == null) {
						// this slot is free, we can use it straight away.
						this.pageAllocationPointer = slot + 1;
						return slot;
					}

					if (this.buffers[slot].usecount > 0) {
						// decrement, even if dirty or pinned as long as it has
						// usecnt.
						--this.buffers[slot].usecount;
						flushed = true;
					}

					if (this.buffers[slot].dirty) {
						// page is dirty, need to skip.
						continue;
					}

					if (this.buffers[slot].pincount > 0) {
						// page is currently pinned, skip.
						continue;
					}

					if (this.buffers[slot].usecount == 0) {
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
			this.buffers[slot].unlink();
			this.buffers[slot] = null;
		}

		/**
		 * dispatch a load (or append) request.
		 *
		 * returns false if the load is rejected because of too much backlog,
		 * otherwise true.
		 *
		 */

		public boolean dispatch(PageId pageId, PageConsumer consumer) {

			//
			BufferPageLoad loadedBuffer = this.pageIndex.get(pageId);

			// if it is already loaded, then skip.
			if (loadedBuffer != null) {
				log.info("Fast-path load of page {}", pageId);
				loadedBuffer.enqueue(consumer);
				return true;
			}

			// this page is not currently buffered nor is it pending.
			// allocate a new load, and try to dispatch.

			if (this.pendingLoads.size() >= this.backlog) {
				// too much pending work already.
				return false;
			}

			// allocate new load.

			loadedBuffer = new BufferPageLoad();
			loadedBuffer.pageId = pageId;
			loadedBuffer.enqueue(consumer);

			// if we have a free slot (or can make one available right now
			// without blocking), dispatch immediately.

			final int slot = this.freeSlot();

			if (slot == -1) {
				// there are no free slots available, so we need to enqueue.
				this.pendingLoads.add(loadedBuffer);
				return true;
			}

			this.assign(loadedBuffer, slot);

			return true;

		}

		/**
		 * called when a pending load is assigned a slot.
		 *
		 * @param buffer
		 *            The pending buffer that is being assigned a lot.
		 * @param slot
		 *            The slot to place the buffer in. Must be empty (null).
		 */

		private void assign(BufferPageLoad buffer, int slot) {

			// target slot must be empty.
			Preconditions.checkState(this.buffers[slot] == null);

			// a free slot is available, so assign and load.
			this.buffers[slot] = buffer;

			// create the slice of the buffer.
			final ByteBuf page = this.buffer.retainedSlice(slot * 8192, 8192).clear();

			// perform a load request.
			buffer.load(DefaultBufferManager.this.storage, page);

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

	private final BufferClockPool defaultPool = new BufferClockPool(8, 8192 * 8);

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
	public void load(long objid, long pageno, PageConsumer consumer) {

		final PageId pageId = new PageId(objid, PageFork.Main, pageno);

		if (!this.defaultPool.dispatch(pageId, consumer)) {
			throw new IllegalStateException("Load rejected, too much work.");
		}

	}

	/**
	 * Allocate and initialise a new page in the given object. The page will be
	 * persisted as any normal page would.
	 *
	 * @param objid
	 *            The object ID to append a new page to.
	 *
	 * @param fork
	 *            The {@link PageFork} to allocate a page in.
	 *
	 * @param consumer
	 *            The handler that is called when the page has been allocated.
	 *
	 */

	public void append(long objid, PageFork fork, PageConsumer consumer) {

		final PageId pageId = new PageId(objid, PageFork.Main, -1);

		if (!this.defaultPool.dispatch(pageId, consumer)) {
			throw new IllegalStateException("Append rejected, too much work.");
		}

	}

	/**
	 *
	 */

	public static DefaultBufferManager create(Path base) {
		return new DefaultBufferManager(DiskPageAccessService.create(base));
	}

}

package io.ewok.linux.io;

import com.google.common.base.Preconditions;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import io.ewok.linux.JLinux;
import io.ewok.linux.ProcFS;
import io.netty.buffer.ByteBuf;

/**
 * Linux kernel async io api wrapper.
 *
 * @author theo
 *
 */

public class AsyncDiskContext implements AutoCloseable {

	/**
	 * The default size of the queue.
	 */

	public static final int DEFAULT_QUEUE_DEPTH = 128;

	/**
	 * The context, 0 if not open.
	 */

	private long ctx = 0;

	/**
	 * The number of queued events in this context.
	 */

	private final int nr_events;

	/**
	 * the allocated memory used for the control blocks. this will be enough to
	 * store all of the control blocks that could be queued.
	 */

	private final Memory memory;

	/**
	 *
	 */

	private final AsyncControlBlock[] iocbs;

	/**
	 *
	 */

	private AsyncControlBlock freehead = null;
	private AsyncControlBlock freetail = null;

	/**
	 *
	 */

	private final Memory mem;
	private final Memory inq;

	private int inqpos = 0;

	private final Object[] results;

	/**
	 * allocate memory for the control blocks.
	 */

	private AsyncDiskContext(int nr_events) {

		Preconditions.checkArgument(nr_events > 0 && nr_events <= ProcFS.sys_fs_aiomaxnr());

		this.nr_events = nr_events;
		this.mem = new Memory(32 * nr_events);
		this.inq = new Memory(8 * nr_events);

		// todo: align on this?
		final int align = AsyncControlBlock.SIZE;

		this.memory = new Memory((nr_events * AsyncControlBlock.SIZE) + align).align(align);

		this.results = new Object[nr_events];

		this.iocbs = new AsyncControlBlock[nr_events];

		for (int i = 0; i < nr_events; ++i) {
			final AsyncControlBlock iocb = new AsyncControlBlock(i,
					this.memory.share(i * AsyncControlBlock.SIZE, AsyncControlBlock.SIZE));
			this.iocbs[i] = iocb;
			this.free(iocb);
		}

	}

	private AsyncControlBlock alloc() {
		Preconditions.checkState(this.freehead != null);
		final AsyncControlBlock iocb = this.freehead;
		if (this.freehead == this.freetail) {
			this.freehead = this.freetail = null;
		} else {
			this.freehead = iocb.next;
		}
		iocb.next = null;
		return iocb;

	}

	private void free(AsyncControlBlock iocb) {
		if (this.freehead == null) {
			this.freehead = iocb;
			this.freetail = iocb;
		} else {
			this.freetail.next = iocb;
			this.freetail = iocb;
		}
	}

	/**
	 * Open the IO context.
	 */

	private void setup() {
		Preconditions.checkState(this.ctx == 0L);
		this.memory.clear();
		this.ctx = JLinux.io_setup(this.nr_events);
	}

	/**
	 * close the context.
	 */

	@Override
	public void close() {
		JLinux.io_destroy(this.ctx);
	}

	/**
	 *
	 * @param nr_events
	 * @return
	 */

	public static AsyncDiskContext create(int nr_events) {
		final AsyncDiskContext io = new AsyncDiskContext(nr_events);
		io.setup();
		return io;
	}

	/**
	 * Create a new async io disk context with the default queue depth.
	 */

	public static AsyncDiskContext createDefault() {
		return create(DEFAULT_QUEUE_DEPTH);
	}

	/**
	 * Perform an async read for this file.
	 *
	 * @param io
	 * @param buf
	 * @param offset
	 * @param length
	 */

	public void read(BlockFileHandle fd, ByteBuf buf, long offset, long length, Object attachment) {

		// allocate a control block for this op
		final AsyncControlBlock iocb = this.alloc();

		// set up the control block.
		final Pointer ptr = iocb.pread(fd, buf.memoryAddress(), offset, length, attachment);

		// set the syscall pointer
		this.inq.setPointer((this.inqpos++ * 8), ptr);

		// flush if we have no space left, even if not requested
		if (this.inqpos == this.nr_events) {
			this.flush();
		}

	}

	public void flush() {
		if (this.inqpos > 0) {
			final int nr = this.inqpos;
			this.inqpos = 0;
			JLinux.io_submit(this.ctx, this.inq, nr);
		}
	}

	/**
	 * Performs a poll of the queue, retrieving any available io results.
	 */

	public int events(AsyncResult[] results) {

		final int nrevents = JLinux.io_getevents(this.ctx, 1, results.length, this.mem);

		// __u64 data; /* the data field from the iocb */
		// __u64 obj; /* what iocb this event came from */
		// __s64 res; /* result code for this event */
		// __s64 res2; /* secondary result */

		Pointer ptr = this.mem;

		for (int i = 0; i < nrevents; ++i) {

			final long slot = ptr.getLong(0);

			final AsyncControlBlock iocb = this.iocbs[(int) slot];

			results[i].result = ptr.getLong(16);
			results[i].result2 = ptr.getLong(24);
			results[i].attachment = iocb.clear();

			this.results[i] = iocb.clear();

			this.free(iocb);

			ptr = ptr.share(32);

		}

		if (nrevents < this.nr_events) {
			this.results[nrevents] = null;
		}

		return nrevents;

	}

}

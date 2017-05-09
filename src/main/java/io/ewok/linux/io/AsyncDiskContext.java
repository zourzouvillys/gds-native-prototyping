package io.ewok.linux.io;

import java.util.concurrent.Executor;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import io.ewok.io.BlockAccessCallback;
import io.ewok.io.BlockAccessService;
import io.ewok.io.PagePointer;
import io.ewok.io.ReadBlockFileHandle;
import io.ewok.io.WriteBlockFileHandle;
import io.ewok.linux.JLinux;
import io.ewok.linux.ProcFS;
import lombok.Getter;

/**
 * Linux kernel async io api wrapper.
 *
 * @author theo
 *
 */

public class AsyncDiskContext implements BlockAccessService {

	/**
	 * mmm, stats
	 */

	@Getter
	private final AsyncDiskStats stats = new AsyncDiskStats();

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

	private int outstanding = 0;

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

	@Override
	public <T> void read(ReadBlockFileHandle bfh, PagePointer buf, long offset, long length, BlockAccessCallback<T> cb,
			T attachment) {

		final LinuxBlockFileHandle fd = (LinuxBlockFileHandle) bfh;

		// allocate a control block for this op
		final AsyncControlBlock iocb = this.alloc();

		// set up the control block.
		final Pointer ptr = iocb.pread(fd, buf, offset, length, cb, attachment);

		// set the pointer on the inq
		this.inq.setPointer((this.inqpos++ * 8), ptr);

		this.stats.read_ops.getAndIncrement();
		this.stats.buffered.getAndIncrement();

		// flush if we have no space left, even if not requested
		if (this.inqpos == this.nr_events) {
			this.flush();
		}

	}

	@Override
	public <T> void write(WriteBlockFileHandle bfh, PagePointer buf, long offset, long length,
			BlockAccessCallback<T> cb, T attachment) {

		final LinuxBlockFileHandle fd = (LinuxBlockFileHandle) bfh;

		// allocate a control block for this op
		final AsyncControlBlock iocb = this.alloc();

		// set up the control block.
		final Pointer ptr = iocb.pwrite(fd, buf, offset, length, cb, attachment);

		// set the syscall pointer
		this.inq.setPointer((this.inqpos++ * 8), ptr);

		this.stats.write_ops.getAndIncrement();
		this.stats.buffered.getAndIncrement();

		// flush if we have no space left, even if not requested
		if (this.inqpos == this.nr_events) {
			this.flush();
		}

	}

	@Override
	public void flush() {
		if (this.inqpos > 0) {
			final int nr = this.inqpos;
			this.inqpos = 0;
			this.stats.flushes.getAndIncrement();
			final int calc = JLinux.io_submit(this.ctx, this.inq, nr);
			this.outstanding += calc;
			this.stats.buffered.addAndGet(-calc);
			this.stats.pending.addAndGet(calc);
			if (calc != nr) {
				throw new RuntimeException(String.format("%d != %d", calc, nr));
			}
		}
	}

	/**
	 * Performs a poll of the queue, retrieving any available io results.
	 */

	@Override
	public int events(AsyncBlockResult[] results) {

		if (this.outstanding == 0) {
			return 0;
		}

		final int nrevents = JLinux.io_getevents(this.ctx, 1, Math.min(this.outstanding, results.length), this.mem);

		this.outstanding -= nrevents;
		this.stats.pending.addAndGet(-nrevents);

		// __u64 data; /* the data field from the iocb */
		// __u64 obj; /* what iocb this event came from */
		// __s64 res; /* result code for this event */
		// __s64 res2; /* secondary result */

		Pointer ptr = this.mem;

		for (int i = 0; i < nrevents; ++i) {

			final long slot = ptr.getLong(0);

			final AsyncControlBlock iocb = this.iocbs[(int) slot];

			switch (iocb.op) {
				case JLinux.IOCB_CMD_PREAD:
					this.stats.read_bytes.addAndGet(iocb.bytes);
					break;
				case JLinux.IOCB_CMD_PWRITE:
					this.stats.write_bytes.addAndGet(iocb.bytes);
					break;
			}

			results[i].result = ptr.getLong(16);
			results[i].result2 = ptr.getLong(24);
			results[i].callback = iocb.callback;
			results[i].page = iocb.page;
			results[i].attachment = iocb.data;

			if (results[i].result < 0) {
				this.stats.errors.getAndIncrement();
			} else {
				this.stats.success.getAndIncrement();
			}

			this.free(iocb);

			ptr = ptr.share(32);

		}

		if (nrevents < this.nr_events) {
			this.results[nrevents] = null;
		}

		return nrevents;

	}

	public int outstanding() {
		return this.outstanding;
	}

	public int dispatch(AsyncBlockResult[] res) {
		return this.dispatch(res, MoreExecutors.directExecutor());
	}

	/**
	 * Polls for events, and dispatches any on the given executor.
	 *
	 * @param res
	 *            An array of result slots. Must not be reused until the
	 *            executor has finished dispatching any work.
	 *
	 * @param executor
	 *            The executor to dispatch on.
	 *
	 * @return
	 */

	public int dispatch(AsyncBlockResult[] res, Executor executor) {
		final int nr = this.events(res);
		for (int i = 0; i < nr; ++i) {
			executor.execute(res[i]);
		}
		return nr;
	}

	/**
	 *
	 */

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("ioctx[ ");
		sb.append(this.stats);
		sb.append(" ]");
		return sb.toString();
	}

}

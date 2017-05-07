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
	 * The available control blocks.
	 */

	private final AsyncControlBlock[] cbs;

	/**
	 *
	 */

	private int nextSlot = 0;

	/**
	 * allocate memory for the control blocks.
	 */

	private AsyncDiskContext(int nr_events) {
		Preconditions.checkArgument(nr_events > 0 && nr_events <= ProcFS.sys_fs_aiomaxnr());
		this.nr_events = nr_events;
		this.cbs = new AsyncControlBlock[nr_events];

		// todo: align on this?
		final int align = AsyncControlBlock.SIZE;

		this.memory = new Memory((nr_events * AsyncControlBlock.SIZE) + align).align(align);

		for (int i = 0; i < nr_events; ++i) {
			this.cbs[i] = new AsyncControlBlock(this.memory.share(i * AsyncControlBlock.SIZE, AsyncControlBlock.SIZE));
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

	public void read(BlockFileHandle fd, ByteBuf buf, long offset, long length) {

		// set up the control block.
		final Pointer iocb = this.cbs[this.nextSlot++].pread(fd, new Pointer(buf.memoryAddress()), offset, length);

		// submit the iocb
		JLinux.io_submit(this.ctx, iocb);

		// return

	}

	public void events() {

		JLinux.io_getevents(this.ctx, 0, 6);

	}

}

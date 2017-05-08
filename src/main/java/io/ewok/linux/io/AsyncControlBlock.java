package io.ewok.linux.io;

import java.util.Objects;

import com.google.common.base.Preconditions;
import com.sun.jna.Pointer;

import io.ewok.io.BlockAccessCallback;
import io.ewok.io.PagePointer;
import io.ewok.linux.JLinux;
import lombok.Getter;

/**
 * An async io control block java interface.
 *
 * @author theo
 *
 */

public class AsyncControlBlock {

	// number of bytes in an iocb
	public static final int SIZE = 64;

	private final Pointer ipcbp;
	volatile AsyncControlBlock next = null;

	@Getter
	private final int slot;

	PagePointer page;
	BlockAccessCallback<?> callback;
	Object data;
	byte op;
	long bytes;



	public AsyncControlBlock(int slot, Pointer ptr) {
		this.slot = slot;
		this.ipcbp = Objects.requireNonNull(ptr);
		this.ipcbp.clear(SIZE);
	}

	public Pointer pread(LinuxBlockFileHandle fd, PagePointer page, long offset, long nbytes,
			BlockAccessCallback<?> callback, Object data) {

		Objects.requireNonNull(page);
		Objects.requireNonNull(callback);
		Preconditions.checkArgument(fd.fd >= 0);

		// the data associated
		this.callback = callback;
		this.data = data;
		this.page = page;
		this.op = JLinux.IOCB_CMD_PREAD;
		this.bytes = nbytes;

		// __u64 aio_data; /* data to be returned in event's data */
		this.ipcbp.setLong(0, this.slot);

		/* the kernel sets aio_key to the req # */
		this.ipcbp.setInt(8, 0);

		// reserved1
		this.ipcbp.setInt(12, 0);

		/* common fields */
		// __u16 aio_lio_opcode; /* see IOCB_CMD_ above */
		this.ipcbp.setShort(16, JLinux.IOCB_CMD_PREAD);

		// __s16 aio_reqprio;
		this.ipcbp.setShort(18, (short) 0);

		// __u32 aio_fildes;
		this.ipcbp.setInt(20, fd.fd);

		// __u64 aio_buf;
		this.ipcbp.setLong(24, page.memoryAddress());

		// __u64 aio_nbytes;
		this.ipcbp.setLong(32, nbytes);

		// __s64 aio_offset;
		this.ipcbp.setLong(40, offset);

		/* extra parameters */
		// __u64 aio_reserved2; /* TODO: use this for a (struct sigevent *) */
		this.ipcbp.setLong(48, 0);

		/* flags for the "struct iocb" */
		// __u32 aio_flags;
		// this.ipcbp.setInt(56, JLinux.IOCB_FLAG_RESFD);
		this.ipcbp.setInt(56, 0);
		/*
		 * if the IOCB_FLAG_RESFD flag of "aio_flags" is set, this is an eventfd
		 * to signal AIO readiness to
		 */

		// __u32 aio_resfd;
		// this.ipcbp.setInt(60, eventfd);
		this.ipcbp.setInt(60, 0);

		return this.ipcbp;

	}

	public void clear() {
		this.data = null;
		this.callback = null;
		this.page = null;
	}

	public Pointer pwrite(LinuxBlockFileHandle fd, PagePointer page, long offset, long nbytes,
			BlockAccessCallback<?> cb, Object attachment) {

		Preconditions.checkArgument(fd.fd >= 0);

		// the data associated
		this.page = page;
		this.callback = cb;
		this.data = attachment;
		this.op = JLinux.IOCB_CMD_PWRITE;
		this.bytes = nbytes;

		// __u64 aio_data; /* data to be returned in event's data */
		this.ipcbp.setLong(0, this.slot);

		/* the kernel sets aio_key to the req # */
		this.ipcbp.setInt(8, 0);

		// reserved1
		this.ipcbp.setInt(12, 0);

		/* common fields */
		// __u16 aio_lio_opcode; /* see IOCB_CMD_ above */
		this.ipcbp.setShort(16, JLinux.IOCB_CMD_PWRITE);

		// __s16 aio_reqprio;
		this.ipcbp.setShort(18, (short) 0);

		// __u32 aio_fildes;
		this.ipcbp.setInt(20, fd.fd);

		// __u64 aio_buf;
		this.ipcbp.setLong(24, page.memoryAddress());

		// __u64 aio_nbytes;
		this.ipcbp.setLong(32, nbytes);

		// __s64 aio_offset;
		this.ipcbp.setLong(40, offset);

		/* extra parameters */
		// __u64 aio_reserved2; /* TODO: use this for a (struct sigevent *) */
		this.ipcbp.setLong(48, 0);

		/* flags for the "struct iocb" */
		// __u32 aio_flags;
		// this.ipcbp.setInt(56, JLinux.IOCB_FLAG_RESFD);
		this.ipcbp.setInt(56, 0);
		/*
		 * if the IOCB_FLAG_RESFD flag of "aio_flags" is set, this is an eventfd
		 * to signal AIO readiness to
		 */

		// __u32 aio_resfd;
		// this.ipcbp.setInt(60, eventfd);
		this.ipcbp.setInt(60, 0);

		return this.ipcbp;

	}

}

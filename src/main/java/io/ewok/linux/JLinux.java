package io.ewok.linux;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Native linux API syscalls.
 *
 * @author theo
 *
 */

public final class JLinux {

	/*
	 * misc constants
	 */

	public static final int F_GETFL = 3;
	public static final int F_SETFL = 4;

	public static final int O_NONBLOCK = 0x2000;

	/**
	 * uname.
	 */

	public static final UTSName uname() {
		final Memory mem = new Memory(65 * 5);
		if (NativeLinux.libc.syscall(SYSCALL64.uname, mem) != 0) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}
		return new UTSName(mem);
	}

	public static final int EFD_SEMAPHORE = 00000001;
	public static final int EFD_NONBLOCK = 00004000;
	public static final int EFD_CLOEXEC = 02000000;

	/**
	 * create new eventfd
	 */

	public static int eventfd(int initval, int flags) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.eventfd2, initval, flags);
		if (res < 0) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}
		return (int) res;
	}

	/**
	 * close a file descriptor
	 */

	public static void close(int fd) {
		if (NativeLinux.libc.syscall(SYSCALL64.close, fd) != 0) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}
	}

	/**
	 * fcntl without arg
	 *
	 * @param fd
	 * @param cmd
	 * @return
	 */

	public static int fcntl(int fd, int cmd) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.fcntl, fd, cmd);
		if (res == -1) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}
		return (int) res;
	}

	/**
	 * fcntl with arg
	 *
	 * @param fd
	 * @param cmd
	 * @param arg
	 * @return
	 */

	public static int fcntl(int fd, int cmd, int arg) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.fcntl, fd, cmd, arg);
		if (res == -1) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}
		return (int) res;
	}

	/**
	 * create new epoll socket.
	 */

	public static int epoll_create(int flags) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.epoll_create1, flags);
		if (res < 0) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}
		return (int) res;
	}

	public static final int EPOLL_CTL_ADD = 1;
	public static final int EPOLL_CTL_MOD = 2;
	public static final int EPOLL_CTL_DEL = 3;

	public static final int EPOLLIN = 0x001;
	public static final int EPOLLPRI = 0x002;
	public static final int EPOLLOUT = 0x004;
	public static final int EPOLLERR = 0x008;
	public static final int EPOLLHUP = 0x010;

	public static final int EPOLLRDNORM = 0x040;
	public static final int EPOLLRDBAND = 0x080;
	public static final int EPOLLWRNORM = 0x100;
	public static final int EPOLLWRBAND = 0x200;

	public static final int EPOLLMSG = 0x400;

	public static final int EPOLLET = (1 << 31);
	public static final int EPOLLONESHOT = (1 << 30);
	public static final int EPOLLWAKEUP = (1 << 29);
	public static final int EPOLLEXCLUSIVE = (1 << 28);

	/**
	 * control epoll
	 */

	public static void epoll_ctl_add(int epollfd, int fd, int events, long data) {
		final Memory eventp = new Memory(12);
		eventp.setInt(0, events);
		eventp.setLong(4, data);
		final long res = NativeLinux.libc.syscall(SYSCALL64.epoll_ctl, epollfd, EPOLL_CTL_ADD, fd, eventp);
		if (res < 0) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}
	}

	public static void epoll_ctl_mod(int epollfd, int fd, int events, long data) {
		final Memory eventp = new Memory(12);
		eventp.setInt(0, events);
		eventp.setLong(4, data);
		final long res = NativeLinux.libc.syscall(SYSCALL64.epoll_ctl, epollfd, EPOLL_CTL_MOD, fd, eventp);
		if (res < 0) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}
	}

	public static void epoll_ctl_del(int epollfd, int fd) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.epoll_ctl, epollfd, EPOLL_CTL_DEL, fd, null);
		if (res < 0) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}
	}

	/**
	 * wait for epoll activity
	 *
	 * @return
	 */

	public static int epoll_wait(int epollfd, int maxevents, int timeout) {
		final Memory eventsp = new Memory(12 * maxevents);
		try {
			final long res = NativeLinux.libc.syscall(SYSCALL64.epoll_wait, epollfd, eventsp, maxevents, timeout);
			if (res < 0) {
				throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
			}
			return (int) res;
		} finally {
		}
	}

	/**
	 * create an async io context
	 *
	 * @param num
	 * @return
	 */

	public static final long io_setup(int num) {
		final LongByReference ref = new LongByReference();
		if (NativeLinux.libc.syscall(SYSCALL64.io_setup, num, ref) != 0) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}
		return ref.getValue();
	}

	/**
	 * destroy an async io context.
	 *
	 * @param ctx
	 */

	public static final void io_destroy(long ctx) {
		if (NativeLinux.libc.syscall(SYSCALL64.io_destroy, ctx) != 0) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}
	}

	public static final byte IOCB_CMD_PREAD = 0;
	public static final byte IOCB_CMD_PWRITE = 1;
	public static final byte IOCB_CMD_FSYNC = 2;
	public static final byte IOCB_CMD_FDSYNC = 3;

	// These two are experimental.
	// IOCB_CMD_PREADX = 4;
	// IOCB_CMD_POLL = 5;

	public static final byte IOCB_CMD_NOOP = 6;
	public static final byte IOCB_CMD_PREADV = 7;
	public static final byte IOCB_CMD_PWRITEV = 8;

	// IOCB_FLAG_RESFD - Set if the "aio_resfd" member of the "struct iocb" is
	// valid.
	public static final int IOCB_FLAG_RESFD = (1 << 0);

	static Memory ipcbp[] = new Memory[2];

	/**
	 *
	 * @param ctx
	 * @param eventfd
	 * @param cb
	 */

	public static final void io_submit(long ctx, int eventfd, IOCB... cb) {

		final Memory ipcbp = new Memory(8192).align(4096);

		// DirectIoLib.posix_memalign(pointerToPointer, blockSize, new
		// NativeLong(capacity));

		ipcbp.clear();

		// __u64 aio_data; /* data to be returned in event's data */
		ipcbp.setLong(0, 0);
		/* the kernel sets aio_key to the req # */
		ipcbp.setInt(8, 0);
		// reserved1
		ipcbp.setInt(12, 0);

		/* these are internal to the kernel/libc. */

		/* common fields */
		// __u16 aio_lio_opcode; /* see IOCB_CMD_ above */
		ipcbp.setShort(16, IOCB_CMD_PREAD);

		// __s16 aio_reqprio;
		ipcbp.setShort(18, (short) 0);

		// __u32 aio_fildes;
		ipcbp.setInt(20, cb[0].getFd());

		// __u64 aio_buf;
		final Memory val = new Memory(8192).align(4096);
		val.setString(0, "Hello");
		ipcbp.setPointer(24, val);

		// __u64 aio_nbytes;
		ipcbp.setLong(32, 512);

		// __s64 aio_offset;
		ipcbp.setLong(40, 0);

		/* extra parameters */
		// __u64 aio_reserved2; /* TODO: use this for a (struct sigevent *) */
		ipcbp.setLong(48, 0);

		/* flags for the "struct iocb" */
		// __u32 aio_flags;
		ipcbp.setInt(56, IOCB_FLAG_RESFD);

		/*
		 * if the IOCB_FLAG_RESFD flag of "aio_flags" is set, this is an eventfd
		 * to signal AIO readiness to
		 */

		// __u32 aio_resfd;
		ipcbp.setInt(60, eventfd);

		// final PointerByReference ipcbpp = new PointerByReference(ipcbp);

		final long res = NativeLinux.libc.syscall(SYSCALL64.io_submit, ctx, 1L, new PointerByReference(ipcbp));

		if (res != 1) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}

		JLinux.ipcbp[0] = ipcbp;

	}

	/**
	 * fetch a given number of events from an async io context.
	 *
	 * @param ioctx
	 * @param min_nr
	 * @param max_nr
	 * @return
	 */

	public static final int io_getevents(long ioctx, long min_nr, long max_nr) {

		final Memory eventsp = new Memory(128);

		final long res = NativeLinux.libc.syscall(SYSCALL64.io_getevents, ioctx, min_nr, max_nr, eventsp, null);

		if (res < 0) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}

		// __u64 data; /* the data field from the iocb */
		// __u64 obj; /* what iocb this event came from */
		// __s64 res; /* result code for this event */
		// __s64 res2; /* secondary result */

		System.err.println("DATA: " + eventsp.getLong(0));
		System.err.println("IOCBP: " + eventsp.getLong(8));
		System.err.println("RES: " + eventsp.getLong(16));
		System.err.println("RES2: " + eventsp.getLong(24));

		return (int) res;

		// throw new RuntimeException();
	}

	/**
	 * cancel an outstanding async io call.
	 *
	 * @param ioctx
	 * @param iocb
	 */

	public static final void io_cancel(long ioctx, IOCB iocb) {
		final Memory iocbp = null;
		final Memory result = null;
		if (NativeLinux.libc.syscall(SYSCALL64.io_cancel, ioctx, iocbp, result) == -1) {
			throw new RuntimeException(NativeLinux.libc.strerror(Native.getLastError()));
		}
		throw new RuntimeException();
	}

}

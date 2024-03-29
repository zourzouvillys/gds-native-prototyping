package io.ewok.linux;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Native linux API syscalls.
 *
 * @author theo
 *
 */

@Slf4j
public final class JLinux {

	/**
	 * file types
	 */

	// bit mask for the file type bit field
	public static final int S_IFMT = 0170000;
	// socket
	public static final int S_IFSOCK = 0140000;
	// symbolic link
	public static final int S_IFLNK = 0120000;
	// regular file
	public static final int S_IFREG = 0100000;
	// block device
	public static final int S_IFBLK = 0060000;
	// directory
	public static final int S_IFDIR = 0040000;
	// character device
	public static final int S_IFCHR = 0020000;
	// FIFO
	public static final int S_IFIFO = 0010000;

	/**
	 * file modes
	 */

	// user (file owner) has read, write, and execute permission
	public static final int S_IRWXU = 00700;
	// user has read permission
	public static final int S_IRUSR = 00400;
	// user has write permission
	public static final int S_IWUSR = 00200;
	// user has execute permission
	public static final int S_IXUSR = 00100;
	// group has read, write, and execute permission
	public static final int S_IRWXG = 00070;
	// group has read permission
	public static final int S_IRGRP = 00040;
	// group has write permission
	public static final int S_IWGRP = 00020;
	// group has execute permission
	public static final int S_IXGRP = 00010;
	// others have read, write, and execute permission
	public static final int S_IRWXO = 00007;
	// others have read permission
	public static final int S_IROTH = 00004;
	// others have write permission
	public static final int S_IWOTH = 00002;
	// others have execute permission
	public static final int S_IXOTH = 00001;
	// set-user-ID bit
	public static final int S_ISUID = 0004000;
	// set-group-ID bit (see inode(7)).
	public static final int S_ISGID = 0002000;
	// sticky bit (see inode(7)).
	public static final int S_ISVTX = 0001000;

	/*
	 * misc constants
	 */

	public static final int F_GETFL = 3;
	public static final int F_SETFL = 4;

	/**
	 *
	 */

	public static final int O_RDONLY = 00000000;
	public static final int O_WRONLY = 00000001;
	public static final int O_RDWR = 00000002;
	public static final int O_ACCMODE = 00000003;

	/**
	 * fcntl
	 */

	public static final int O_ASYNC = 00020000;

	/**
	 *
	 */

	public static final int O_APPEND = 00002000;
	public static final int O_CLOEXEC = 02000000;
	public static final int O_CREAT = 00000100;
	public static final int O_DIRECT = 00040000;
	public static final int O_DIRECTORY = 00200000;
	public static final int O_DSYNC = 00010000;
	public static final int O_EXCL = 00000200;
	public static final int O_LARGEFILE = 00100000;
	public static final int O_NOATIME = 01000000;
	public static final int O_NOCTTY = 00000400;
	public static final int O_NOFOLLOW = 00400000;
	public static final int O_NONBLOCK = 00004000;
	public static final int O_PATH = 010000000;
	public static final int O_SYNC = 04000000 | O_DSYNC;
	public static final int O_TMPFILE = 020000000 | O_DIRECTORY;
	public static final int O_TRUNC = 00001000;

	/**
	 * open() a file.
	 *
	 * @param path
	 * @param flags
	 * @param mode
	 * @return
	 */

	public static int open(Path path, int flags, int mode) {
		Objects.requireNonNull(path, "path");
		final long fd = NativeLinux.libc.syscall(SYSCALL64.open, path.toString(), flags, mode);
		if (fd == -1) {
			throw LinuxErrorException.capture("open", path.toString(), flags, mode);
		}
		return (int) fd;
	}

	public static int openat(int dir_fd, Path pathname, int flags, int mode) {
		final long fd = NativeLinux.libc.syscall(SYSCALL64.openat, dir_fd,
				(pathname != null) ? pathname.toString() : "", flags, mode);
		if (fd == -1) {
			throw LinuxErrorException.capture("openat", dir_fd, pathname, flags, mode);
		}
		return (int) fd;
	}

	/**
	 *
	 */

	public static final int AT_FDCWD = -100;

	public static final int AT_EMPTY_PATH = 0x1000;
	public static final int AT_SYMLINK_FOLLOW = 0x400;

	/**
	 *
	 */

	private static long syscall(int sysno, Object... args) {
		return NativeLinux.libc.syscall(sysno, args);
	}

	/**
	 *
	 * @param oldfd
	 * @param oldpath
	 * @param newdir
	 * @param newpath
	 * @param flags
	 */

	public static void linkat(int oldfd, Path oldpath, int newdir, Path newpath, int flags) {

		final long fd = syscall(
				SYSCALL64.linkat,
				oldfd,
				(oldpath != null) ? oldpath.toString() : "",
						newdir,
						(newpath != null) ? newpath.toString() : "",
								flags);

		if (fd == -1) {
			throw LinuxErrorException.capture("linkat", oldfd, oldpath, newdir, newpath, flags);
		}

	}

	public static void linkat(Path oldpath, Path newpath, int flags) {
		linkat(AT_FDCWD, oldpath, AT_FDCWD, newpath, flags);
	}

	/**
	 * remove file at the given path.
	 */

	public static void unlink(Path path) {
		if (syscall(SYSCALL64.unlink, path.toString()) != 0) {
			throw LinuxErrorException.capture("unlink");
		}
	}

	/**
	 *
	 * @param dir_fd
	 * @param path
	 * @param flags
	 */

	public static void unlinkat(int dir_fd, Path path, int flags) {
		if (syscall(SYSCALL64.unlinkat, dir_fd, path.toString(), flags) != 0) {
			throw LinuxErrorException.capture("unlinkat");
		}
	}

	/**
	 *
	 */

	public static String readlink(Path path) {

		final byte[] buffer = new byte[4096];

		final int len = (int) syscall(SYSCALL64.readlink, path.toString(), buffer, buffer.length);

		if (len != 0) {
			throw LinuxErrorException.capture("readlink", path.toString());
		}

		return new String(buffer, 0, len, StandardCharsets.UTF_8);

	}

	/**
	 *
	 */

	public static void ioctl() {
		throw new RuntimeException("not implemented");
	}

	/**
	 *
	 * @param dirfd
	 * @param pathname
	 * @param flags
	 * @param mask
	 */

	public static void statx(int dirfd, Path pathname, int flags, int mask) {
		final Memory ptr = new Memory(118);
		final long fd = NativeLinux.libc.syscall(SYSCALL64.statx, dirfd, pathname.toString(), flags, mask, ptr);
		if (fd == -1) {
			throw LinuxErrorException.capture("statx");
		}
		throw new RuntimeException("not implemented");
	}

	/**
	 *
	 */

	public static StatFS statfs(Path file) {
		final Memory ptr = new Memory(88 + 32);
		final long fd = NativeLinux.libc.syscall(SYSCALL64.stat, file.toString(), ptr);
		if (fd == -1) {
			throw LinuxErrorException.capture("stat");
		}
		return new StatFS(ptr);
	}

	/**
	 * Stats the file
	 *
	 * @param file
	 * @return
	 */

	@SneakyThrows
	public static LinuxStat stat(Path file) {

		final Memory ptr = new Memory(144);

		ptr.clear();

		final long res = NativeLinux.libc.syscall(SYSCALL64.stat, file.toString(), ptr);

		if (res == -1) {
			throw LinuxErrorException.capture("stat");
		}

		return new LinuxStat(ptr);

	}

	@SneakyThrows
	public static LinuxStat fstat(int fd) {

		final Memory ptr = new Memory(144);

		ptr.clear();

		final long res = NativeLinux.libc.syscall(SYSCALL64.fstat, fd, ptr);

		if (res == -1) {
			throw LinuxErrorException.capture("fstat");
		}

		return new LinuxStat(ptr);

	}

	/**
	 * The system pagesize
	 */

	public static int pagesize() {
		return NativeLinux.libc.getpagesize();
	}

	public static long sysconf(int name) {
		return NativeLinux.libc.sysconf(name);
	}

	public static long fpathconf(int fd, int name) {
		return NativeLinux.libc.fpathconf(fd, name);
	}

	public static long pathconf(Path path, int name) {
		return NativeLinux.libc.pathconf(path, name);
	}

	/*
	 *
	 */

	public static final int PROT_READ = 0x1;
	public static final int PROT_WRITE = 0x2;
	public static final int PROT_EXEC = 0x4;
	public static final int PROT_SEM = 0x8;
	public static final int PROT_NONE = 0x0;

	//

	public static final int MAP_SHARED = 0x01;
	public static final int MAP_PRIVATE = 0x02;

	//

	public static final int MAP_FIXED = 0x10;
	public static final int MAP_ANONYMOUS = 0x20;
	public static final int MAP_UNINITIALIZED = 0x4000000;

	//

	public static final int MAP_FILE = 16;

	public static final int MAP_GROWSDOWN = 0x0100;
	public static final int MAP_DENYWRITE = 0x0800;
	public static final int MAP_EXECUTABLE = 0x1000;
	public static final int MAP_LOCKED = 0x2000;
	public static final int MAP_NORESERVE = 0x4000;
	public static final int MAP_POPULATE = 0x8000;
	public static final int MAP_NONBLOCK = 0x10000;
	public static final int MAP_STACK = 0x20000;
	public static final int MAP_HUGETLB = 0x40000;

	//

	// public static final int MAP_32BIT = 0;
	// public static final int MAP_HUGE_2MB = 0;
	// public static final int MAP_HUGE_1GB = 0;

	/*
	 *
	 */

	public static final int MS_ASYNC = 1;
	public static final int MS_INVALIDATE = 2;
	public static final int MS_SYNC = 4;

	/*
	 *
	 */

	public static final int MCL_CURRENT = 1;
	public static final int MCL_FUTURE = 2;
	public static final int MCL_ONFAULT = 4;

	/*
	 *
	 */

	public static final int MADV_NORMAL = 0;
	public static final int MADV_RANDOM = 1;
	public static final int MADV_SEQUENTIAL = 2;
	public static final int MADV_WILLNEED = 3;
	public static final int MADV_DONTNEED = 4;

	//

	public static final int MADV_FREE = 8;
	public static final int MADV_REMOVE = 9;
	public static final int MADV_DONTFORK = 10;
	public static final int MADV_DOFORK = 11;
	public static final int MADV_HWPOISON = 100;
	public static final int MADV_SOFT_OFFLINE = 101;

	public static final int MADV_MERGEABLE = 12;
	public static final int MADV_UNMERGEABLE = 13;

	public static final int MADV_HUGEPAGE = 14;
	public static final int MADV_NOHUGEPAGE = 15;

	public static final int MADV_DONTDUMP = 16;
	public static final int MADV_DODUMP = 16;

	/*
	 * When MAP_HUGETLB is set bits [26:31] encode the log2 of the huge page
	 * size. This gives us 6 bits, which is enough until someone invents 128 bit
	 * address spaces.
	 *
	 * Assume these are all power of twos. When 0 use the default page size.
	 */

	public static final int MAP_HUGE_SHIFT = 26;
	public static final int MAP_HUGE_MASK = 0x3f;

	/**
	 *
	 * @param addr
	 * @param length
	 * @param prot
	 * @param flags
	 * @param fd
	 * @param offset
	 * @return
	 */

	public static long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.mmap, addr, length, prot, flags, fd, offset);
		if (res < 0) {
			throw LinuxErrorException.capture("mmap", addr, length, prot, flags, fd, offset);
		}
		return res;
	}

	/**
	 *
	 * @param addr
	 * @param length
	 * @param flags
	 * @return
	 */

	public static long msync(long addr, long length, int flags) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.msync, addr, length, flags);
		if (res != 0) {
			throw LinuxErrorException.capture("msync", addr, length);
		}
		return res;
	}

	/**
	 *
	 * @param addr
	 * @param length
	 * @param flags
	 * @return
	 */

	public static long mremap(long old_address, long old_size, long new_size, int flags) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.mremap, old_address, old_size, new_size, flags);
		if (res != 0) {
			throw LinuxErrorException.capture("mremap", old_address, old_size, new_size, flags);
		}
		return res;
	}

	/**
	 *
	 * @param addr
	 * @param length
	 * @param flags
	 * @return
	 */

	public static long mlock(long address, long len, int flags) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.mlock2, address, len, flags);
		if (res != 0) {
			throw LinuxErrorException.capture("mlock2", address, len, flags);
		}
		return res;
	}

	/**
	 *
	 * @param addr
	 * @param length
	 * @param flags
	 * @return
	 */

	public static long madvise(long address, long len, int advice) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.madvise, address, len, advice);
		if (res != 0) {
			throw LinuxErrorException.capture("mlock2", address, len, advice);
		}
		return res;
	}

	/*
	 *
	 */

	public static final int MPOL_F_STATIC_NODES = 0;
	public static final int MPOL_F_RELATIVE_NODES = 0;

	public static final int MPOL_F_MEMS_ALLOWED = 0;
	public static final int MPOL_F_ADDR = 0;
	public static final int MPOL_F_NODE = 0;

	public static final int MPOL_DEFAULT = 0;
	public static final int MPOL_BIND = 0;
	public static final int MPOL_INTERLEAVE = 0;
	public static final int MPOL_PREFERRED = 0;
	public static final int MPOL_LOCAL = 0;

	public static final int MPOL_MF_STRICT = 0;
	public static final int MPOL_MF_MOVE = 0;
	public static final int MPOL_MF_MOVE_ALL = 0;

	/**
	 *
	 * @param addr
	 * @param length
	 * @param flags
	 * @return
	 */

	public static long mbind(long addr, long len, int mode, long nodemask, long maxnode, int flags) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.mbind, addr, len, mode, nodemask, maxnode, flags);
		if (res != 0) {
			throw LinuxErrorException.capture("mbind", addr, len, mode, nodemask, maxnode, flags);
		}
		return res;
	}

	/**
	 *
	 * @param addr
	 * @param length
	 * @return
	 */

	public static long munmap(long addr, long length) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.munmap, addr, length);
		if (res < 0) {
			throw LinuxErrorException.capture("munmap", addr, length);
		}
		return res;
	}

	/*
	 *
	 */

	/**
	 *
	 */

	public static void get_mempolicy(long maxnode, long addr, long flags) {
		final IntByReference modep = new IntByReference(0);
		final LongByReference nodemaskp = new LongByReference(0);
		if (NativeLinux.libc.syscall(SYSCALL64.get_mempolicy, modep, nodemaskp, maxnode, addr, flags) < 0) {
			throw LinuxErrorException.capture("get_mempolicy", modep, nodemaskp, maxnode, addr, flags);
		}
	}

	/**
	 *
	 */

	public static void set_mempolicy(int mode, long nodemask, long maxnode) {
		final LongByReference nodemaskp = new LongByReference(nodemask);
		if (NativeLinux.libc.syscall(SYSCALL64.set_mempolicy, mode, nodemaskp, maxnode) < 0) {
			throw LinuxErrorException.capture("set_mempolicy", mode, nodemaskp, maxnode);
		}
	}

	/**
	 *
	 */

	public static void migrate_pages() {
		NativeLinux.libc.syscall(SYSCALL64.migrate_pages);
	}

	public static void move_pages() {
		NativeLinux.libc.syscall(SYSCALL64.move_pages);
	}

	/*
	 *
	 */

	public void ioprio_get() {
		NativeLinux.libc.syscall(SYSCALL64.ioprio_get);
	}

	public void ioprio_set() {
		NativeLinux.libc.syscall(SYSCALL64.ioprio_set);
	}

	public void getpriority() {
		NativeLinux.libc.syscall(SYSCALL64.getpriority);
	}

	public void setpriority() {
		NativeLinux.libc.syscall(SYSCALL64.setpriority);
	}

	/**
	 * uname.
	 */

	public static final UTSName uname() {
		final Memory mem = new Memory(65 * 5);
		if (NativeLinux.libc.syscall(SYSCALL64.uname, mem) != 0) {
			throw LinuxErrorException.capture("uname");
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
			throw LinuxErrorException.capture("eventfd");
		}
		return (int) res;
	}

	/**
	 * close a file descriptor
	 */

	public static void close(int fd) {
		if (NativeLinux.libc.syscall(SYSCALL64.close, fd) != 0) {
			throw LinuxErrorException.capture("close");
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
			throw LinuxErrorException.capture("fcntl");
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
			throw LinuxErrorException.capture("fcntl");
		}
		return (int) res;
	}

	/**
	 *
	 */

	public static final int FALLOC_FL_KEEP_SIZE = 0x01;
	public static final int FALLOC_FL_PUNCH_HOLE = 0x02;
	public static final int FALLOC_FL_NO_HIDE_STALE = 0x04;

	public static final int FALLOC_FL_ZERO_RANGE = 0x10;
	public static final int FALLOC_FL_INSERT_RANGE = 0x20;
	public static final int FALLOC_FL_UNSHARE_RANGE = 0x40;

	/**
	 * fallocate
	 *
	 * @param fd
	 * @param cmd
	 * @param arg
	 * @return
	 */

	public static int fallocate(int fd, int mode, long offset, long length) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.fallocate, fd, mode, offset, length);
		if (res == -1) {
			throw LinuxErrorException.capture("fallocate");
		}
		return (int) res;
	}

	/**
	 *
	 * @param fd
	 * @param length
	 */

	public static void ftruncate(int fd, long length) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.ftruncate, fd, length);
		if (res == -1) {
			throw LinuxErrorException.capture("ftruncate");
		}
	}

	/*
	 *
	 */

	// The file offset is set to offset bytes.
	public static final int SEEK_SET = 0;

	// The file offset is set to its current location plus offset bytes.
	public static final int SEEK_CUR = 1;

	// The file offset is set to the size of the file plus offset bytes.
	public static final int SEEK_END = 2;

	//
	public static final int SEEK_DATA = 3;

	public static final int SEEK_HOLE = 4;

	/**
	 *
	 */

	public static int lseek(int fd, long offset, int whence) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.lseek, fd, offset, whence);
		if (res == -1) {
			throw LinuxErrorException.capture("lseek");
		}
		return (int) res;
	}

	/**
	 *
	 */

	public static long copy_file_range(int fd_in, long off_in, int fd_out, long off_out, long len, int flags) {
		final LongByReference off_inp = new LongByReference(off_in);
		final LongByReference off_outp = new LongByReference(off_out);
		final long res = NativeLinux.libc.syscall(SYSCALL64.copy_file_range, fd_in, off_inp, fd_out, off_outp, len,
				flags);
		if (res == -1) {
			throw LinuxErrorException.capture("file_copy_range");
		}
		return res;
	}

	/**
	 * create new epoll socket.
	 */

	public static int epoll_create(int flags) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.epoll_create1, flags);
		if (res < 0) {
			throw LinuxErrorException.capture("epoll_create");
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
			throw LinuxErrorException.capture("epoll_ctl");
		}
	}

	public static void epoll_ctl_mod(int epollfd, int fd, int events, long data) {
		final Memory eventp = new Memory(12);
		eventp.setInt(0, events);
		eventp.setLong(4, data);
		final long res = NativeLinux.libc.syscall(SYSCALL64.epoll_ctl, epollfd, EPOLL_CTL_MOD, fd, eventp);
		if (res < 0) {
			throw LinuxErrorException.capture("epoll_ctl");
		}
	}

	public static void epoll_ctl_del(int epollfd, int fd) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.epoll_ctl, epollfd, EPOLL_CTL_DEL, fd, null);
		if (res < 0) {
			throw LinuxErrorException.capture("epoll_ctl");
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
				throw LinuxErrorException.capture("epoll_wait");
			}
			return (int) res;
		} finally {
		}
	}

	/**
	 *
	 */

	public static final int SOCK_STREAM = 1;
	public static final int SOCK_DGRAM = 2;
	public static final int SOCK_RAW = 3;
	public static final int SOCK_RDM = 4;
	public static final int SOCK_SEQPACKET = 5;
	public static final int SOCK_PACKET = 10;

	//

	public static final int SOCK_NONBLOCK = O_NONBLOCK;

	/*
	 *
	 */

	public static final int AF_UNIX = 1;
	public static final int AF_INET = 2;
	public static final int AF_INET6 = 10;

	/**
	 *
	 * @param domain
	 * @param type
	 * @param protocol
	 */

	public static int[] socketpair(int domain, int type, int protocol) {
		final Memory sv = new Memory(8);
		if (NativeLinux.libc.syscall(SYSCALL64.socketpair, domain, type, protocol, sv) != 0) {
			throw LinuxErrorException.capture("socketpair");
		}
		return new int[] { sv.getInt(0), sv.getInt(4) };
	}

	/**
	 * create an async io context
	 *
	 * @param num
	 * @return
	 */

	public static final long io_setup(int num) {
		final LongByReference ref = new LongByReference();
		// must be initialized to 0
		ref.setValue(0);
		if (NativeLinux.libc.syscall(SYSCALL64.io_setup, num, ref) != 0) {
			throw LinuxErrorException.capture("io_setup");
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
			throw LinuxErrorException.capture("io_destroy");
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

	public static final void io_submit(long ctx, Pointer... iocbs) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.io_submit, ctx, 1L, new PointerByReference(iocbs[0]));
		if (res != 1) {
			throw LinuxErrorException.capture("io_submit");
		}
	}

	public static final int io_submit(long ctx, Pointer iocbs, int num) {
		final long res = NativeLinux.libc.syscall(SYSCALL64.io_submit, ctx, num, iocbs);
		if (res != num) {
			throw LinuxErrorException.capture("io_submit", ctx, iocbs, num);
		}
		return (int) res;
	}

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
			throw LinuxErrorException.capture("io_submit");
		}

		JLinux.ipcbp[0] = ipcbp;

	}

	/**
	 * fetch a given number of events from an async io context.
	 *
	 * @param ioctx
	 * @param min_nr
	 * @param mem
	 * @return
	 */

	public static final int io_getevents(long ioctx, long min_nr, long max_nr, Memory evp) {

		assert max_nr <= evp.size() / 32;

		final long res = NativeLinux.libc.syscall(SYSCALL64.io_getevents, ioctx, min_nr, max_nr, evp, null);

		if (res < 0) {
			throw LinuxErrorException.capture("io_getevents");
		}

		return (int) res;

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
			throw LinuxErrorException.capture("io_cancel");
		}
		throw new RuntimeException("not implemented");
	}

	/**
	 * @return
	 *
	 */

	public static long sendfile(int out_fd, int in_fd, long offset, long count) {
		final LongByReference offp = new LongByReference(offset);
		final long res = NativeLinux.libc.syscall(SYSCALL64.sendfile, out_fd, in_fd, offp, count);
		if (res == -1) {
			throw LinuxErrorException.capture("sendfile");
		}
		return res;
	}

	public static void tee(int fd_in, int fd_out, long len, int flags) {
		if (NativeLinux.libc.syscall(SYSCALL64.tee, fd_in, fd_out, len, flags) == -1) {
			throw LinuxErrorException.capture("tee");
		}
	}

	public static void splice(int fd_in, long off_in, int fd_out, long off_out, long len, int flags) {
		if (NativeLinux.libc.syscall(SYSCALL64.splice, fd_in) == -1) {
			throw LinuxErrorException.capture("splice");
		}
	}

	public static void vmsplice(int fd) {
		if (NativeLinux.libc.syscall(SYSCALL64.vmsplice, fd) == -1) {
			throw LinuxErrorException.capture("vmsplice");
		}
	}

	/**
	 *
	 */

	/* New cgroup namespace */
	public static final int CLONE_NEWCGROUP = 0x02000000;
	/* New ipc namespace */
	public static final int CLONE_NEWIPC = 0x08000000;
	/* New network namespace */
	public static final int CLONE_NEWNET = 0x40000000;
	/* New mount namespace group */
	public static final int CLONE_NEWNS = 0x00020000;
	/* New user namespace */
	public static final int CLONE_NEWUSER = 0x10000000;
	/* New utsname namespace */
	public static final int CLONE_NEWUTS = 0x04000000;
	/* New pid namespace */
	public static final int CLONE_NEWPID = 0x20000000;

	/**
	 *
	 */

	public static void setns(int fd, int nstype) {
		if (NativeLinux.libc.syscall(SYSCALL64.setns, fd, nstype) == -1) {
			throw LinuxErrorException.capture("setns");
		}
	}

	/**
	 *
	 */

	public static void sched_yield() {
		NativeLinux.libc.syscall(SYSCALL64.sched_yield);
	}

	/**
	 *
	 */

	public static SchedAttr sched_getattr(long pid, int flags) {
		final SchedAttr attrs = new SchedAttr();
		if (NativeLinux.libc.syscall(SYSCALL64.sched_getattr, pid, attrs, attrs.size(), flags) != 0) {
			throw LinuxErrorException.capture("sched_getattr");
		}
		return attrs;
	}

	/**
	 *
	 */

	public static void sched_setattr(long pid, SchedAttr attrs, int flags) {
		NativeLinux.libc.syscall(SYSCALL64.sched_setattr, pid, attrs, flags);
	}

	public void sched_getaffinity() {
		NativeLinux.libc.syscall(SYSCALL64.sched_getaffinity);
	}

	public void sched_setaffinity() {
		NativeLinux.libc.syscall(SYSCALL64.sched_setaffinity);
	}

	public void sched_getscheduler() {
		NativeLinux.libc.syscall(SYSCALL64.sched_getscheduler);
	}

	public void sched_setscheduler() {
		NativeLinux.libc.syscall(SYSCALL64.sched_setscheduler);
	}

	public void sched_get_priority_max() {
		NativeLinux.libc.syscall(SYSCALL64.sched_get_priority_max);
	}

	public void sched_get_priority_min() {
		NativeLinux.libc.syscall(SYSCALL64.sched_get_priority_min);
	}

	public void sched_getparam() {
		NativeLinux.libc.syscall(SYSCALL64.sched_getparam);
	}

	public void sched_setparam() {
		NativeLinux.libc.syscall(SYSCALL64.sched_setparam);
	}

	public void sched_rr_get_interval() {
		NativeLinux.libc.syscall(SYSCALL64.sched_rr_get_interval);
	}

	/**
	 *
	 */

	public static final int PERF_TYPE_HARDWARE = 0;

	public static final int PERF_COUNT_HW_INSTRUCTIONS = 1;

	/*
	 *perf_event_open(0x7f6f2c358bc0, 0, -1, -1, 0) = -1 ENOENT (No such file or directory)
	 */

	@SneakyThrows
	public static int perf_event_open(int pid, int cpu, int group_fd, long flags) {

		final Memory hw_event = new Memory(112);

		hw_event.clear();

		// type
		hw_event.setInt(0, 1 << 0);

		// size
		hw_event.setInt(4, (int) hw_event.size());

		// config
		hw_event.setInt(8, PERF_COUNT_HW_INSTRUCTIONS);

		// flags
		hw_event.setLong(40, 0);

		// period/freq
		hw_event.setLong(48, 10000);

		// sample_type
		hw_event.setLong(56, 1 << 16);

		// read_format
		hw_event.setLong(64, 1 << 2);

		// pe.disabled = 1;
		// pe.exclude_kernel = 1;
		// pe.exclude_hv = 1;
		//
		// fd = perf_event_open(&pe, 0, -1, -1, 0);
		// if (fd == -1) {
		// fprintf(stderr, "Error opening leader %llx\n", pe.config);
		// exit(EXIT_FAILURE);
		// }
		//
		// ioctl(fd, PERF_EVENT_IOC_RESET, 0);
		// ioctl(fd, PERF_EVENT_IOC_ENABLE, 0);

		flags = 1 << 0;

		final long fd = NativeLinux.libc.syscall(SYSCALL64.perf_event_open, hw_event, pid, cpu, group_fd, flags);

		if (fd == -1) {
			throw LinuxErrorException.capture("perf_event_open");

		}

		return (int) fd;

	}

}

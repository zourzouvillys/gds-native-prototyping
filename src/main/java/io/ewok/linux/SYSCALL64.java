package io.ewok.linux;

/**
 * List of linux syscall numbers for x86-64.
 *
 * Content is generated using:
 *
 * <pre>
 *  curl -s 'https://raw.githubusercontent.com/torvalds/linux/master/arch/x86/entry/syscalls/syscall_64.tbl' | grep -E '^([[:digit:]]+)\t(common|64)\t' | awk '{print "public static final int "$3" = "$1";"}' |
 * </pre>
 *
 * @author theo
 *
 */
public final class SYSCALL64 {

	public static final int read = 0;
	public static final int write = 1;
	public static final int open = 2;
	public static final int close = 3;
	public static final int stat = 4;
	public static final int fstat = 5;
	public static final int lstat = 6;
	public static final int poll = 7;
	public static final int lseek = 8;
	public static final int mmap = 9;
	public static final int mprotect = 10;
	public static final int munmap = 11;
	public static final int brk = 12;
	public static final int rt_sigaction = 13;
	public static final int rt_sigprocmask = 14;
	public static final int rt_sigreturn = 15;
	public static final int ioctl = 16;
	public static final int pread64 = 17;
	public static final int pwrite64 = 18;
	public static final int readv = 19;
	public static final int writev = 20;
	public static final int access = 21;
	public static final int pipe = 22;
	public static final int select = 23;
	public static final int sched_yield = 24;
	public static final int mremap = 25;
	public static final int msync = 26;
	public static final int mincore = 27;
	public static final int madvise = 28;
	public static final int shmget = 29;
	public static final int shmat = 30;
	public static final int shmctl = 31;
	public static final int dup = 32;
	public static final int dup2 = 33;
	public static final int pause = 34;
	public static final int nanosleep = 35;
	public static final int getitimer = 36;
	public static final int alarm = 37;
	public static final int setitimer = 38;
	public static final int getpid = 39;
	public static final int sendfile = 40;
	public static final int socket = 41;
	public static final int connect = 42;
	public static final int accept = 43;
	public static final int sendto = 44;
	public static final int recvfrom = 45;
	public static final int sendmsg = 46;
	public static final int recvmsg = 47;
	public static final int shutdown = 48;
	public static final int bind = 49;
	public static final int listen = 50;
	public static final int getsockname = 51;
	public static final int getpeername = 52;
	public static final int socketpair = 53;
	public static final int setsockopt = 54;
	public static final int getsockopt = 55;
	public static final int clone = 56;
	public static final int fork = 57;
	public static final int vfork = 58;
	public static final int execve = 59;
	public static final int exit = 60;
	public static final int wait4 = 61;
	public static final int kill = 62;
	public static final int uname = 63;
	public static final int semget = 64;
	public static final int semop = 65;
	public static final int semctl = 66;
	public static final int shmdt = 67;
	public static final int msgget = 68;
	public static final int msgsnd = 69;
	public static final int msgrcv = 70;
	public static final int msgctl = 71;
	public static final int fcntl = 72;
	public static final int flock = 73;
	public static final int fsync = 74;
	public static final int fdatasync = 75;
	public static final int truncate = 76;
	public static final int ftruncate = 77;
	public static final int getdents = 78;
	public static final int getcwd = 79;
	public static final int chdir = 80;
	public static final int fchdir = 81;
	public static final int rename = 82;
	public static final int mkdir = 83;
	public static final int rmdir = 84;
	public static final int creat = 85;
	public static final int link = 86;
	public static final int unlink = 87;
	public static final int symlink = 88;
	public static final int readlink = 89;
	public static final int chmod = 90;
	public static final int fchmod = 91;
	public static final int chown = 92;
	public static final int fchown = 93;
	public static final int lchown = 94;
	public static final int umask = 95;
	public static final int gettimeofday = 96;
	public static final int getrlimit = 97;
	public static final int getrusage = 98;
	public static final int sysinfo = 99;
	public static final int times = 100;
	public static final int ptrace = 101;
	public static final int getuid = 102;
	public static final int syslog = 103;
	public static final int getgid = 104;
	public static final int setuid = 105;
	public static final int setgid = 106;
	public static final int geteuid = 107;
	public static final int getegid = 108;
	public static final int setpgid = 109;
	public static final int getppid = 110;
	public static final int getpgrp = 111;
	public static final int setsid = 112;
	public static final int setreuid = 113;
	public static final int setregid = 114;
	public static final int getgroups = 115;
	public static final int setgroups = 116;
	public static final int setresuid = 117;
	public static final int getresuid = 118;
	public static final int setresgid = 119;
	public static final int getresgid = 120;
	public static final int getpgid = 121;
	public static final int setfsuid = 122;
	public static final int setfsgid = 123;
	public static final int getsid = 124;
	public static final int capget = 125;
	public static final int capset = 126;
	public static final int rt_sigpending = 127;
	public static final int rt_sigtimedwait = 128;
	public static final int rt_sigqueueinfo = 129;
	public static final int rt_sigsuspend = 130;
	public static final int sigaltstack = 131;
	public static final int utime = 132;
	public static final int mknod = 133;
	public static final int uselib = 134;
	public static final int personality = 135;
	public static final int ustat = 136;
	public static final int statfs = 137;
	public static final int fstatfs = 138;
	public static final int sysfs = 139;
	public static final int getpriority = 140;
	public static final int setpriority = 141;
	public static final int sched_setparam = 142;
	public static final int sched_getparam = 143;
	public static final int sched_setscheduler = 144;
	public static final int sched_getscheduler = 145;
	public static final int sched_get_priority_max = 146;
	public static final int sched_get_priority_min = 147;
	public static final int sched_rr_get_interval = 148;
	public static final int mlock = 149;
	public static final int munlock = 150;
	public static final int mlockall = 151;
	public static final int munlockall = 152;
	public static final int vhangup = 153;
	public static final int modify_ldt = 154;
	public static final int pivot_root = 155;
	public static final int _sysctl = 156;
	public static final int prctl = 157;
	public static final int arch_prctl = 158;
	public static final int adjtimex = 159;
	public static final int setrlimit = 160;
	public static final int chroot = 161;
	public static final int sync = 162;
	public static final int acct = 163;
	public static final int settimeofday = 164;
	public static final int mount = 165;
	public static final int umount2 = 166;
	public static final int swapon = 167;
	public static final int swapoff = 168;
	public static final int reboot = 169;
	public static final int sethostname = 170;
	public static final int setdomainname = 171;
	public static final int iopl = 172;
	public static final int ioperm = 173;
	public static final int create_module = 174;
	public static final int init_module = 175;
	public static final int delete_module = 176;
	public static final int get_kernel_syms = 177;
	public static final int query_module = 178;
	public static final int quotactl = 179;
	public static final int nfsservctl = 180;
	public static final int getpmsg = 181;
	public static final int putpmsg = 182;
	public static final int afs_syscall = 183;
	public static final int tuxcall = 184;
	public static final int security = 185;
	public static final int gettid = 186;
	public static final int readahead = 187;
	public static final int setxattr = 188;
	public static final int lsetxattr = 189;
	public static final int fsetxattr = 190;
	public static final int getxattr = 191;
	public static final int lgetxattr = 192;
	public static final int fgetxattr = 193;
	public static final int listxattr = 194;
	public static final int llistxattr = 195;
	public static final int flistxattr = 196;
	public static final int removexattr = 197;
	public static final int lremovexattr = 198;
	public static final int fremovexattr = 199;
	public static final int tkill = 200;
	public static final int time = 201;
	public static final int futex = 202;
	public static final int sched_setaffinity = 203;
	public static final int sched_getaffinity = 204;
	public static final int set_thread_area = 205;
	public static final int io_setup = 206;
	public static final int io_destroy = 207;
	public static final int io_getevents = 208;
	public static final int io_submit = 209;
	public static final int io_cancel = 210;
	public static final int get_thread_area = 211;
	public static final int lookup_dcookie = 212;
	public static final int epoll_create = 213;
	public static final int epoll_ctl_old = 214;
	public static final int epoll_wait_old = 215;
	public static final int remap_file_pages = 216;
	public static final int getdents64 = 217;
	public static final int set_tid_address = 218;
	public static final int restart_syscall = 219;
	public static final int semtimedop = 220;
	public static final int fadvise64 = 221;
	public static final int timer_create = 222;
	public static final int timer_settime = 223;
	public static final int timer_gettime = 224;
	public static final int timer_getoverrun = 225;
	public static final int timer_delete = 226;
	public static final int clock_settime = 227;
	public static final int clock_gettime = 228;
	public static final int clock_getres = 229;
	public static final int clock_nanosleep = 230;
	public static final int exit_group = 231;
	public static final int epoll_wait = 232;
	public static final int epoll_ctl = 233;
	public static final int tgkill = 234;
	public static final int utimes = 235;
	public static final int vserver = 236;
	public static final int mbind = 237;
	public static final int set_mempolicy = 238;
	public static final int get_mempolicy = 239;
	public static final int mq_open = 240;
	public static final int mq_unlink = 241;
	public static final int mq_timedsend = 242;
	public static final int mq_timedreceive = 243;
	public static final int mq_notify = 244;
	public static final int mq_getsetattr = 245;
	public static final int kexec_load = 246;
	public static final int waitid = 247;
	public static final int add_key = 248;
	public static final int request_key = 249;
	public static final int keyctl = 250;
	public static final int ioprio_set = 251;
	public static final int ioprio_get = 252;
	public static final int inotify_init = 253;
	public static final int inotify_add_watch = 254;
	public static final int inotify_rm_watch = 255;
	public static final int migrate_pages = 256;
	public static final int openat = 257;
	public static final int mkdirat = 258;
	public static final int mknodat = 259;
	public static final int fchownat = 260;
	public static final int futimesat = 261;
	public static final int newfstatat = 262;
	public static final int unlinkat = 263;
	public static final int renameat = 264;
	public static final int linkat = 265;
	public static final int symlinkat = 266;
	public static final int readlinkat = 267;
	public static final int fchmodat = 268;
	public static final int faccessat = 269;
	public static final int pselect6 = 270;
	public static final int ppoll = 271;
	public static final int unshare = 272;
	public static final int set_robust_list = 273;
	public static final int get_robust_list = 274;
	public static final int splice = 275;
	public static final int tee = 276;
	public static final int sync_file_range = 277;
	public static final int vmsplice = 278;
	public static final int move_pages = 279;
	public static final int utimensat = 280;
	public static final int epoll_pwait = 281;
	public static final int signalfd = 282;
	public static final int timerfd_create = 283;
	public static final int eventfd = 284;
	public static final int fallocate = 285;
	public static final int timerfd_settime = 286;
	public static final int timerfd_gettime = 287;
	public static final int accept4 = 288;
	public static final int signalfd4 = 289;
	public static final int eventfd2 = 290;
	public static final int epoll_create1 = 291;
	public static final int dup3 = 292;
	public static final int pipe2 = 293;
	public static final int inotify_init1 = 294;
	public static final int preadv = 295;
	public static final int pwritev = 296;
	public static final int rt_tgsigqueueinfo = 297;
	public static final int perf_event_open = 298;
	public static final int recvmmsg = 299;
	public static final int fanotify_init = 300;
	public static final int fanotify_mark = 301;
	public static final int prlimit64 = 302;
	public static final int name_to_handle_at = 303;
	public static final int open_by_handle_at = 304;
	public static final int clock_adjtime = 305;
	public static final int syncfs = 306;
	public static final int sendmmsg = 307;
	public static final int setns = 308;
	public static final int getcpu = 309;
	public static final int process_vm_readv = 310;
	public static final int process_vm_writev = 311;
	public static final int kcmp = 312;
	public static final int finit_module = 313;
	public static final int sched_setattr = 314;
	public static final int sched_getattr = 315;
	public static final int renameat2 = 316;
	public static final int seccomp = 317;
	public static final int getrandom = 318;
	public static final int memfd_create = 319;
	public static final int kexec_file_load = 320;
	public static final int bpf = 321;
	public static final int execveat = 322;
	public static final int userfaultfd = 323;
	public static final int membarrier = 324;
	public static final int mlock2 = 325;
	public static final int copy_file_range = 326;
	public static final int preadv2 = 327;
	public static final int pwritev2 = 328;
	public static final int pkey_mprotect = 329;
	public static final int pkey_alloc = 330;
	public static final int pkey_free = 331;
	public static final int statx = 332;

}

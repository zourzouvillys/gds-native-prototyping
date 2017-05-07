package io.ewok.linux;

import com.sun.jna.Memory;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class StatFS {

	private final long type;
	private final long blockSize;
	private final long totalBlocks;
	private final long freeBlocks;
	private final long availableBlocks;
	private final long totalNodes;
	private final long freeNodes;
	private final int fsid1;
	private final int fsid2;
	private final long nameMax;
	private final long frsize;
	private final long mountFlags;

	public StatFS(Memory ptr) {

		this.type = ptr.getLong(0);

		this.blockSize = ptr.getInt(8);

		this.totalBlocks = ptr.getLong(16);
		this.freeBlocks = ptr.getLong(24);
		this.availableBlocks = ptr.getLong(32);

		this.totalNodes = ptr.getLong(40);
		this.freeNodes = ptr.getLong(48);

		// ("f_bsize (block size): %lu\n"
		// "f_frsize (fragment size): %lu\n"
		// "f_blocks (size of fs in f_frsize units): %lu\n"
		// "f_bfree (free blocks): %lu\n"
		// "f_bavail free blocks for unprivileged users): %lu\n"
		// "f_files (inodes): %lu\n"
		// "f_ffree (free inodes): %lu\n"
		// "f_favail (free inodes for unprivileged users): %lu\n"
		// "f_fsid (file system ID): %lu\n"
		// "f_flag (mount flags): %lu\n"
		// "f_namemax (maximum filename length)%lu\n",

		this.fsid1 = ptr.getInt(56);
		this.fsid2 = ptr.getInt(60);

		this.nameMax = ptr.getLong(64);

		this.frsize = ptr.getLong(72);

		this.mountFlags = ptr.getLong(80);

	}

}

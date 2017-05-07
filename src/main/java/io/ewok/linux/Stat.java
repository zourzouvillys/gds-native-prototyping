package io.ewok.linux;

import java.time.Instant;

import com.sun.jna.Memory;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Stat {

	private final long deviceId;
	private final long inode;
	private final long numLinks;

	private final int mode;
	private final int userId;
	private final int groupId;

	private final long rdev;
	private final long size;
	private final long blockSize;
	private final long num512kBlocks;

	private final Instant accessTime;
	private final Instant modifyTime;
	private final Instant creationTime;

	public Stat(Memory ptr) {

		this.deviceId = ptr.getLong(0);
		this.inode = ptr.getLong(8);
		this.numLinks = ptr.getLong(16);

		this.mode = ptr.getInt(24);
		this.userId = ptr.getInt(28);
		this.groupId = ptr.getInt(32);

		this.rdev = ptr.getLong(40);
		this.size = ptr.getLong(48);
		this.blockSize = ptr.getLong(56);
		this.num512kBlocks = ptr.getLong(64);

		this.accessTime = Instant.ofEpochMilli((ptr.getLong(72) * 1000) + (ptr.getLong(80) / 1000 / 1000));
		this.modifyTime = Instant.ofEpochMilli((ptr.getLong(88) * 1000) + (ptr.getLong(96) / 1000 / 1000));
		this.creationTime = Instant.ofEpochMilli((ptr.getLong(104) * 1000) + (ptr.getLong(112) / 1000 / 1000));

	}

}

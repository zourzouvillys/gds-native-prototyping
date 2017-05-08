package io.ewok.linux;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.sun.jna.Memory;

import io.ewok.linux.io.LinuxFileType;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class LinuxStat {

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

	public LinuxStat(Memory ptr) {

		this.deviceId = ptr.getLong(0) >> 8;

		this.inode = ptr.getLong(8);
		this.numLinks = ptr.getLong(16);
		this.mode = ptr.getInt(24);

		this.userId = ptr.getInt(28);
		this.groupId = ptr.getInt(32);

		// skip int padding.

		this.rdev = ptr.getLong(40);
		this.size = ptr.getLong(48);

		this.blockSize = ptr.getLong(56);
		this.num512kBlocks = ptr.getLong(64);

		this.accessTime = Instant.ofEpochMilli((ptr.getLong(72) * 1000) + (ptr.getLong(80) / 1000 / 1000));
		this.modifyTime = Instant.ofEpochMilli((ptr.getLong(88) * 1000) + (ptr.getLong(96) / 1000 / 1000));
		this.creationTime = Instant.ofEpochMilli((ptr.getLong(104) * 1000) + (ptr.getLong(112) / 1000 / 1000));

	}

	public long getDeviceMajor() {
		return NativeLinux.libc.gnu_dev_major(this.deviceId);
	}

	public long getDeviceMinor() {
		return NativeLinux.libc.gnu_dev_minor(this.deviceId);
	}

	public int getPermissionBits() {
		return this.getMode() & 07777;
	}

	public EnumSet<LinuxFilePermission> getPermissions() {

		final Set<LinuxFilePermission> set = Sets.newHashSet();

		if ((this.getPermissionBits() & JLinux.S_IRUSR) != 0) {
			set.add(LinuxFilePermission.OwnerRead);
		}
		if ((this.getPermissionBits() & JLinux.S_IWUSR) != 0) {
			set.add(LinuxFilePermission.OwnerWrite);
		}
		if ((this.getPermissionBits() & JLinux.S_IXUSR) != 0) {
			set.add(LinuxFilePermission.OwnerExecute);
		}

		if ((this.getPermissionBits() & JLinux.S_IRGRP) != 0) {
			set.add(LinuxFilePermission.GroupRead);
		}
		if ((this.getPermissionBits() & JLinux.S_IWGRP) != 0) {
			set.add(LinuxFilePermission.GroupWrite);
		}
		if ((this.getPermissionBits() & JLinux.S_IXGRP) != 0) {
			set.add(LinuxFilePermission.GroupExecute);
		}

		if ((this.getPermissionBits() & JLinux.S_IROTH) != 0) {
			set.add(LinuxFilePermission.OtherRead);
		}
		if ((this.getPermissionBits() & JLinux.S_IWOTH) != 0) {
			set.add(LinuxFilePermission.OtherWrite);
		}
		if ((this.getPermissionBits() & JLinux.S_IXOTH) != 0) {
			set.add(LinuxFilePermission.OtherExecute);
		}

		return EnumSet.copyOf(set);

	}

	public boolean isSetUID() {
		return (this.getMode() & JLinux.S_ISUID) != 0;
	}

	public boolean isSetGroupID() {
		return (this.getMode() & JLinux.S_ISGID) != 0;
	}

	public boolean isSticky() {
		return (this.getMode() & JLinux.S_ISVTX) != 0;
	}

	public LinuxFileType getFileType() {
		return LinuxFileType.fromFileMode(this.getMode());
	}

}

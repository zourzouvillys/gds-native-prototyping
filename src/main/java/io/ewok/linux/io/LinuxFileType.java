package io.ewok.linux.io;

import io.ewok.linux.JLinux;

public enum LinuxFileType {

	Socket,

	SymbolicLink,

	Regular,

	BlockDevice,

	Directory,

	CharacterDevice,

	FIFO;

	public static LinuxFileType fromFileMode(int mode) {
		switch (mode & JLinux.S_IFMT) {
			case JLinux.S_IFSOCK:
				return LinuxFileType.Socket;
			case JLinux.S_IFLNK:
				return LinuxFileType.SymbolicLink;
			case JLinux.S_IFREG:
				return LinuxFileType.Regular;
			case JLinux.S_IFBLK:
				return LinuxFileType.BlockDevice;
			case JLinux.S_IFDIR:
				return LinuxFileType.Directory;
			case JLinux.S_IFCHR:
				return LinuxFileType.CharacterDevice;
			case JLinux.S_IFIFO:
				return LinuxFileType.FIFO;
		}
		throw new IllegalArgumentException(Integer.toString(mode & JLinux.S_IFMT));
	}

}

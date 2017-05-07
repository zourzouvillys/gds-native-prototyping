package io.ewok.linux.io;

import io.ewok.linux.JLinux;
import lombok.Getter;

public enum FileAlloc {

	KeepSize(JLinux.FALLOC_FL_KEEP_SIZE),

	PunchHole(JLinux.FALLOC_FL_PUNCH_HOLE),

	NoHideStale(JLinux.FALLOC_FL_NO_HIDE_STALE),

	ZeroRange(JLinux.FALLOC_FL_ZERO_RANGE),

	InsertRange(JLinux.FALLOC_FL_INSERT_RANGE),

	UnshareRange(JLinux.FALLOC_FL_UNSHARE_RANGE)

	;

	@Getter
	private int value;

	FileAlloc(int value) {
		this.value = value;
	}

}

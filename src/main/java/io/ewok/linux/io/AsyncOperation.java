package io.ewok.linux.io;

import io.ewok.linux.JLinux;
import lombok.Getter;

public enum AsyncOperation {

	PREAD(JLinux.IOCB_CMD_PREAD),

	PWRITE(JLinux.IOCB_CMD_PWRITE),

	FSYNC(JLinux.IOCB_CMD_FSYNC),

	FDSYNC(JLinux.IOCB_CMD_FDSYNC),

	PREADV(JLinux.IOCB_CMD_PREADV),

	PWRITEV(JLinux.IOCB_CMD_PWRITEV)

	;

	@Getter
	private int opcode;

	private AsyncOperation(int opcode) {
		this.opcode = opcode;
	}

}

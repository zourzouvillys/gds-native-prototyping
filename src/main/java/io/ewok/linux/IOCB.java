package io.ewok.linux;

import lombok.Getter;

@Getter
public class IOCB {

	private final int fd;

	public IOCB(int fd) {
		this.fd = fd;
	}

}

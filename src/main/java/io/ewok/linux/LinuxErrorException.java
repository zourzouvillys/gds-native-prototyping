package io.ewok.linux;

import com.sun.jna.Native;

import lombok.Getter;

public class LinuxErrorException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Getter
	private final int errno;

	public LinuxErrorException(String msg, int errno) {
		super(String.format("SYSCALL64.%s: errno=%s (%s)", msg, errno, NativeLinux.libc.strerror(errno)));
		this.errno = errno;
	}

	public String getStringError() {
		return NativeLinux.libc.strerror(this.errno);
	}

	public static LinuxErrorException capture(String msg, Object... args) {
		if (args.length > 0) {
			final StringBuilder sb = new StringBuilder(msg).append("(");
			for (final Object arg : args) {
				sb.append(" [").append(arg).append("] ");
			}
			sb.append(")");
			return new LinuxErrorException(sb.toString(), Native.getLastError());
		}
		return new LinuxErrorException(msg, Native.getLastError());
	}

}

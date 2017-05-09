package io.ewok.linux;

public class PerfEvents {

	public static void main(String[] args) {

		final int fd = JLinux.perf_event_open(0, -1, -1, 0);

	}

}

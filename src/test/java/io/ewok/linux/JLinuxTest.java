package io.ewok.linux;

import java.io.IOException;

import org.junit.Test;

public class JLinuxTest {

	@Test
	public void test() throws IOException, InterruptedException {

		System.err.println(JLinux.uname());

		final int eventfd = JLinux.eventfd(0, JLinux.EFD_NONBLOCK | JLinux.EFD_CLOEXEC);

		JLinux.fcntl(eventfd, JLinux.F_SETFL, (JLinux.fcntl(eventfd, JLinux.F_GETFL, 0) | JLinux.O_NONBLOCK));

		System.err.println("EPOLL FD: " + eventfd);

		// channel.allocate(0, 0, 8192 * 32);
		final long ioctx = JLinux.io_setup(512);

		System.err.println(Long.toHexString(ioctx).toUpperCase());

		final int epollfd = JLinux.epoll_create(0);

		JLinux.epoll_ctl_add(epollfd, eventfd, JLinux.EPOLLIN | JLinux.EPOLLET, eventfd);

		final int events = JLinux.epoll_wait(epollfd, 1, -1);

		System.err.println("EVENTS: " + events);

		JLinux.io_destroy(ioctx);

		JLinux.close(epollfd);

		JLinux.close(eventfd);


	}

}

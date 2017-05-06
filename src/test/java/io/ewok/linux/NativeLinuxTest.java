package io.ewok.linux;

import org.junit.Test;

public class NativeLinuxTest {

	@Test
	public void test() {

		System.err.println(NativeLinux.io_setup(0, null));

	}

}

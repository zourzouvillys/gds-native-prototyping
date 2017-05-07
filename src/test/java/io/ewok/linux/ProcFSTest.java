package io.ewok.linux;

import org.junit.Test;

public class ProcFSTest {

	@Test
	public void test() {
		System.err.println(ProcFS.longval("sys/fs/aio-max-nr"));
	}

}

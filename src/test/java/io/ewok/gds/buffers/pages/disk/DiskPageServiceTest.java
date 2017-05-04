package io.ewok.gds.buffers.pages.disk;

import java.nio.file.Paths;

import org.junit.Test;

public class DiskPageServiceTest {

	@Test
	public void test() {

		final DiskPageAccessService pages = new DiskPageAccessService(
				new DefaultDiskAccessMapper(Paths.get("/tmp/gds/")));

	}

}

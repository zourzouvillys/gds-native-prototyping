package io.ewok.gds.buffers.pages.disk;

import java.nio.file.Paths;

import org.junit.Test;

import io.ewok.gds.buffers.pages.PageFork;
import io.ewok.gds.buffers.pages.PageId;

public class DiskAccessPathMapperTest {

	@Test
	public void test() {

		final DefaultDiskAccessMapper mapper = new DefaultDiskAccessMapper(Paths.get("/tmp/gds"));

		System.err.println(mapper.map(new PageId(1, PageFork.Main, 0)));
		System.err.println(mapper.map(new PageId(4354351, PageFork.Main, 0)));
		System.err.println(mapper.map(new PageId(132332, PageFork.Main, 0)));
		System.err.println(mapper.map(new PageId(2, PageFork.Main, 0)));

		System.err.println(mapper.map(new PageId(42342341, PageFork.Main, 12330)));
		System.err.println(mapper.map(new PageId(42342341, PageFork.Main, 250000)));
		System.err.println(mapper.map(new PageId(42342341, PageFork.Main, 499999)));
		System.err.println(mapper.map(new PageId(42342341, PageFork.Main, 999999)));
	}

}

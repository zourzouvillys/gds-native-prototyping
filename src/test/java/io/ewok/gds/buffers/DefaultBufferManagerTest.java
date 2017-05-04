package io.ewok.gds.buffers;

import java.nio.file.Paths;

import org.junit.Test;

import io.ewok.gds.buffers.pages.disk.DefaultDiskAccessMapper;
import io.ewok.gds.buffers.pages.disk.DiskPageAccessService;

public class DefaultBufferManagerTest {

	@Test
	public void test() throws InterruptedException {

		final DiskPageAccessService disk = new DiskPageAccessService(
				new DefaultDiskAccessMapper(Paths.get("/tmp/gds")));

		final DefaultBufferManager mgr = new DefaultBufferManager(disk);

		for (int i = 0; i < 30; ++i) {
			mgr.load(1, i, page -> {
				System.err.println(page);
			});
		}

		Thread.sleep(1000);

	}

}

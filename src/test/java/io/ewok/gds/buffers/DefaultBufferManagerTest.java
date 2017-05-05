package io.ewok.gds.buffers;

import java.nio.file.Paths;
import java.util.Objects;

import org.junit.Test;

import io.ewok.gds.buffers.pages.PageFork;

public class DefaultBufferManagerTest {

	@Test
	public void test() throws InterruptedException {

		final DefaultBufferManager mgr = DefaultBufferManager.create(Paths.get("/tmp/gds"));

		mgr.append(0, PageFork.Main, (ex, page) -> {
			System.err.println("New Page: " + page.pageno());
		});

		for (int i = 0; i < 100_000; ++i) {
			mgr.load(1, i, (ex, page) -> {
				if (ex != null) {
					System.err.println("ERROR: " + ex.getMessage());
				} else {
					Objects.requireNonNull(page);
					System.err.println(page);
				}
			});
		}

		Thread.sleep(1000);

	}

}

package io.ewok.gds.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import io.ewok.gds.buffers.pages.PageFork;
import io.netty.buffer.Unpooled;

public class DiskStorageRelationTest {

	@Test
	public void test() throws IOException, InterruptedException, ExecutionException {

		final Path base = Paths.get("/tmp/gds/");

		Files.createDirectories(base);

		final DiskStorageRelation rel = new DiskStorageRelation(base, 0x1);

		try {

			Assert.assertFalse(rel.exists(PageFork.Main).get());

			rel.create(PageFork.Main).get();

			Assert.assertTrue(rel.exists(PageFork.Main).get());

			Assert.assertEquals(0, (int) rel.nblocks(PageFork.Main).get());

			rel.read(PageFork.Main, 0, Unpooled.directBuffer()).get();

		} finally {

			rel.unlink(PageFork.Main);

		}

	}

}

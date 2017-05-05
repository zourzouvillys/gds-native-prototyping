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

			final StorageRelationFork main = rel.fork(PageFork.Main);

			Assert.assertFalse(main.exists().get());

			main.create(0).get();

			Assert.assertTrue(main.exists().get());

			Assert.assertEquals(0, main.pagecount().get().intValue());

			main.write(0, Unpooled.directBuffer(8192), false).get();

			Assert.assertEquals(1, main.pagecount().get().intValue());

			main.read(0, Unpooled.directBuffer(8192)).get();

		} finally {

			rel.unlink().get();
			rel.close().get();

		}

	}

	@Test
	public void testWithBufferManager() throws IOException, InterruptedException, ExecutionException {

		final Path base = Paths.get("/tmp/gds/");

		Files.createDirectories(base);

		final DiskStorageRelation rel = new DiskStorageRelation(base, 0x1);

		try {

			final StorageRelationFork main = rel.fork(PageFork.Main);

			Assert.assertFalse(main.exists().get());

			main.create(0).get();

			Assert.assertTrue(main.exists().get());

			Assert.assertEquals(0, main.pagecount().get().intValue());

			main.write(0, Unpooled.directBuffer(8192), false).get();

			Assert.assertEquals(1, main.pagecount().get().intValue());

			main.read(0, Unpooled.directBuffer(8192)).get();

		} finally {

			rel.unlink().get();

			rel.close().get();

		}

	}

}

package io.ewok.gds.buffers.pages.disk;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import io.ewok.gds.buffers.pages.PageFork;
import io.ewok.gds.buffers.pages.PageId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class DiskAccessServiceTest {

	// @Test
	public void test() throws InterruptedException, ExecutionException, IOException {

		final Path base = Paths.get("/tmp/gds");

		java.nio.file.Files.createDirectories(base);

		final DiskPageAccessService disk = new DiskPageAccessService(new DefaultDiskAccessMapper(base));

		try {
			final ByteBuf readA = Unpooled.buffer(8192);
			final ByteBuf readB = Unpooled.buffer(8192);
			final ByteBuf writeA = Unpooled.buffer(8192);
			final ByteBuf writeB = Unpooled.buffer(8192);

			writeA.writeZero(8192);
			writeB.writeZero(8192);

			for (int i = 0; i < 10; ++i) {
				writeA.setIndex(0, 0);
				disk.write(writeA, new PageId(0, PageFork.Main, i)).get();
			}

			for (int i = 2; i < 500_000 / 2; ++i) {

				writeA.setIndex(0, 0);
				final CompletableFuture<?> f1a = disk.write(writeA, new PageId(0, PageFork.Main, i));

				writeB.setIndex(0, 0);
				final CompletableFuture<?> f1b = disk.write(writeB, new PageId(0, PageFork.Main, i + 1));

				readA.setIndex(0, 0);
				final CompletableFuture<?> f2a = disk.read(readA, new PageId(0, PageFork.Main, i - 2));

				readB.setIndex(0, 0);
				final CompletableFuture<?> f2b = disk.read(readB, new PageId(0, PageFork.Main, i - 1));

				CompletableFuture.allOf(f1a, f1b, f2a, f2b).get();

			}

		} finally {
			disk.stopAsync().awaitTerminated();
		}

	}

	@Test
	public void testWriteOnly() throws InterruptedException, ExecutionException, IOException {

		final Path base = Paths.get("/tmp/gds");

		java.nio.file.Files.createDirectories(base);

		final DiskPageAccessService disk = new DiskPageAccessService(new DefaultDiskAccessMapper(base));

		try {

			final long pages = 500_000;
			final int workers = 16;

			final ByteBuf[] write = new ByteBuf[workers];

			for (int i = 0; i < workers; ++i) {
				write[i] = Unpooled.directBuffer(8192, 8192);
				write[i].writeZero(8192);
			}

			final CompletableFuture<?>[] futures = new CompletableFuture[workers];

			final long start = System.currentTimeMillis();

			for (long i = 0; i < (pages / workers); ++i) {

				for (int x = 0; x < workers; ++x) {
					write[x].clear();
					final long pno = ThreadLocalRandom.current().nextInt(0, (int) pages);
					futures[x] = disk.write(write[x], new PageId(2, PageFork.Main, pno));
				}

				CompletableFuture.allOf(futures).get();

			}

			final long end = System.currentTimeMillis();

			final long bytes = (8192 * pages);

			final long mb = bytes / 1024 / 1024;

			final double secs = (end - start) / 1000.0;

			System.err.println(String.format("%d MB in %.2f seconds (%.2f MB/sec, %.2f Gbit/sec)", mb, secs,
					(mb / secs), ((mb / secs) * 8) / 1024));

		} finally {
			disk.stopAsync().awaitTerminated();
		}

	}

}

package io.ewok.gds.buffers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import io.ewok.gds.buffers.pages.PageFork;
import io.ewok.gds.buffers.pages.PageId;
import io.ewok.gds.buffers.pages.disk.DefaultDiskAccessMapper;
import io.ewok.gds.buffers.pages.disk.DiskPageAccessService;
import io.ewok.gds.buffers.pages.disk.PageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BenchmarkRunner {

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

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

				// layout & version: first 4 bits are version, second 4 are page
				// size.
				write[i].setByte(0, PageUtils.layout(1, 8192));
				// freeStart
				write[i].setShort(16, 22);
				// freeEnd
				write[i].setShort(18, 8192);

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

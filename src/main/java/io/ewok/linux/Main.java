package io.ewok.linux;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;

import io.ewok.linux.io.AlignedByteBufPool;
import io.ewok.linux.io.AsyncDiskContext;
import io.ewok.linux.io.AsyncResult;
import io.ewok.linux.io.BlockFileHandle;
import io.ewok.linux.io.BlockFiles;
import io.netty.buffer.ByteBuf;

public class Main {

	private static Path file;
	private static int ioq;
	private static int readSize;
	private static int pageSize;
	private static double secs;
	private static long bytes;

	public static void main(String[] args) throws InterruptedException {

		if (args.length != 4) {
			System.err.println("Usage: ... [-DpageSize=4096] [-DreadSize=4096] <file> <threads> <qsize> <readcount>");
		}

		Main.pageSize = Integer.parseInt(System.getProperty("pageSize", "4096"));
		Main.readSize = Integer.parseInt(System.getProperty("readSize", Integer.toString(pageSize)));

		Main.file = Paths.get(args[0]);
		final int numthreads = Integer.parseInt(args[1]);
		Main.ioq = Integer.parseInt(args[2]);
		final int total = Integer.parseInt(args[3]);

		final Thread[] threads = new Thread[numthreads];

		dispatch(true, total / 100);

		System.err.println("Warmed up");

		for (int i = 0; i < numthreads; ++i) {
			threads[i] = new Thread(() -> dispatch(false, total));
		}

		for (int i = 0; i < numthreads; ++i) {
			threads[i].start();
		}

		for (int i = 0; i < numthreads; ++i) {
			threads[i].join();
		}

		final int count = (total * numthreads);

		System.err.println(String.format("%d in %.2f secs (%.02f iops/sec, %.2f MB/sec)",
				count,
				secs,
				count / secs,
				bytes / secs / 1024 / 1024));

	}

	public static void dispatch(boolean warmup, int count) {

		final long nbytes = BlockFiles.stat(file).getSize();

		final long pages = (int) (nbytes / pageSize);

		System.err.println("File Size  : " + nbytes);
		System.err.println("Num. Pages : " + pages);

		try (final BlockFileHandle fd = BlockFiles.openExisting(file)) {

			final AlignedByteBufPool pool = new AlignedByteBufPool(pageSize, ioq);

			int remaining = count;

			final ThreadLocalRandom random = ThreadLocalRandom.current();

			final AsyncResult[] results = AsyncResult.allocate(ioq);

			final long start = System.currentTimeMillis();

			try (final AsyncDiskContext io = AsyncDiskContext.create(ioq)) {

				int num = 0;

				for (int i = 0; i < ioq; ++i) {
					final ByteBuf buf = pool.allocate(1);
					io.read(fd, buf, (random.nextLong(0, pages - 1) * pageSize), readSize, buf);
					num++;
				}

				while (num > 0 || remaining > 0) {

					final int nr = io.events(results);

					for (int i = 0; i < nr; ++i) {

						final AsyncResult result = results[i];

						if (result.result != readSize) {
							throw new RuntimeException("failed: " + result.result);
						}

						Main.bytes += result.result;

						final ByteBuf buf = (ByteBuf) result.attachment;

						--num;

						if (remaining > 0) {
							--remaining;
							if (!warmup && (remaining % (count / 10)) == 0) {
								System.err.println(remaining);
							}
							num++;
							io.read(fd, buf, (random.nextLong(0, pages - 1) * pageSize), readSize, buf);
						}

					}

					io.flush();

				}

				pool.reset();

			}

			Main.secs = (System.currentTimeMillis() - start) / 1000.0;

		}
		catch (final Exception ex) {
			ex.printStackTrace();
		}

	}

}

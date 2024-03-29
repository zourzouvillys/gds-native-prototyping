package io.ewok.linux.io;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import io.ewok.io.EwokPlatform;
import io.ewok.io.PageBuffer;
import io.ewok.io.ReadBlockFileHandle;

public class AsyncDiskContextTest {

	@Test
	public void test() throws InterruptedException, ExecutionException {

		final Path file = Paths.get("/tmp/");

		// try (BlockFileHandle tmp =
		// BlockFiles.createTempFile(Paths.get("/tmp"), 8192)) {

		try (final ReadBlockFileHandle fd = EwokPlatform.fs().openExistingForRead(file.resolve("moo"))) {

			// BlockFiles.unlink(file.resolve("moo"));

			final int ioq = 32;

			final AlignedByteBufPool pool = new AlignedByteBufPool(4096, ioq);

			// fd.write(pool.allocate(2).writeZero(2048)).flush();

			final int pages = 100_000;

			final int readSize = 4096;

			final int total = 100_000;

			int remaining = total;
			long bytes = 0;

			final ThreadLocalRandom random = ThreadLocalRandom.current();

			final AsyncBlockResult[] results = AsyncBlockResult.allocate(ioq);

			final long start = System.currentTimeMillis();

			try (final AsyncDiskContext io = AsyncDiskContext.create(ioq)) {

				int num = 0;

				for (int i = 0; i < ioq; ++i) {
					final PageBuffer buf = pool.allocate();
					io.read(fd, buf, random.nextInt(0, pages - 1) * 4096, readSize, buf);
					num++;
				}

				while (num > 0 || remaining > 0) {

					final int done = io.events(results);

					for (int i = 0; i < done; ++i) {

						final AsyncBlockResult result = results[i];

						final PageBuffer buf = (PageBuffer) result.attachment;

						if (result.result != readSize) {
							throw new RuntimeException(Long.toString(result.result));
						}

						bytes += result.result;

						--num;

						if (remaining > 0) {
							--remaining;
							if ((remaining % 100_000) == 0) {
								System.err.println(remaining);
							}
							num++;
							io.read(fd, buf, random.nextInt(0, pages - 1) * 4096, readSize, buf);
						}

					}

					io.flush();

				}

				pool.reset();

			}

			final double secs = (System.currentTimeMillis() - start) / 1000.0;

			System.err.println(String.format("%,d in %,.2f secs (%,.0f iops/sec, %,.0f MB/sec)", total, secs, total / secs,
					bytes / secs / 1024 / 1024));

		}

		// }

	}

}

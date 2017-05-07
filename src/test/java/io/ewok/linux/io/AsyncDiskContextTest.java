package io.ewok.linux.io;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class AsyncDiskContextTest {

	@Test
	public void test() throws InterruptedException, ExecutionException {

		final Path file = Paths.get("/tmp/");

		try (BlockFileHandle tmp = BlockFiles.createTempFile(Paths.get("/tmp"), 8192)) {

			try (final BlockFileHandle fd = BlockFiles.openExisting(file.resolve("moo"))) {

				//BlockFiles.unlink(file.resolve("moo"));

				final AlignedByteBufPool pool = new AlignedByteBufPool(4096, 16, 4096);

				//fd.write(pool.allocate(2).writeZero(2048)).flush();

				try (final AsyncDiskContext io = AsyncDiskContext.createDefault()) {

					io.read(fd, pool.allocate(2), 0, 8192);
					io.read(fd, pool.allocate(2), 0, 8192);
					io.read(fd, pool.allocate(2), 0, 8192);
					io.read(fd, pool.allocate(2), 0, 8192);
					io.read(fd, pool.allocate(2), 0, 8192);
					io.read(fd, pool.allocate(2), 0, 8192);
					io.read(fd, pool.allocate(2), 0, 8192);
					io.read(fd, pool.allocate(2), 0, 8192);

					io.events();

				}

			}

		}

	}

}

package io.ewok.io.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.google.common.util.concurrent.MoreExecutors;

import io.ewok.io.EwokPlatform;
import io.ewok.io.PageBufferPool;
import io.ewok.io.PageBufferUtils;
import io.ewok.io.ReadWriteBlockFileHandle;
import io.ewok.linux.io.AlignedByteBufPool;
import io.ewok.linux.io.AsyncBlockResult;
import io.ewok.linux.io.AsyncDiskContext;

public class PageBufferedBlockFileWriterTest {

	@Test

	public void test() throws InterruptedException {

		final Path path = Paths.get("/tmp/wal");

		try (final ReadWriteBlockFileHandle file = EwokPlatform.fs().createFile(path, BlockSizeUnits.MB.bytes(16))) {

			// create a pool of buffers aligned to the given page size.
			final PageBufferPool pool = AlignedByteBufPool.createAligned(file.pageSize(), 16, BlockSizeUnits.MB);

			// the io context we will use.
			final AsyncDiskContext io = AsyncDiskContext.createDefault();

			// write the page out.
			io.write(file, PageBufferUtils.pattern(pool.allocate()), 0, file.pageSize());

			// sync it.
			this.sync(io);

			// now create the writer.
			final PageBufferedBlockFileWriter writer = new PageBufferedBlockFileWriter(pool, file, io, file.size());

			// initialise the writer with any data on the page.
			writer.init();

			// wait for the io to complete.
			this.sync(io);

			// flush the writers pages to disk.
			writer.flush();

			// wait for the io to complete.
			this.sync(io);

			System.err.println(io);

			file.truncate(0);

			// XThreadInfo.main(null);

		} finally {

			EwokPlatform.fs().unlink(path);

		}

	}

	final AsyncBlockResult[] res = new AsyncBlockResult[] { new AsyncBlockResult() };

	private void sync(AsyncDiskContext io) {
		io.flush();
		while (io.outstanding() > 0) {
			io.dispatch(this.res, MoreExecutors.directExecutor());
		}
	}

}

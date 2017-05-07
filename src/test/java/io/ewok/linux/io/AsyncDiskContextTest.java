package io.ewok.linux.io;

import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import io.ewok.linux.JLinux;

public class AsyncDiskContextTest {

	@Test
	public void test() throws InterruptedException, ExecutionException {

		System.err.println(JLinux.stat(Paths.get("/tmp/moo")));


		try (final AsyncDiskContext io = AsyncDiskContext.open(1024)) {
			io.create(Paths.get("/tmp/moo"), 8192, AsyncDiskPriority.Normal).get();
		}

	}

}

package io.ewok.gds.journal;

import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import io.netty.buffer.Unpooled;

public class JournalPageManagerTest {

	@Test
	public void testAppend() throws InterruptedException, ExecutionException {
		final JournalWriter w = new JournalWriter(Paths.get("/tmp/gds"), 0x1);
		w.create(0);
		w.append("HELLO".getBytes());
		final long offset = w.append(Unpooled.directBuffer().writeInt(Integer.MAX_VALUE - 1));
		w.flush(offset).get();
	}

	@Test
	public void testFileNAmes() {

		Assert.assertEquals(0L, JournalFileUtils.base(16_384_000, 0));
		Assert.assertEquals(0L, JournalFileUtils.base(16_384_000, 1000));
		Assert.assertEquals(16_384_000, JournalFileUtils.base(16_384_000, 16_384_000));
		Assert.assertEquals(16_384_000, JournalFileUtils.base(16_384_000, 16_384_000 + 10000));

		Assert.assertEquals("000000010000000000000000", JournalFileUtils.segment(1, 16_384_000, 0));
		Assert.assertEquals("000000010000000000FA0000", JournalFileUtils.segment(1, 16_384_000, 16_384_000 + 1000));
		Assert.assertEquals("000000010000000000FA0000", JournalFileUtils.segment(1, 16_384_000, 16_384_000 + 1));
		Assert.assertEquals("000000010000000000FA0000", JournalFileUtils.segment(1, 16_384_000, 16_384_000 + 100_000));

	}

}

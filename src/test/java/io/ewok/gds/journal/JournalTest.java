package io.ewok.gds.journal;

import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import io.ewok.gds.work.WorkContext;

public class JournalTest {

	@Test
	public void test() throws InterruptedException, ExecutionException {

		final JournalWriter wal = new JournalWriter(Paths.get("/tmp/gds"), 0x1);

		wal.create(0);

		final Journal journal = new Journal(wal);

		journal.write(WorkContext.SYSTEM, JournalEntries.txnAbort(0));
		journal.write(WorkContext.SYSTEM, JournalEntries.txnAbort(0));
		final long offset = journal.write(WorkContext.SYSTEM, JournalEntries.txnAbort(0));

		System.err.println(offset);

		wal.flush(offset).get();
	}

}

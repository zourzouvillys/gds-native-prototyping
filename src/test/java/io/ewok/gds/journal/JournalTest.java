package io.ewok.gds.journal;

import org.junit.Test;

import io.ewok.gds.work.WorkContext;

public class JournalTest {

	@Test
	public void test() {
		final Journal journal = new Journal();
		journal.write(WorkContext.SYSTEM, JournalEntries.txnAbort(0));
		journal.write(WorkContext.SYSTEM, JournalEntries.txnAbort(0));
		final long offset = journal.write(WorkContext.SYSTEM, JournalEntries.txnAbort(0));

		System.err.println(offset);
	}

}

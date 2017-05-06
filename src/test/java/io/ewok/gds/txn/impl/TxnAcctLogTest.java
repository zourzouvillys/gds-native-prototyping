package io.ewok.gds.txn.impl;

import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import io.ewok.gds.journal.Journal;
import io.ewok.gds.journal.JournalWriter;
import io.ewok.gds.work.WorkContext;

public class TxnAcctLogTest {

	@Test
	public void test() throws InterruptedException, ExecutionException {


		final JournalWriter writer = new JournalWriter(Paths.get("/tmp/gds"), 1);

		writer.create(0);

		final Journal journal = new Journal(writer);

		final TxnAcctLog log = new TxnAcctLog(journal);

		log.begin(WorkContext.SYSTEM);

		writer.flush().get();

	}

}

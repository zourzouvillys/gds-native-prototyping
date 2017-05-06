package io.ewok.gds.journal;

import java.util.concurrent.CompletableFuture;

import io.ewok.gds.journal.messages.JournalEntry;
import io.ewok.gds.work.WorkContext;

public interface WAL {

	/**
	 * write a WAL entry, and flush the journal to disk at this position,
	 * ensuring durability.
	 */

	CompletableFuture<Long> writeAndFlush(WorkContext ctx, JournalEntry... entry);

	/**
	 * Write the given message out to the log.
	 */

	long write(WorkContext ctx, JournalEntry... entry);

}

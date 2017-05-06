package io.ewok.gds.journal;

import java.util.concurrent.CompletableFuture;

import io.ewok.gds.journal.messages.JournalEntry;
import io.ewok.gds.journal.messages.JournalRecord;
import io.ewok.gds.work.WorkContext;
import io.ewok.gds.work.WorkLimitUnit;
import lombok.NonNull;

public class Journal implements WAL {

	private final JournalWriter writer;

	/**
	 *
	 */

	public Journal(JournalWriter writer) {
		this.writer = writer;
	}

	/**
	 * Write an entry, and then flush the journal to disk.
	 */

	@Override
	public CompletableFuture<Long> writeAndFlush(WorkContext ctx, JournalEntry... entry) {
		ctx.consume(WorkLimitUnit.JournalFlush, 1);
		final long offset = this.write(ctx, entry);
		return this.writer.flush(offset).thenApply(x -> offset);
	}

	/**
	 * Write an entry into the journal for the given work context.
	 */

	@Override
	public long write(@NonNull WorkContext ctx, JournalEntry... entries) {

		ctx.consume(WorkLimitUnit.JournalRecords, 1);

		final JournalRecord.Builder builder = JournalRecord.newBuilder();

		for (final JournalEntry entry : entries) {
			builder.addEntry(entry);
		}

		final JournalRecord record = builder.build();

		ctx.consume(WorkLimitUnit.JournalBytes, record.getSerializedSize());

		return this.writer.append(record.toByteArray());

	}

}

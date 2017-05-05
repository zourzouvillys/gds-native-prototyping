package io.ewok.gds.journal;

import java.util.concurrent.CompletableFuture;

import io.ewok.gds.journal.messages.JournalEntry;
import io.ewok.gds.journal.messages.JournalRecord;
import io.ewok.gds.wal.WAL;
import io.ewok.gds.work.WorkContext;
import io.ewok.gds.work.WorkLimitUnit;
import lombok.NonNull;

public class Journal implements WAL {

	/**
	 * Our position in the WAL.
	 */

	private long currentOffset = 0;

	/**
	 * Write an entry, and then flush the journal to disk.
	 */

	@Override
	public CompletableFuture<Long> writeAndFlush(WorkContext ctx) {
		ctx.consume(WorkLimitUnit.JournalFlush, 1);
		final long offset = this.write(ctx);
		return CompletableFuture.completedFuture(offset);
	}

	/**
	 * Write an entry into the journal for the given work context.
	 */

	@Override
	public long write(@NonNull WorkContext ctx, JournalEntry... entries) {

		ctx.consume(WorkLimitUnit.JournalRecords, 1);

		final JournalRecord.Builder builder = JournalRecord.newBuilder();

		int bytes = 0;

		for (final JournalEntry entry : entries) {
			bytes += entry.getSerializedSize();
			builder.addEntry(entry);
		}

		ctx.consume(WorkLimitUnit.JournalBytes, bytes);

		return this.send(builder);

	}

	/**
	 * Append journal record to the current page. We don't perform any sync.
	 */

	private long send(JournalRecord.Builder record) {

		final JournalRecord je = record.build();

		final int size = je.getSerializedSize();

		this.currentOffset += size;

		return this.currentOffset;

	}

}

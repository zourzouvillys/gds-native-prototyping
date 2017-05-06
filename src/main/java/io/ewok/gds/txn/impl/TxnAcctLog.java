package io.ewok.gds.txn.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.ewok.gds.journal.JournalEntries;
import io.ewok.gds.journal.WAL;
import io.ewok.gds.work.WorkContext;
import io.ewok.gds.work.WorkLimitUnit;

/**
 * Transaction status accounting log.
 *
 * Each transaction is assigned 2 bits, and has a current head (FrozenXID) Each
 * page is a block of 8192 bytes, so ((8192 - 2 - 4) * 4) = 32k transactions per
 * page minus the checksum and head XID. The log uses its own buffer pool, which
 * should be set to keep the number of pages that trail back as far as there are
 * unfrozen transactions.
 *
 * Changes to the txactlog is recorded in the WAL using TxnAcct messages.
 *
 * Starting a transaction tries to be non blocking. We pre-fetch a new page in
 * the background before space runs out - keeping one page extra, and allocating
 * a new one when we rollover to the next one. If a new page has not been
 * allocated by the time we need to use it, then we return a temporary failure.
 * Even with 10k transactions a second, this would have to take more than 3
 * seconds to generate a new page - something is obviously wrong in this
 * scenario! If it was to be an issue, the preallocation policy could be
 * increased to work around it.
 *
 * @author theo
 *
 */

public class TxnAcctLog {

	/**
	 * The XACT page buffers.
	 */

	private final Object xactdat = null;

	/**
	 * The system WAL.
	 */

	private final WAL wal;

	/**
	 * The next XID to assign a transaction.
	 */

	private volatile int nextXID = 1;

	private final Map<Integer, XID> transactions = new HashMap<>();

	/**
	 * Initialise with the WAL.
	 */

	TxnAcctLog(WAL wal) {
		this.wal = wal;
	}

	/**
	 * Allocate a new XID (increment by one) which will be in the "Active"
	 * state. This generates a WAL message TxnBegin.
	 *
	 * We reserve enough credit in the WorkContext to always be able to perform
	 * an abort.
	 *
	 */

	public int begin(WorkContext ctx) {

		// we need to consume enough for an abort or commit.
		ctx.consume(WorkLimitUnit.JournalBytes, 8);
		ctx.consume(WorkLimitUnit.JournalRecords, 1);
		ctx.consume(WorkLimitUnit.JournalFlush, 1);

		this.wal.write(ctx, JournalEntries.txnBegin(0));

		// set the transaction flags

		this.transactions.put(this.nextXID, new XID(this.nextXID));

		return this.nextXID++;

	}

	/**
	 * Mark the XID as aborted, recording it in the WAL.
	 *
	 * This write will always succeed, as we reserve journal credits from the
	 * {@link WorkContext} when starting the transaction.
	 *
	 */

	public void abort(WorkContext ctx, int xid) {
		this.transactions.get(xid).aborted();
		this.wal.write(ctx, JournalEntries.txnAbort(xid));
	}

	/**
	 * Commit this XID.
	 *
	 * To commit, we append TxnCommit, and flush the WAL to that point. Once
	 * flushed, we can confirm that the changes are applied to the caller.
	 *
	 */

	public CompletableFuture<?> commit(WorkContext ctx, int xid) {
		this.transactions.get(xid).commited();
		return this.wal.writeAndFlush(ctx, JournalEntries.txnCommit(xid));
	}

}

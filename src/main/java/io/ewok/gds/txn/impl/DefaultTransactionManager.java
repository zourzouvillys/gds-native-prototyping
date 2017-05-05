package io.ewok.gds.txn.impl;

import io.ewok.gds.txn.MutableView;
import io.ewok.gds.txn.StableView;
import io.ewok.gds.txn.TransactionManager;

/**
 * Keeps track of transactions in progress and the associated resources.
 *
 * @author theo
 *
 */

public class DefaultTransactionManager implements TransactionManager {

	/**
	 * The global XID after which all references to previous transactions have
	 * been removed, and any tuples that were part of an aborted transaction
	 * removed from storage. The number of transactions is limited to (2^31-1)
	 * from this XID. Any attempt to allocate a transaction that would wrap
	 * around past the frozen XID will fail.
	 *
	 * Once a transaction has been committed, it can be replaced with this XID
	 * at any point after to indicate that the XID information has been purged.
	 * This XID is always shown as visible and committed.
	 *
	 * GDS must hold on to all transaction information until a full vacuum has
	 * been performed up to that XID.
	 *
	 * Each transaction consumes 64 bits, so a million transactions will consume
	 * 50MB of storage. However, when mapped this uses 1 bit per transaction to
	 * indicate if it was committed (bit is set) or aborted (bit is unset). This
	 * means 1 MB per 1 million transactions.
	 *
	 * The WAL log can be used to find pages which contains records to freeze
	 * the XIDs of. The implementation keeps history up to a point that it
	 * wishes to be able to view by transporting back, after which point it can
	 * tidy up pages and replace xmin with the frozen XID.
	 *
	 */

	private final int frozenXid = 0;

	/**
	 * Allocate (start) a new transaction.
	 *
	 * The steps needed are:
	 *
	 * <ul>
	 * <li>Grab transaction lock</li>
	 * <li>Allocate XID</li>
	 * <li>Record frozen XID and currently open transactions to WAL. (no flush
	 * needed).</li>
	 * <li>Return transaction handle</li>
	 * </ul>
	 *
	 */

	@Override
	public MutableView begin() {
		return null;
	}

	/**
	 * Create a new stable view.
	 *
	 * Stable views do not consume any transactional resources directly.
	 * Multiple snapshot views created without a new transaction being started
	 * can be shared, so we keep a cache. For read-heavy instances, this saves a
	 * lot of calculation.
	 *
	 */

	@Override
	public StableView view() {
		return null;
	}

	/**
	 * Commit a previously prepared transaction.
	 */

	@Override
	public void commit(String tpcid) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

}

package io.ewok.gds.txn;

/**
 * All operations require a transaction. Transactions must be short lived.
 *
 * @author theo
 *
 */

public interface TransactionManager {

	/**
	 * Start a new read only transaction.
	 */

	StableView view();

	/**
	 * Start a transaction for mutation.
	 */

	MutableView begin();

	/**
	 * Commit a previously prepared transaction for a two phase commit.
	 *
	 * @param tpcid
	 *            The client supplied two phase commit identifier.
	 */

	void commit(String tpcid);

}

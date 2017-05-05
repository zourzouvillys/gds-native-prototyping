package io.ewok.gds.txn.impl;

public enum TxnStatus {

	/**
	 * Transaction is currently open (00 - 0x0).
	 */

	Active,

	/**
	 * Transaction has been committed (01 - 0x1).
	 */

	Commited,

	/**
	 * Transaction has been aborted (10 - 0x2).
	 */

	Aborted

}

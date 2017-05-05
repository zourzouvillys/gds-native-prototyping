package io.ewok.gds.txn;

import java.util.Set;

public interface StableView extends AutoCloseable {

	/**
	 * The transaction identifier.
	 */

	int tid();

	/**
	 * Transactions which were open at the time we started this view.
	 */

	Set<Integer> xopen();

	/**
	 * Releases any resources associated with this view.
	 */

	@Override
	void close();

}

package io.ewok.gds.txn;

import java.util.concurrent.CompletableFuture;

/**
 * A view which can be used for modifications to objects.
 *
 * Changes made in this view can not be seen by anyone else until
 * {@link #commit()} is called.
 *
 * <code>
 *   try (MutableView view = ...) {
 *   	view.commit();
 *   }
 * </code>
 *
 * @author theo
 *
 */

public interface MutableView extends StableView {

	/**
	 * How many mutations have been made so far.
	 */

	int mutations();

	/**
	 * Prepare for a two phase commit.
	 *
	 * @param tpcid
	 *            A client supplied unique two phase commit id. This will be
	 *            passed to {@link TransactionManager#commit(String)} to apply
	 *            it.
	 */

	CompletableFuture<?> prepare(String tpcid);

	/**
	 * Make all changes made in this view visible globally.
	 *
	 * Note that {@link StableView#close()} must still be called after this to
	 * release the view.
	 *
	 * @return the transaction ID if committed successfully, otherwise the
	 *         transaction error.
	 *
	 */

	CompletableFuture<String> commit();

}

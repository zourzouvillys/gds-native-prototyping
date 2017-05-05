package io.ewok.gds.work;

/**
 * Tracks in memory work currently in progress.
 *
 * A work context may be short or long lived. A producer on a stream could
 * continue a work context indefinitely. A query may fetch a single page and be
 * done.
 *
 * A work context has nothing to do with a transaction - although a transaction
 * is always running within the scope of a {@link WorkContext}. A
 * {@link WorkContext} can have any number of transactions come and go over
 * time. However, a transaction is always assigned to a work context - although
 * the work context can be changed mid transaction.
 *
 * The {@link WorkContext} also dispatches async work and provides admission
 * control. It can be used to cancel {@link WorkContext} instances that are
 * using too much resources.
 *
 * @author theo
 *
 */

public class WorkManager {

	/**
	 * Allocate a work context.
	 */

	public WorkContext allocate(WorkLimit limits) {
		return new WorkContext(this, limits);
	}

}

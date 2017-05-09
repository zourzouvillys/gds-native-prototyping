package io.ewok.fiber;

import com.google.common.base.Preconditions;

import io.ewok.jvm.GCMointor;

/**
 * The scheduler creates, schedules, and destroys {@link Fiber} instances and
 * the {@link FiberThread} instances they run in.
 *
 * NUMA: be aware of running on NUMA nodes: Just like with CPU pinning of
 * threads to minimize pipeline stalls, avoid migrating work between NUMA nodes,
 * and treat any non local memory ranges as expensive fetches.
 *
 *
 *
 * @author theo
 */

public class Scheduler {

	public Scheduler() {
		GCMointor.installGCMonitoring();
	}

	/**
	 * Create a new unscheduled {@link Fiber}.
	 */

	public Fiber allocate(FiberGuest guest) {
		return new RuntimeFiberHost(this, guest);
	}

	/**
	 * Adds a {@link Fiber} to the run-queue.
	 *
	 * @param fiber
	 *            The {@link Fiber} to start. Must be in
	 *            {@link FiberState#Initial} state.
	 *
	 */

	void schedule(RuntimeFiberHost fiber) {
		Preconditions.checkArgument(fiber.isRunnable());
		fiber.resume();
	}

	void dispatch(RuntimeFiberHost runtimeFiberHost, Runnable run) {
	}

	void signal(RuntimeFiberHost runtimeFiberHost) {
	}

	void join(RuntimeFiberHost runtimeFiberHost) {
	}

	ThreadGroup threadGroup = new ThreadGroup("FIBERS");

	public ThreadGroup threadGroup() {
		return this.threadGroup;
	}

	public void startThread() {
		new FiberThread(this).start();
	}

	public void removeThread(FiberThread t) {
		System.err.println("Thread finished");
	}

}

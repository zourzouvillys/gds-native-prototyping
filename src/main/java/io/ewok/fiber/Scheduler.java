package io.ewok.fiber;

import java.util.ArrayList;

import com.google.common.base.Preconditions;

import lombok.SneakyThrows;

/**
 * The scheduler creates, schedules, and destroys {@link Fiber} instances and
 * the {@link FiberThread} instances they run in.
 *
 *
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

	private final ArrayList<FiberThread> threads = new ArrayList<>();


	public Scheduler() {
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
		final FiberThread thread = new FiberThread(this);
		this.threads.add(thread);
		thread.start();
	}

	/**
	 * called by the {@link FiberThread} when it has finished processing, and is
	 * about to exit.
	 */

	@SneakyThrows
	public void removeThread(FiberThread t) {
		System.err.println("Thread " + t + " finished");
		// we need to enqueue a Thread#join to reap it.
	}

	/**
	 * called to shutdown the scheduler.
	 */

	public void shutdown() {
		this.threads.forEach(thread -> thread.shutdown());
		this.threads.forEach(thread -> {
			try {
				thread.join();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		});
	}

}

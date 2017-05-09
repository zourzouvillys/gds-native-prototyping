package io.ewok.fiber;

import java.time.Instant;

/**
 * A {@link Fiber} is a light weight async work context that spans multiple
 * executions but doesn't ever run in parallel.
 *
 * A Fiber runs until it has no pending async work left and no timers.
 *
 * @author theo
 *
 */

public interface Fiber {

	/**
	 * Returns the current time.
	 */

	Instant now();

	/**
	 * The current state of the fiber.
	 */

	FiberState state();

	/**
	 * Dispatch a signal to this {@link Fiber}.
	 */

	Fiber signal();

	/**
	 * requests that this {@link Fiber} is scheduled. will result in
	 * {@link FiberGuest#reenter(Fiber)} being called at some point.
	 */

	Fiber schedule();

	/**
	 * post some work to this {@link Fiber}
	 */

	Fiber dispatch(Runnable run);

	/**
	 * Join this Fiber, meaning that this call will block until it has
	 * terminated.
	 */

	Fiber join();

}

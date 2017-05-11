package io.ewok.fiber;

/**
 * Code that runs within a {@link Fiber} can implement this interface to receive
 * signalling events from the scheduler.
 *
 * @author theo
 *
 */

public interface FiberGuest {

	/**
	 * called on first run to start this fiber.
	 */

	void start(Fiber fiber) throws Exception;

	/**
	 * called each time there is some external wakeup.
	 */

	void reenter(Fiber fiber) throws Exception;

	/**
	 * A signal provided by the scheduler
	 */

	void signal(Fiber fiber) throws Exception;

	/**
	 * called when this guest is serviced because a caller dispatched to us but
	 * we were not scheduled.
	 */

	void rapidenter(Fiber self, Fiber caller) throws Exception;

}

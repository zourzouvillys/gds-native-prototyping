package io.ewok.fiber;

public enum FiberState {

	/**
	 * The process has been created, but not yet started.
	 */

	Initial,

	/**
	 * The Fiber is either currently running, or on the run queue.
	 */

	Running,

	/**
	 * The Fiber is blocked on something that can't be inturrupted.
	 */

	Blocked,

	/**
	 * The Fiber is asleep but can be interrupted.
	 */

	InterruptibleSleep,

	/**
	 * The fiber is stopped, but still consuming resources - it needs to be
	 * resumed to move to another state.
	 */

	Stopped,

	/**
	 * The process is dead. It is currently being deallocated.
	 */

	Dead,

	/**
	 * The process has terminated, but it has not been reaped by its parent yet.
	 */

	Defunct

}

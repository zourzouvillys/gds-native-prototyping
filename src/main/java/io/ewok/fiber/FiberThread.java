package io.ewok.fiber;

import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

/**
 * A thread which runs {@link Fiber} executions.
 *
 * @author theo
 *
 */

@Slf4j
final class FiberThread extends Thread implements Thread.UncaughtExceptionHandler {

	private final Scheduler scheduler;

	FiberThread(Scheduler scheduler) {
		super(scheduler.threadGroup(), null, "FIBER-THREAD", 0);
		this.scheduler = scheduler;
		this.setDaemon(false);
		this.setPriority(Thread.MAX_PRIORITY);
		this.setUncaughtExceptionHandler(this);
	}

	/**
	 *
	 */

	@Override
	public void run() {

		try {

			// wait for work.

		} catch (final Exception ex) {

		}

		this.scheduler.removeThread(this);

	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {

		try {
			// cannot use FormattingLogger due to a dependency loop
			log.error(String.format(Locale.ROOT, "Caught an exception in %s.  Shutting down.", t), e);
		} catch (final Throwable errorInLogging) {
			// If logging fails, e.g. due to missing memory, at least try to log
			// the
			// message and the cause for the failed logging.
			System.err.println(e.getMessage());
			System.err.println(errorInLogging.getMessage());
		} finally {

			// we need to kill the Fiber that generated this.

		}

	}
}

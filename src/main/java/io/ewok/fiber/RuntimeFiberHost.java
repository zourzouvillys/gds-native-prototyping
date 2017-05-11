package io.ewok.fiber;

import java.time.Instant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class RuntimeFiberHost implements Fiber {

	private final Scheduler scheduler;

	private final FiberGuest guest;

	volatile boolean running = false;
	volatile FiberState state = FiberState.Initial;

	private long lastScheduledAt;

	// when in the run queue, this is
	private RuntimeFiberHost runq;

	RuntimeFiberHost(Scheduler scheduler, FiberGuest guest) {
		this.scheduler = scheduler;
		this.guest = guest;
	}

	/**
	 * called by the scheduler to execute.
	 */

	void resume() {

		try {

			//this.lastScheduledAt = System.nanoTime();

			switch (this.state) {
				case Initial:
					this.state(FiberState.Running);
					this.guest.start(this);
					break;
				case Running:
					this.guest.reenter(this);
					break;
				case Blocked:
				case Dead:
				case Defunct:
				case InterruptibleSleep:
				case Stopped:
					break;
			}


		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *
	 */

	void state(FiberState state) {
		log.info("FIBER {} -> {}", this.state, state);
		this.state = state;
	}

	/**
	 *
	 */

	@Override
	public FiberState state() {
		return this.state;
	}

	/**
	 * Perform the dispatch.
	 */

	@Override
	public RuntimeFiberHost schedule() {
		this.scheduler.schedule(this);
		return this;
	}

	@Override
	public RuntimeFiberHost dispatch(Runnable run) {
		this.scheduler.dispatch(this, run);
		return this;
	}

	@Override
	public RuntimeFiberHost signal() {
		this.scheduler.signal(this);
		return this;
	}

	@Override
	public RuntimeFiberHost join() {
		this.scheduler.join(this);
		return this;
	}

	public FiberGuest guest() {
		return this.guest;
	}

	@Override
	public Instant now() {
		return Instant.ofEpochMilli(this.lastScheduledAt / 1000 / 1000);
	}

	public boolean isRunnable() {

		switch (this.state) {
			case Initial:
			case Running:
				return true;
			case Blocked:
			case Dead:
			case Defunct:
			case InterruptibleSleep:
			case Stopped:
				break;
		}
		throw new IllegalArgumentException();
	}

}

package io.ewok.gds.work;

import lombok.NonNull;

public class WorkContext {

	public static final WorkContext SYSTEM = new WorkContext(null, WorkLimit.builder().build());

	private final WorkLimit limits;
	private final WorkManager mgr;

	public WorkContext(WorkManager mgr, @NonNull WorkLimit limits) {
		this.mgr = mgr;
		this.limits = limits;
	}

	/**
	 * schedule a callback to continue processing this work context.
	 */

	public void schedule(Runnable run) {
		run.run();
	}

	public void consume(WorkLimitUnit unit, long count) {
	}

}

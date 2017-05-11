package io.ewok.fiber;

import io.ewok.io.PageBuffer;

public interface FiberRuntime {

	/**
	 * Provide a clean {@link PageBuffer}.
	 *
	 * <p>
	 * The {@link PageBuffer} will be kept around until this fiber completes,
	 * unless {@link PageBuffer#release()} is called.
	 * </p>
	 *
	 * <p>
	 * The scheduler can optionally protect page access outside of this fiber by
	 * checking memory accesses. note that this isn't super fast, so should only
	 * be used for development / debugging.
	 * </p>
	 *
	 * @param flags:
	 *            PRIVATE, SHARED
	 *
	 * @return
	 */

	PageBuffer pool(int pagesize, int align, int flags);

	// - Buffer Pools (Pages & Protection)
	// - Clock Time & Scheduler Ticks
	// - Ambient State & Work Contexts
	// - Cancellation Tokens
	// - Async Callback Scheduling
	// - Stream Backpressure
	// - Logging
	// - Metrics / Perf
	// - Network IO
	// - Disk IO
	// - Async FD/path operations
	// - Encryption
	// - IO DMA
	// - Inter Fiber Signals
	// - Inter Fiber Channels
	// - Inter Fiber Memory
	// - Fiber Dependency Scheduling
	// - Service Registry (iface based marshalling)
	// - Scheduling Priorities
	// - API timeout handlers

}

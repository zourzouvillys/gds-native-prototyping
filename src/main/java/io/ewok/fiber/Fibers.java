package io.ewok.fiber;

/**
 * An ongoing context of execution that can be scheduled or yielded due to
 * waiting for external io, timers, or some other async notification.
 *
 * When a Fiber yields, it gets rescheduled by the FiberScheduler. A Fiber can
 * optionally ask the scheduler if it should yield itself and request to be
 * rescheduled.
 *
 * A Fiber may not be cancelled or aborted directly. Instead, it is signalled.
 *
 * @author theo
 */

public class Fibers {

}

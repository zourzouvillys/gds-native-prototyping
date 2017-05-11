package io.ewok.fiber;

import kotlin.coroutines.experimental.Continuation;
import kotlin.coroutines.experimental.CoroutineContext;

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

	public static void main(String[] args) {

		final Continuation<Object> cont = new Continuation<Object>() {

			@Override
			public CoroutineContext getContext() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void resume(Object arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void resumeWithException(Throwable arg0) {
				// TODO Auto-generated method stub

			}
		};

		ktest.TestKt.secureAwait(null);

	}

}

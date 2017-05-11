package io.ewok.continuation;

public class Continuations {

	public static void aggregateExceptions(Object... objects) throws AggregateContinuationException {
		// no-op for non async
	}

	public static void cancel(Object...objects) {
		// TODO Auto-generated method stub

	}

	public static ParallelContinuationContext parallelContext() {
		// TODO Auto-generated method stub
		return null;
	}

}

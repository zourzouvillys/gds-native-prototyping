package io.ewok.continuation;

/**
 * When an async method is called, this is passed in to receive the result. To
 * minimise overhead, we don't box/unbox natives.
 */

public abstract class AbstractContinuationImpl implements Continuation {

	private final ContinuationResultReceiver caller;

	protected AbstractContinuationImpl(ContinuationResultReceiver caller) {
		this.caller = caller;
	}

	public abstract boolean invoke();

	@Override
	public void resume() {

	}

	protected boolean returnValue(Object value) {
		return true;
	}

}

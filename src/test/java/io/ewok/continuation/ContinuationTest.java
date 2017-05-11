package io.ewok.continuation;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import io.ewok.continuations.api.async;

public class ContinuationTest {

	private final class MyContinuation extends AbstractBoxedContinuationResultReceiver {

		private int state = 0;
		private boolean pending;
		private Throwable error = null;
		private String value = null;

		@Override
		public void returnWithException(Throwable t) {

			for (final StackTraceElement e : t.getStackTrace()) {

				System.err.println(e);

			}

			this.error = t;
			this.pending = false;
		}

		@Override
		public void returnValue(Object value) {
			this.value = (String) value;
			this.pending = false;
		}

		// start
		boolean resume(int maxsteps) {

			while (this.pending == false && maxsteps-- > 0) {

				this.pending = true;

				try {

					switch (this.state) {

						case 0: {
							this.state = 1;
							func1(this);
							break;
						}

						case 1: {
							if (this.error != null) {
								this.state = 100;
							} else {
								this.state = 2;
								func2(this);
							}
							break;
						}

						case 2: {
							if (this.error != null) {
								this.state = 100;
								this.pending = false;
							} else {
								this.state = 0;
								System.err.println("DONE");
								return true;
							}
							break;
						}

						case 100: {
							System.err.println("ERR");
							return true;
						}

						default: {
							System.err.println("Illegal State: " + this.state);
							return true;
						}

					}

				} catch (final Throwable ex) {
					this.returnWithException(ex);
				}

			}

			return false;

		}

		public String result() throws Throwable {
			if (this.error != null) {
				throw this.error;
			}
			return this.value;
		}

	}

	@Test
	public void test() throws Throwable {

		final MyContinuation c = new MyContinuation();

		while (!c.resume(1)) {
		}

		System.err.println(c.result());

	}

	private static int func1(ContinuationResultReceiver c) {
		System.err.println("A");
		c.returnValue("Hello");
		return 0;
	}

	private static int func2(ContinuationResultReceiver c) throws Exception {
		System.err.println("B");
		// c.returnValue(" World");
		throw new Exception() {
			private static final long serialVersionUID = 1L;
		};
	}

	@async
	private int randomize(int val) {

		return val + (ThreadLocalRandom.current().nextInt() - val);

	}

	@async
	public int moo(final int a, final int b) {

		final int c = this.randomize(a);

		System.err.println(c);

		if (c < 0 && a > 1) {
			return 1;
		}

		if (b > 0 || this.lower(c) || this.randomize(a) != 123) {
			return this.moo(a, b);
		}

		switch (a) {
			case 1:
				return this.randomize(a);
			case 2:
				break;
			case 4:
				// ignore return value.
				this.moo(1, 23);
				System.err.println("XXX");
				this.randomize(1234);
				break;
			default:
				this.lower(c);
				break;
		}

		Integer.toHexString(this.randomize(a + b));

		//
		return Integer.compare(this.randomize(a + b), this.randomize(c));

	}

	@async
	private boolean lower(int c) {
		return false;
	}

}

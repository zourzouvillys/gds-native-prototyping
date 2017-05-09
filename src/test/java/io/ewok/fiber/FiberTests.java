package io.ewok.fiber;

import org.junit.Test;

public class FiberTests {

	@Test
	public void test() {

		final Scheduler scheduler = new Scheduler();

		scheduler.startThread();

		final Fiber fiber = scheduler.allocate(new FiberGuest() {

			@Override
			public void start(Fiber fiber) throws InterruptedException {
				System.err.println("START: " + fiber.now());
				Thread.sleep(1500);
				System.err.println("START DONE: " + fiber.now());
			}

			@Override
			public void signal(Fiber fiber) {
				System.err.println("SIGNAL");
			}

			@Override
			public void reenter(Fiber fiber) {
			}

		});

		for (int i = 0; i < 1_0000; ++i) {
			fiber.schedule();
		}

		final long lastScheduledAt = System.nanoTime();

		for (int i = 0; i < 1_000_000; ++i) {
			fiber.schedule();
		}

		final double running = ((System.nanoTime() - lastScheduledAt) / 1000.0);

		System.err.println(String.format("%,.04f us", running / 1_000_000));

		//fiber.schedule();

		fiber.join();

	}

}

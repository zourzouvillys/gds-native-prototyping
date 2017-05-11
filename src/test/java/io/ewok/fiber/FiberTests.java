package io.ewok.fiber;

import org.junit.Test;

public class FiberTests {

	@Test
	public void test() {

		// GCMointor.installGCMonitoring();

		for (int x = 0; x < 100; ++x) {

			final Scheduler scheduler = new Scheduler();

			scheduler.startThread();

			final Fiber fiber = scheduler.allocate(new TestGuest());

			for (int i = 0; i < 1_0000; ++i) {
				fiber.schedule();
			}

			final long lastScheduledAt = System.nanoTime();

			for (int i = 0; i < 1_000_000; ++i) {
				fiber.schedule();
			}

			final double running = ((System.nanoTime() - lastScheduledAt) / 1000.0);

			System.err.println(String.format("%,.04f us", running / 1_000_000));

			// fiber.schedule();

			fiber.join();

			scheduler.shutdown();

			System.err.println("Shutdown");

		}

	}

}

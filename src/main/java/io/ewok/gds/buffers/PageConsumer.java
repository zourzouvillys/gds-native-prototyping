package io.ewok.gds.buffers;

@FunctionalInterface
public interface PageConsumer {

	/**
	 * Provides a {@link PageRef} to a consumer.
	 *
	 * @param ex
	 *            An exception if the load failed.
	 * @param page
	 *            The requested page.
	 */

	void accept(Throwable th, PageRef page);

}

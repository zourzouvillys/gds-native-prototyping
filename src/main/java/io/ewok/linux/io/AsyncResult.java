package io.ewok.linux.io;

public class AsyncResult {

	public Object attachment;
	public long result;
	public long result2;

	public static  AsyncResult[] allocate(int nr) {
		final AsyncResult[] results = new AsyncResult[nr];
		for (int i = 0; i < nr; ++i) {
			results[i] = new AsyncResult();
		}
		return results;
	}

}

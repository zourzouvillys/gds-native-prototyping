package io.ewok.linux.io;

import com.google.common.base.Preconditions;

import io.ewok.io.BlockAccessCallback;
import io.ewok.io.BlockAccessResult;
import io.ewok.io.PagePointer;
import io.ewok.linux.NativeLinux;

/**
 * Holds results from an IO operation.
 *
 * @author theo
 *
 */

public class AsyncBlockResult implements BlockAccessResult, Runnable {

	public Object attachment;
	public long result;
	public long result2;

	public BlockAccessCallback<?> callback;
	public PagePointer page;

	public static AsyncBlockResult[] allocate(int nr) {
		final AsyncBlockResult[] results = new AsyncBlockResult[nr];
		for (int i = 0; i < nr; ++i) {
			results[i] = new AsyncBlockResult();
		}
		return results;
	}

	@Override
	public PagePointer page() {
		return this.page;
	}

	@Override
	public long length() {
		Preconditions.checkState(this.result >= 0, this.result);
		if (this.result > 0) {
			return this.result;
		}
		return 0;
	}

	@Override
	public Throwable exception() {
		if (this.result < 0) {
			throw new RuntimeException(NativeLinux.strerror((int) this.result));
		}
		return null;
	}

	public <T> T attachment() {
		return (T) this.attachment;
	}

	public void dispatch() {
		try {
			this.callback.complete(this, this.attachment());
		} finally {
			this.attachment = null;
			this.result = -1;
			this.result2 = -1;
			this.callback = null;
			this.page = null;
		}
	}

	@Override
	public void run() {
		this.dispatch();
	}

}

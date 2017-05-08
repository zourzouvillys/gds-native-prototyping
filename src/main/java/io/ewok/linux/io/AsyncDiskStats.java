package io.ewok.linux.io;

import java.util.concurrent.atomic.AtomicLong;

public class AsyncDiskStats {

	AtomicLong read_ops = new AtomicLong(0);
	AtomicLong write_ops = new AtomicLong(0);

	AtomicLong read_bytes = new AtomicLong(0);
	AtomicLong write_bytes = new AtomicLong(0);

	AtomicLong success = new AtomicLong(0);
	AtomicLong errors = new AtomicLong(0);

	AtomicLong flushes = new AtomicLong(0);

	AtomicLong pending = new AtomicLong(0);
	AtomicLong buffered = new AtomicLong(0);

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("");
		sb.append("reads { ops:").append(this.read_ops.get()).append(", bytes:").append(this.read_bytes.get()).append(" }");
		sb.append(", writes { ops:").append(this.write_ops.get()).append(" bytes:").append(this.write_bytes.get()).append(" }");
		sb.append(", success:").append(this.success.get());
		sb.append(", errors:").append(this.errors.get()).append("");
		sb.append(", flushes:").append(this.flushes.get());
		sb.append(", pending:").append(this.pending.get());
		sb.append(", buffered:").append(this.buffered.get());
		return sb.toString();
	}

}

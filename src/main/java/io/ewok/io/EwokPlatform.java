package io.ewok.io;

import io.ewok.linux.io.AlignedByteBufPool;
import io.ewok.linux.io.LinuxBlockFiles;

public final class EwokPlatform {

	private static final LinuxBlockFiles FS = new LinuxBlockFiles();

	public static final BlockSystem fs() {
		return FS;
	}

	public static final PageBufferAllocator createPool(int pageSize, int pageCount) {
		return new AlignedByteBufPool(pageSize, pageCount);
	}

}

package io.ewok.io;

import io.ewok.linux.io.LinuxBlockFileHandle;

public interface WriteBlockFileHandle extends BlockFileHandle {

	WriteBlockFileHandle preallocate(long bytes);

	WriteBlockFileHandle flush();

	LinuxBlockFileHandle truncate(long length);

}

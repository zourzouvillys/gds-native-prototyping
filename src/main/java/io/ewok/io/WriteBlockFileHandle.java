package io.ewok.io;

public interface WriteBlockFileHandle extends BlockFileHandle {

	WriteBlockFileHandle preallocate(long bytes);

	WriteBlockFileHandle flush();

}

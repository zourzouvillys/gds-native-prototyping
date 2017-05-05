package io.ewok.gds.storage;

import java.nio.file.Path;

public class DefaultStorageManager implements StorageManager {

	private final Path datadir;

	public DefaultStorageManager(Path base) {
		this.datadir = base;
	}

	@Override
	public StorageRelation open(long objid) {
		return new DiskStorageRelation(this.datadir, objid);
	}

}

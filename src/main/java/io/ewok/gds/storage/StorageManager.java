package io.ewok.gds.storage;

public interface StorageManager {

	/**
	 * Opens a storage relation for use.
	 *
	 * @param objid
	 *            The object ID to open the storage manager for.
	 *
	 * @return
	 */

	StorageRelation open(long objid);

}

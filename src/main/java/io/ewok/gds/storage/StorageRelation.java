package io.ewok.gds.storage;

import io.ewok.gds.buffers.pages.PageFork;

/**
 * Direct storage access.
 *
 * Instances of this interface do not use the buffer manager. Consumers should
 * always use the buffer manager for accessing pages (except for recovery
 * tools).
 *
 * @author theo
 *
 */

public interface StorageRelation extends StorageEntity {

	/**
	 * Fetch the given fork.
	 */

	StorageRelationFork fork(PageFork fork);

}

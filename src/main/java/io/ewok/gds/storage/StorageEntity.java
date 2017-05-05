package io.ewok.gds.storage;

import java.util.concurrent.CompletableFuture;

/**
 * {@link StorageRelation} and {@link StorageRelationFork} both support common
 * methods. This interface provides them.
 *
 * @author theo
 *
 */

public interface StorageEntity {

	/**
	 *
	 */

	CompletableFuture<?> sync();

	/**
	 *
	 */

	CompletableFuture<?> unlink();

	/**
	 *
	 */

	CompletableFuture<?> close();

}

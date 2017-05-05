package io.ewok.gds.txn.impl;

import java.util.Set;

import lombok.Value;

/**
 * transaction snapshot information.
 *
 * @author theo
 *
 */

@Value
public class TXShot {

	/**
	 * The largest txnid before which all have been completed (either aborted or
	 * committed).
	 */

	private int head;

	/**
	 * A list of active transactions at the time this snapshot was started.
	 */

	private Set<Integer> active;

}

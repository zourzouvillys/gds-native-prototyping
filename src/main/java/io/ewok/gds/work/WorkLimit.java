package io.ewok.gds.work;

import java.time.Duration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WorkLimit {

	/**
	 * relative priority (against all other work).
	 */

	private int priority;

	/**
	 * max duration of the work (wall time).
	 */

	private final Duration wallTimeLimit;

	/**
	 * total cpu time allowed (including context switches).
	 */

	private final Duration cpuTimeLimit;

	/**
	 * max wall clock duration for any single transaction.
	 */

	private final Duration txnTimeLimit;

	/**
	 * maximum number of transactions. set to zero to make work context read
	 * only.
	 */

	private int numTransactions;

	/**
	 * max number of commands in a single txn.
	 */

	private int commandsPerTxn;

	/**
	 * Number of WAL records that can be generated.
	 */

	private int journalEntries;

	/**
	 * max number of bytes allowed to be written to the WAL.
	 */

	private int journalBytes;

	/**
	 * a flushing of the journal.
	 */

	private int journalFlush;

	/**
	 * amount of memory allocated.
	 */

	private final int workingMemory;

	/**
	 * amount of temporary disk space allowed.
	 */

	private final int tempStorageBytes;

	/**
	 * read IO operations per second allowed.
	 */

	private final int diskReadOpsPerSecond;

	/**
	 * write IO operations per second allowed.
	 */

	private final int diskOpsPerSecond;

	/**
	 * network read mbits/sec allowed.
	 */

	private final int netReadBPS;

	/**
	 * network write mbits/sec allowed.
	 */

	private final int netWriteBPS;

	/**
	 * limit of read io operations.
	 */

	private final int diskReadOpCount;

	/**
	 * limit of write io operations.
	 */

	private final int diskWriteOpCount;

	/**
	 * limit on the number of network MB read.
	 */

	private final int netWriteBytes;

	/**
	 * limit on the number of network MBs written.
	 */

	private final int netReadBytes;

	/**
	 * number of new pages allowed to be created.
	 */

	private final int newPages;

	/**
	 * number of pages allowed to be modified.
	 */

	private final int pageWrites;

	/**
	 * number of pages allowed to be read.
	 */

	private final int pageReads;

	/**
	 * max number of tuples that can be added
	 */

	private final int tupleInserts;

	/**
	 * max number of tuples that can be updated.
	 */

	private final int tupleUpdates;

	/**
	 * max number of tuples which can be deleted.
	 */

	private final int tupleDeletes;

}

package io.ewok.gds.work;

public enum WorkLimitUnit {

	/**
	 * A WAL journal entry.
	 */

	JournalRecords,

	/**
	 * Bytes within the WAL journal.
	 */

	JournalBytes,

	/**
	 * A WAL journal flush
	 */

	JournalFlush

}

package io.ewok.gds.journal;

public class JournalFileUtils {

	/**
	 * Maximum length of a journal entry is 128k.
	 *
	 * A journal entry must fit within a single segment.
	 *
	 */

	public static final int JOURNAL_ENTRY_MAX_BYTES = (1024 * 128);

	/**
	 * The number of bytes in each segment. The start of the segment is always
	 * the start of a journal record, and then they are back to back from there.
	 */

	public static final int JOURNAL_SEGMENT_BYTES = 16_384_000;

	/**
	 * calculate file name for the segment that contains the given position.
	 */

	public static String segment(int timeline, int segmentSize, long position) {
		// [timeline: 0000 0001 ] [ offset: 0000 0000 0000 0000 ]
		return String.format("%08X%016X", timeline, base(segmentSize, position));
	}

	/**
	 * given a journal offset, returns the base segment offset.
	 */

	public static long base(int segmentSize, long position) {
		return position - (position % segmentSize);
	}

	public static int segmentOffset(int segmentSize, long position) {
		return (int) (position % segmentSize);
	}

}

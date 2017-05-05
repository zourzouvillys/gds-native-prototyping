package io.ewok.gds.journal;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import io.ewok.gds.storage.AsyncFileIO;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * WAL/Journal page manager.
 *
 * @author theo
 *
 */

public class JournalWriter {

	// path to the segments
	private final Path folder;

	@Getter
	private final int timeline;

	@Getter
	private final int blockSize = 8192; // 8K

	@Getter
	private final int segmentSize = 16_384_000; // 16MB

	/**
	 * the position in the journal where our next write will be.
	 */

	private long nextPosition = -1;

	/**
	 *
	 */

	private AsyncFileIO segment;

	private PagePool pages;

	/**
	 *
	 * @param folder
	 *            The folder that the WAL segments will be stored in.
	 */

	public JournalWriter(Path folder, int timeline) {
		this.folder = folder;
		this.timeline = timeline;
	}

	/**
	 * Open the journal writer for appending at the given offset. The segment
	 * must already exist.
	 */

	@SneakyThrows
	public void open(long nextPosition) {
		final Path segment = this.folder
				.resolve(JournalFileUtils.segment(this.timeline, this.segmentSize, nextPosition));
		this.segment = AsyncFileIO.open(segment).get();
		this.pages = new PagePool(this.blockSize, this.segment);
		this.pages.seek(JournalFileUtils.segmentOffset(this.segmentSize, nextPosition)).get();
		this.nextPosition = nextPosition;
	}

	/**
	 * Open the journal writer for appending at the given offset, creating a new
	 * journal file.
	 */

	@SneakyThrows
	public void create(long nextPosition) {
		final Path segment = this.folder
				.resolve(JournalFileUtils.segment(this.timeline, this.segmentSize, nextPosition));
		this.segment = AsyncFileIO.create(segment, this.segmentSize).get();
		this.pages = new PagePool(this.blockSize, this.segment);
		this.pages.seek(0).get();
		this.nextPosition = nextPosition;
	}

	/**
	 * Append the given bytes to the journal. They will be provided back as a
	 * single buffer.
	 */

	public long append(byte[] record) {

		Preconditions.checkArgument(record.length > 0);
		Preconditions.checkArgument(record.length <= JournalFileUtils.JOURNAL_ENTRY_MAX_BYTES);

		// variable length, excluding checksum.
		final int vulen = this.pages.writeVarUInt32(record.length);

		// TODO: make sure we have enough room in this segment, otherwise we
		// need to rotate
		// final int full = vulen + record.length + 2;

		// calculate checksum
		final Hasher hasher = Hashing.crc32c().newHasher();
		hasher.putBytes(record);
		this.pages.writeShort((hasher.hash().asInt() % 65535) + 1);
		this.pages.writeBytes(record);

		this.nextPosition += record.length + vulen + 2;

		return this.nextPosition;

	}

	/**
	 * appends the provided buffer to the journal, without flushing. However, a
	 * page which is full will have a write issued.
	 *
	 * the record will be prefixed with the record size and a checksum. The
	 * "next journal offset" will be returned.
	 *
	 * this writes to the current page buffer.
	 *
	 */

	public long append(ByteBuf record) {

		Preconditions.checkArgument(record.isReadable());
		Preconditions.checkArgument(record.readableBytes() <= JournalFileUtils.JOURNAL_ENTRY_MAX_BYTES);

		// variable length, excluding checksum.
		final int vulen = this.pages.writeVarUInt32(record.readableBytes());

		// calculate checksum
		final Hasher hasher = Hashing.crc32c().newHasher();
		record.forEachByte(b -> {
			hasher.putByte(b);
			return true;
		});

		this.pages.writeShort((hasher.hash().asInt() % 65535) + 1);
		this.pages.writeBytes(record);

		this.nextPosition += record.readableBytes() + vulen + 2;

		return this.nextPosition;

	}

	public CompletableFuture<?> flush() {
		return this.pages.flush();
	}

}

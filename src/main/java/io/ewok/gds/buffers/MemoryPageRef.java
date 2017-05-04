package io.ewok.gds.buffers;

import java.util.Optional;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;

/**
 * A single page loaded into memory.
 *
 * Any modifications to the page are done within a transaction, as well as
 * appending a WAL entry. A page will never be written to disk until it has
 * flushed up until the most recent WAL entry which corresponds to the
 * modification to this page.
 *
 *
 *
 *
 * <pre>
 * uint16 checksum = XXXX;
 * uint16 freeStart = X;
 * uint16 freeEnd = Y;
 * uint16 specialOffset = Z;
 * uint8 flags = 0;
 * uint8 versionLayout = 1;
 * uint16 pageSize = 8192;
 * </pre>
 *
 *
 * @author theo
 */

public class MemoryPageRef implements PageRef {

	private ByteBuf page;
	private final int objid;
	private final int pageno;

	public MemoryPageRef(ByteBuf page, int objid, int pageno, Optional<ByteBuf> special) {

		this.page = page;

		this.objid = objid;
		this.pageno = pageno;

		// freeStart
		this.page.setShort(2, 12);

		// specialOffset
		if (special.isPresent()) {

			final ByteBuf trailer = special.get();

			final int len = trailer.readableBytes();

			// freeEnd
			this.page.setShort(4, (short) (8192 - len));

			// special offset
			this.page.setShort(6, (short) (8192 - len));

			this.page.setBytes(8192 - len, trailer);

		} else {
			// freeEnd
			this.page.setShort(4, (short) (8192));
			// special
			this.page.setShort(6, (short) 0);
		}

		// flags
		this.page.setByte(8, (byte) (0));

		// version layout
		this.page.setByte(9, (byte) 1);

		// pageSize
		this.page.setShort(10, (short) 8192);

	}

	@Override
	public long lsn() {
		return -1;
	}

	@Override
	public int objid() {
		return this.objid;
	}

	@Override
	public int pageno() {
		return this.pageno;
	}

	/**
	 * Number of tuples in this page.
	 *
	 * @return
	 */
	private int tuples() {
		return (this.freeStart() - 12) / 4;
	}

	private int checksum() {
		return this.page.getShort(0);
	}

	private int freeStart() {
		return this.page.getShort(2);
	}

	private int freeEnd() {
		return this.page.getShort(4);
	}

	private int specialOffset() {
		return this.page.getShort(6);
	}

	private int flags() {
		return this.page.getByte(8);
	}

	private int versionLayout() {
		return this.page.getByte(9);
	}

	private int pageSize() {
		return this.page.getShort(10);
	}

	/**
	 * how much free space there is in this page.
	 */

	@Override
	public int freespace() {
		return this.freeEnd() - this.freeStart();
	}

	private int indexOffset(int tuple) {
		return 12 + (tuple * 4);
	}

	/**
	 * mutate the page to add this entry.
	 */

	@Override
	public int append(ByteBuf tuple) {

		// xmin (uint32) - insert XID
		// xmax (uint32) - delete XID
		// ... data.

		final int tuples = this.tuples();

		final int len = tuple.readableBytes();

		Preconditions.checkState((len + 8) <= this.freespace());

		// write at the end
		this.page.writerIndex(this.freeEnd() - (len + 8));
		this.page.writeInt(0);
		this.page.writeInt(0);
		this.page.writeBytes(tuple);

		// add into the index
		this.page.setShort(this.indexOffset(tuples), (short) (this.freeEnd() - (len + 8)));
		this.page.setShort(this.indexOffset(tuples) + 2, (short) (len + 8));

		// freeStart
		this.page.setShort(2, (short) this.indexOffset(tuples + 1));

		// freeEnd
		this.page.setShort(4, (short) (this.freeEnd() - (len + 8)));

		return tuples;
	}

	/**
	 * fetch the tuple with the given offset.
	 */

	@Override
	public ByteBuf tuple(int index) {

		final int offset = this.page.getShort(this.indexOffset(index));
		final int len = this.page.getShort(this.indexOffset(index) + 2);

		final int xmin = this.page.getInt(offset);
		final int xmax = this.page.getInt(offset + 4);

		final ByteBuf slice = this.page.slice(offset + 4, len).clear();

		return slice;
	}

	/**
	 *
	 */

	@Override
	public Optional<ByteBuf> special() {

		if (this.specialOffset() == 0) {
			return Optional.empty();
		}

		return Optional.of(this.page.slice(this.specialOffset(), 8192 - this.specialOffset()).clear());

	}

	@Override
	public void special(ByteBuf special) {
		this.page.setBytes(this.specialOffset(), special);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(String.format("[MEM-PAGE:%s/%s]", this.objid(), this.pageno()));
		sb.append(" freespace=").append(this.freespace()).append(" (").append(this.freeStart()).append("->")
		.append(this.freeEnd()).append(")");
		sb.append(" checksum=").append(this.checksum());
		sb.append(" tuples=").append(this.tuples());
		sb.append(" flags=").append(this.flags());
		sb.append(" versionLayout=").append(this.versionLayout());
		sb.append(" pageSize=").append(this.pageSize());
		sb.append(" specialAt=").append(this.specialOffset());
		return sb.toString();
	}

	/**
	 * Used by the {@link DefaultBufferManager} when the page is being unlinked,
	 * so no further usage of this page is allowed.
	 */

	public void unlink() {
		this.page = null;
	}

}

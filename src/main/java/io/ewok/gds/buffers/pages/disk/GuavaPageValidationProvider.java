package io.ewok.gds.buffers.pages.disk;

import com.google.common.base.Preconditions;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;

import io.ewok.gds.buffers.pages.PageId;
import io.netty.buffer.ByteBuf;

public class GuavaPageValidationProvider implements PageValidationProvider {

	// the offset in the page where the checksum is.
	private static final int CHECKSUM_OFFSET = 2;

	private final HashFunction function;

	public GuavaPageValidationProvider(HashFunction function) {
		this.function = function;
	}

	/**
	 * calculate & set the checksum.
	 */

	@Override
	public void update(ByteBuf buffer, PageId pageId) {
		buffer.setInt(CHECKSUM_OFFSET, this.calculate(buffer, pageId));
	}

	/**
	 * validate the checksum of the page.
	 */

	@Override
	public boolean validate(ByteBuf buffer, PageId pageId) {
		return (buffer.getShort(CHECKSUM_OFFSET) == this.calculate(buffer, pageId));
	}

	/**
	 * perform checksum for validate & update.
	 *
	 * @param buffer
	 * @param pageId
	 * @return
	 */

	private short calculate(ByteBuf buffer, PageId pageId) {

		final int pageVersion = PageUtils.versionFromLayout(buffer.getByte(0));

		// safety check to make sure we have the expected page version.
		Preconditions.checkArgument((PageUtils.versionFromLayout(buffer.getByte(0)) == 1), "unknown version", pageVersion);

		// make sure the page is all here.
		final int pageSize = PageUtils.pageSizeFromLayout(buffer.getByte(0));

		// should have a full page.
		Preconditions.checkArgument(buffer.capacity() == pageSize);

		// perform a page checksum calculation
		final Hasher hash = this.function.newHasher();

		// now calculate over the rest of the page.
		buffer.forEachByte(0, CHECKSUM_OFFSET, b -> {
			hash.putByte(b);
			return true;
		});

		// put 2 bytes of 0, in place of where the checksum field is. this
		// ensures other implementations can easily calculate by setting the
		// checksum field to zeros, then calculating over the whole block.
		hash.putShort((short) 0);

		// now calculate over the rest of the page.
		buffer.forEachByte(CHECKSUM_OFFSET + 2, pageSize - (CHECKSUM_OFFSET + 2), b -> {
			hash.putByte(b);
			return true;
		});

		// include the page ID.
		hash.putLong(pageId.getPageId());

		// we don't include the object id, as we want to be bale to change it
		// without rewriting all of the pages.

		// calculate, ensuring never 0.
		return (short) ((hash.hash().asInt() % 65535) + 1);

	}

}

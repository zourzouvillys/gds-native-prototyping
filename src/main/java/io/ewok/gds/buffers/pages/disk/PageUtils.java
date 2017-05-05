package io.ewok.gds.buffers.pages.disk;

import com.google.common.base.Preconditions;

import io.ewok.gds.buffers.pages.PageId;
import io.netty.buffer.ByteBuf;

public class PageUtils {

	/**
	 * Perform some basic integrity checks.
	 *
	 * These could be disabled, but allow early detection of development errors
	 * - so for now they are used on every page write and load.
	 *
	 * @param buffer
	 *
	 */

	/*
| 0  | layout    | uint8  | first 4 bits are version (currently always 1). |
|    |           |        | last 4 bits are page size. see page size table above. |
| 1  | flags     | uint8  | flag bits. see flag table below. |
| 2  | checksum  | uint16 | page checksum |
| 4  | prune_xid | uint32 | oldest unpruned xmax on page, or zero if none. |
| 8  | lsn       | uint64 | next byte in WAL after last write to this page |
| 16 | lower     | uint16 | offset to start of free space |
| 18 | upper     | uint16 | offset to end of free space |
| 20 | special   | uint16 | offset to special space (may be zero) |

	 */
	public static void checkIntegrity(ByteBuf page, PageId pageId) throws IllegalArgumentException {

		final int freeStart = page.getUnsignedShort(16);
		final int freeEnd = page.getUnsignedShort(18);
		final int special = page.getUnsignedShort(20);
		final int flags = page.getUnsignedByte(1);

		final byte layout = page.getByte(0);

		final int version = versionFromLayout(layout);

		Preconditions.checkArgument(version == 1, "version", version, layout);

		final int pageSize = pageSizeFromLayout(layout);

		Preconditions.checkArgument(
				(page.capacity() == pageSize),
				"buffer != pageSize",
				page.readableBytes(),
				pageSize);

		Preconditions.checkArgument(freeStart <= freeEnd, "freeEnd > freeStart", freeStart, freeEnd);
		Preconditions.checkArgument(freeStart >= 22, "freestart", freeStart);

		Preconditions.checkArgument(freeEnd <= pageSize, "freeEnd <= pageSize", freeEnd, pageSize);

		if (special != 0) {
			Preconditions.checkArgument(freeEnd <= special, "freeEnd > special", freeEnd, special);
			Preconditions.checkArgument(special > freeEnd, "special <= freeEnd", special, freeEnd);
		}

		Preconditions.checkArgument(flags == 0, "flags != 0", flags);

		// run through each item header.

	}

	public static int versionFromLayout(byte layout) {
		return (layout & 0xFF) >> 4;
	}

	public static int pageSizeFromLayout(byte layout) {
		switch (layout & 15) {
			case 0:
				return 512;
			case 1:
				return 1024;
			case 2:
				return 2048;
			case 3:
				return 4096;
			case 4:
				return 8192;
			case 5:
				return 8192 * 2;
			case 6:
				return 8192 * 4;
			case 7:
				return 8192 * 8;
		}
		throw new RuntimeException();
	}

	public static byte layout(int layoutVersion, int pageSize) {

		// 0: 512 == 0000 0010 0000 0000
		// 1: 1024 == 0000 0100 0000 0000
		// 2: 2048 == 0000 1000 0000 0000
		// 3: 4096 == 0001 0000 0000 0000
		// 4: 8192 == 0010 0000 0000 0000
		// 4: 16xxx == 0100 0000 0000 0000
		// 4: 32xxx == 1000 0000 0000 0000
		// 4: 64xxx [1] 0000 0000 0000 0000

		switch (pageSize) {
			case 512:
				pageSize = 0;
				break;
			case 1024:
				pageSize = 1;
				break;
			case 2048:
				pageSize = 2;
				break;
			case 4096:
				pageSize = 3;
				break;
			case 8192:
				pageSize = 4;
				break;
			case 8192 * 2:
				pageSize = 5;
				break;
			case 8192 * 4:
				pageSize = 6;
				break;
			case 8192 * 8:
				pageSize = 7;
				break;
			default:
				throw new IllegalArgumentException("Invalid page size");

		}

		return (byte) ((layoutVersion << 4) ^ (pageSize));

	}

}

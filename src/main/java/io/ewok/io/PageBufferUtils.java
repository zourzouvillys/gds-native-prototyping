package io.ewok.io;

public class PageBufferUtils {

	private static final byte[] PATTERN_CHARS = {
			'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
	};

	public static PagePointer pattern(PageBuffer page) {
		for (int i = 0; i < page.pageSize(); ++i) {
			page.setByte(i, PATTERN_CHARS[i % PATTERN_CHARS.length]);
		}
		return page;
	}

}

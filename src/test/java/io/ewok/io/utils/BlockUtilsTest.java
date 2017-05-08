package io.ewok.io.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BlockUtilsTest {

	@Test
	public void test() {

		assertEquals(256, BlockUtils.pagesForSize(4096, 1, BlockSizeUnits.MB));

		assertEquals(0, BlockUtils.offsetInPage(0, 4096));
		assertEquals(10, BlockUtils.offsetInPage(10, 4096));
		assertEquals(0, BlockUtils.offsetInPage(4096, 4096));
		assertEquals(1, BlockUtils.offsetInPage(4097, 4096));

	}

}

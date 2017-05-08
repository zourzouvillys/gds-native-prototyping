package io.ewok.linux;

public class MemoryTests {

	public void testHugePageAllocation() {

		final long hugesize = HugePages.defaultHugePageSize().orElse(0L);

		final long addr = JLinux.mmap(
				0,
				hugesize,
				JLinux.PROT_READ | JLinux.PROT_WRITE,
				JLinux.MAP_ANONYMOUS | JLinux.MAP_PRIVATE | JLinux.MAP_HUGETLB | JLinux.MAP_LOCKED,
				-1, 0);

		JLinux.munmap(addr, hugesize);

	}

}

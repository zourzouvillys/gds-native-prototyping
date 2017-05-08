package io.ewok.linux;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import lombok.Builder;
import lombok.Value;

public class HugePages {

	/**
	 *
	 * @author theo
	 *
	 */

	@Value
	@Builder
	public static final class Pool {

		private final long size;

		private final long free_hugepages;
		private final long nr_hugepages;
		private final long nr_hugepages_mempolicy;
		private final long nr_overcommit_hugepages;
		private final long resv_hugepages;
		private final long surplus_hugepages;

		// private final long nr_over;
		// private final long nr_used;
		// private final long nr_supr;
		// private final long nr_resv;
		// private final long nr_static;
	}

	private static final Path SYSFS_HUGEPAGES_DIR = Paths.get("/sys/kernel/mm/hugepages/");

	public static long[] sizes() {

		try (final DirectoryStream<Path> stream = Files.newDirectoryStream(SYSFS_HUGEPAGES_DIR, "hugepages-*kB")) {
			final Set<Long> sizes = Sets.newHashSet();
			for (final Path path : stream) {
				sizes.add(Long.parseLong(path.getFileName().toString().substring(10).replace("kB", "")) * 1024);
			}
			return sizes.stream().mapToLong(val -> val).toArray();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

	}

	private static final Path hugedir(long size) {
		return SYSFS_HUGEPAGES_DIR.resolve(String.format("hugepages-%dkB", size / 1024));
	}

	public static Set<Path> mountpoints() {
		return ProcFS.mounts().type("hugetlbfs").stream().map(p -> Paths.get(p.getMountpoint()))
				.collect(Collectors.toSet());
	}

	/**
	 *
	 */

	public static Optional<Long> defaultHugePageSize() {
		return ProcFS.meminfo().hugepagesize();
	}

	/**
	 *
	 */

	public static void read() {

		for (final long size : sizes()) {

			// free_hugepages nr_hugepages nr_overcommit_hugepages
			// resv_hugepages surplus_hugepages

			final Pool pool = Pool.builder()
					.size(size)
					.free_hugepages(LinuxUtils.longval(hugedir(size).resolve("free_hugepages")).orElse(-1L))
					.nr_hugepages(LinuxUtils.longval(hugedir(size).resolve("nr_hugepages")).orElse(-1L))
					.nr_hugepages_mempolicy(
							LinuxUtils.longval(hugedir(size).resolve("nr_hugepages_mempolicy")).orElse(-1L))
					.nr_overcommit_hugepages(
							LinuxUtils.longval(hugedir(size).resolve("nr_overcommit_hugepages")).orElse(-1L))
					.resv_hugepages(LinuxUtils.longval(hugedir(size).resolve("resv_hugepages")).orElse(-1L))
					.surplus_hugepages(LinuxUtils.longval(hugedir(size).resolve("surplus_hugepages")).orElse(-1L))
					.build();

			System.err.println(pool);
		}
	}

}

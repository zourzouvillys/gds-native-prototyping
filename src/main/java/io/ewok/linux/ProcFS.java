package io.ewok.linux;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

import lombok.SneakyThrows;

public class ProcFS {

	private static final Path PROC_PATH = Paths.get("/proc");

	@SneakyThrows
	public static long longval(String key) {
		final Path path = PROC_PATH.resolve(key);
		final BufferedReader br = Files.newBufferedReader(path, StandardCharsets.US_ASCII);
		try {
			return Long.parseLong(br.readLine());
		} finally {
			br.close();
		}
	}

	public static long sys_fs_aiomaxnr() {
		return ProcFS.longval("sys/fs/aio-max-nr");
	}

	private static final Path PROC_MEMINFO_PATH = PROC_PATH.resolve("meminfo");

	public static final MemInfo meminfo() {
		try (final BufferedReader br = Files.newBufferedReader(PROC_MEMINFO_PATH, StandardCharsets.US_ASCII)) {
			return new MemInfo(br.lines()
					.map(x -> x.split(":[ ]*", 2))
					.map((String[] a) -> Maps.immutableEntry(a[0], a.length == 2 ? a[1].trim() : ""))
					.collect(Collectors.toList()));
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}



	private static final Path PROC_MOUNTS_PATH = PROC_PATH.resolve("mounts");

	public static final Mounts mounts() {
		try (final BufferedReader br = Files.newBufferedReader(PROC_MOUNTS_PATH, StandardCharsets.US_ASCII)) {
			return new Mounts(br.lines()
					.map(x -> x.split("[ ]+"))
					.map(Mounts.Entry::new)
					.collect(Collectors.toList()));
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}

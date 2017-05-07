package io.ewok.linux;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

}

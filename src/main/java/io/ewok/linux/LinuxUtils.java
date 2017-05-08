package io.ewok.linux;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import lombok.SneakyThrows;

public final class LinuxUtils {

	@SneakyThrows
	public static final Optional<Long> longval(Path path) {
		try (final BufferedReader br = Files.newBufferedReader(path, StandardCharsets.US_ASCII)) {
			return Optional.of(Long.parseLong(br.readLine()));
		} catch (final Exception ex) {
			return Optional.empty();
		}
	}

	public static final long sizeToSmallerUnit(long size) {
		if ((size * 1024) < size) {
			// overflow
			return -1;
		}
		return size * 1024;
	}

	public static long parseByteCount(String value) {
		if (value.toUpperCase().endsWith("KB")) {
			return Long.parseLong(value.substring(0, value.length() - 2).trim()) * 1024;
		}
		else if (value.toUpperCase().endsWith("MB")) {
			return Long.parseLong(value.substring(0, value.length() - 2).trim()) * 1024 * 1024;
		}
		else if (value.toUpperCase().endsWith("GB")) {
			return Long.parseLong(value.substring(0, value.length() - 2).trim()) * 1024 * 1024 * 1024;
		}
		return Long.parseLong(value.trim());
	}

}

package io.ewok.linux;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class MemInfo {

	private final Map<String, String> entries = new TreeMap<>((a, b) -> a.toLowerCase().compareTo(b.toLowerCase()));

	MemInfo(List<Map.Entry<String, String>> list) {
		list.forEach(e -> this.entries.put(e.getKey(), e.getValue()));
	}

	@Override
	public String toString() {
		return this.entries.toString();
	}

	public Optional<Long> hugepagesize() {
		final String value = this.entries.get("hugepagesize");
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(LinuxUtils.parseByteCount(value));
	}

}

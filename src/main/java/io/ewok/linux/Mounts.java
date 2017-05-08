package io.ewok.linux;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.ToString;

public class Mounts {


	private final List<Entry> entries;

	public Mounts(List<Entry> entries) {
		this.entries = entries;
	}

	@Getter
	@ToString
	public static class Entry {

		private final String device;
		private final String mountpoint;
		private final String type;
		private final List<String> options;

		Entry(String[] entry) {
			this.device = entry[0];
			this.mountpoint = entry[1];
			this.type = entry[2];
			this.options = Lists.newArrayList(entry[3].split(","));
		}

	}

	public List<Entry> type(String type) {
		return this.entries.stream().filter(p -> p.type.equals(type)).collect(Collectors.toList());
	}

	public List<Entry> device(String device) {
		return this.entries.stream().filter(p -> p.device.equals(device)).collect(Collectors.toList());
	}

	public List<Entry> mountpoint(String mountpoint) {
		return this.entries.stream().filter(p -> p.mountpoint.equals(mountpoint)).collect(Collectors.toList());
	}

	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder();

		this.entries.forEach(entry -> sb.append(entry.toString()).append("\n"));


		return sb.toString();
	}


}

package io.ewok.io.utils;

public enum BlockSizeUnits {

	KB, MB, GB, TB, PB;

	public long bytes(long value) {
		switch (this) {
			case KB:
				return value * 1024;
			case MB:
				return value * 1024 * 1024;
			case GB:
				return value * 1024 * 1024 * 1024;
			case TB:
				return value * 1024 * 1024 * 1024 * 1024;
			case PB:
				return value * 1024 * 1024 * 1024 * 1024 * 1024;
		}
		throw new RuntimeException(this.toString());
	}

}

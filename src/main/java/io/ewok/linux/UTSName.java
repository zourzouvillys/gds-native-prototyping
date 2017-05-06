package io.ewok.linux;

import com.sun.jna.Memory;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UTSName {

	private final String sysName;
	private final String nodeName;
	private final String release;
	private final String version;
	private final String machine;
	private final String domainName;

	public UTSName(Memory mem) {
		this.sysName = mem.getString(0);
		this.nodeName = mem.getString(65 * 1);
		this.release = mem.getString(65 * 2);
		this.version = mem.getString(65 * 3);
		this.machine = mem.getString(65 * 4);
		this.domainName = mem.getString(65 * 5);
	}

}

package io.ewok.linux;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class SchedAttr extends Structure {

	public int size;
	public int policy;
	public long flags;
	public int nice;
	public int priority;
	public long runtime;
	public long deadline;
	public long period;

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("size", "policy", "flags", "nice", "priority", "runtime", "deadline", "period");
	}

}

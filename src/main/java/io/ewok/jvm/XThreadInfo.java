package io.ewok.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * Mointors JVM threads.
 *
 * @author theo
 *
 */

public class XThreadInfo {

	private final int sampleTime = 10000;
	private final ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
	private final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
	private final OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();
	private final Map<Long, Long> threadInitialCPU = new HashMap<>();
	private final Map<Long, Float> threadCPUUsage = new HashMap<>();
	private final long initialUptime = this.runtimeMxBean.getUptime();

	void run() {

		ThreadInfo[] threadInfos = this.threadMxBean.dumpAllThreads(true, false);

		for (final StackTraceElement[] e : Thread.getAllStackTraces().values()) {
			for (final StackTraceElement a : e) {
				System.err.println(a);
			}
			System.err.println();
		}
		// forEach((k, v) -> System.err.println(v.length));

		for (final ThreadInfo info : threadInfos) {

			System.err.println(info);
			// info.getStackTrace().length);
			this.threadInitialCPU.put(info.getThreadId(), this.threadMxBean.getThreadCpuTime(info.getThreadId()));

		}

		try {
			Thread.sleep(this.sampleTime);
		} catch (final InterruptedException e) {
		}

		final long upTime = this.runtimeMxBean.getUptime();

		final Map<Long, Long> threadCurrentCPU = new HashMap<>();
		threadInfos = this.threadMxBean.dumpAllThreads(false, false);
		for (final ThreadInfo info : threadInfos) {
			threadCurrentCPU.put(info.getThreadId(), this.threadMxBean.getThreadCpuTime(info.getThreadId()));
		}

		// CPU over all processes
		// int nrCPUs = osMxBean.getAvailableProcessors();
		// total CPU: CPU % can be more than 100% (devided over multiple cpus)
		final long nrCPUs = 1;
		// elapsedTime is in ms.
		final long elapsedTime = (upTime - this.initialUptime);
		for (final ThreadInfo info : threadInfos) {
			// elapsedCpu is in ns
			final Long initialCPU = this.threadInitialCPU.get(info.getThreadId());
			if (initialCPU != null) {
				final long elapsedCpu = threadCurrentCPU.get(info.getThreadId()) - initialCPU;
				final float cpuUsage = elapsedCpu / (elapsedTime * 1000000F * nrCPUs);
				this.threadCPUUsage.put(info.getThreadId(), cpuUsage);
			}
		}

		// threadCPUUsage contains cpu % per thread
		System.out.println(this.threadCPUUsage);
		// You can use osMxBean.getThreadInfo(theadId)

	}

	public static void main(String[] args) {
		new XThreadInfo().run();
	}

}

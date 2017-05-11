package io.ewok.jvm;

import java.lang.management.GarbageCollectorMXBean;
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import com.sun.management.GarbageCollectionNotificationInfo;

public class GCMointor {

	public static void installGCMonitoring() {

		// get all the GarbageCollectorMXBeans - there's one for each heap
		// generation
		// so probably two - the old generation and young generation

		final List<GarbageCollectorMXBean> gcbeans = java.lang.management.ManagementFactory
				.getGarbageCollectorMXBeans();

		// ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE;

		// Install a notifcation handler for each bean
		for (final GarbageCollectorMXBean gcbean : gcbeans) {

			System.out.println(gcbean);

			final NotificationEmitter emitter = (NotificationEmitter) gcbean;

			// use an anonymously generated listener for this example
			// - proper code should really use a named class
			final NotificationListener listener = new NotificationListener() {
				// keep a count of the total time spent in GCs
				long totalGcDuration = 0;

				// implement the notifier callback handler
				@SuppressWarnings("restriction")
				@Override
				public void handleNotification(Notification notification, Object handback) {
					System.err.println(notification.getType());
					// we only handle GARBAGE_COLLECTION_NOTIFICATION
					// notifications here
					if (notification.getType()
							.equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {

						final CompositeData cd = (CompositeData) notification.getUserData();

						System.err.println(notification.getSource());

						final GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from(cd);

						System.err.println(info.getGcName());
						System.err.println(info.getGcAction());
						System.err.println(info.getGcCause());
						System.err.println(info.getGcInfo().getDuration());
						System.err.println(info.getGcInfo().getId());
						System.err.println(info.getGcInfo().getMemoryUsageBeforeGc());
						System.err.println(info.getGcInfo().getMemoryUsageAfterGc());

					}
				}
			};

			// Add the listener
			emitter.addNotificationListener(listener, null, null);

		}

	}

}

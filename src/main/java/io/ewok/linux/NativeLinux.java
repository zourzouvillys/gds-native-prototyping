package io.ewok.linux;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Platform;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NativeLinux {


	// package visibility
	static GLIBC libc;

	private static boolean binit;

	static {

		binit = false;
		try {

			if (!Platform.isLinux()) {

				log.warn("Not running Linux, jaydio support disabled");

			} else { // now check to see if we have O_DIRECT...

				final int linuxVersion = 0;
				final int majorRev = 1;
				final int minorRev = 2;

				final List<Integer> versionNumbers = new ArrayList<>();
				for (final String v : System.getProperty("os.version").split("\\.|-")) {
					if (v.matches("\\d")) {
						versionNumbers.add(Integer.parseInt(v));
					}
				}

				/*
				 * From "man 2 open":
				 *
				 * O_DIRECT support was added under Linux in kernel version
				 * 2.4.10. Older Linux kernels simply ignore this flag. Some
				 * file systems may not implement the flag and open() will fail
				 * with EINVAL if it is used.
				 */

				// test to see whether kernel version >= 2.4.10
				if (versionNumbers.get(linuxVersion) > 2) {
					binit = true;
				} else if (versionNumbers.get(linuxVersion) == 2) {
					if (versionNumbers.get(majorRev) > 4) {
						binit = true;
					} else if (versionNumbers.get(majorRev) == 4 && versionNumbers.get(minorRev) >= 10) {
						binit = true;
					}
				}

				if (binit) {

					//Native.setProtected(true);

					//Native.register(Platform.C_LIBRARY_NAME);

					NativeLinux.libc = Native.loadLibrary("c", GLIBC.class);

				} else {
					log.warn(String.format("O_DIRECT not supported on your version of Linux: %d.%d.%d", linuxVersion,
							majorRev, minorRev));
				}
			}
		} catch (final Throwable e) {

			log.warn("Unable to register libc at class load time: " + e.getMessage(), e);

		}

	}


}

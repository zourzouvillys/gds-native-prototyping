/**
 * Various interactions with the JVM.
 *
 * Specifically, a source which provides us with knowledge of GC events so that
 * we can adjust accounting for {@link io.ewok.fiber.Fiber} CPU time, raise notifications for GC pressure, detect potential leaks,
 *
 * @author theo
 *
 */
package io.ewok.jvm;
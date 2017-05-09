/**
 * <ul>
 * <li>Fiber - a lightweight fibre. scheduled to run within a</li>
 * <li>Strand - A thread or a fiber</li>
 * <li>WorkContext - Context for tracking correlation with high level API
 * calls.</li>
 * <li>RuntimeHost -</li>
 * </ul>
 *
 * BEcause a Fiber logically never blocks, all execution is "immediate" (quotes
 * because it's obviously not ...). Therefore, the environment remains the same
 * through any single dispatch. The currentInstant will be the time it started
 * running the Fiber, not the current system time.
 *
 *
 * @author theo
 *
 */
package io.ewok.fiber;
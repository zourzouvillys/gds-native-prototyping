package io.ewok.linux;

import java.nio.file.Path;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * Exposes glibc functions.
 *
 * We currently only expect syscall. This should help run under other libc
 * runtimes, and doesn't mean we need to have a modern glibc for some of the
 * newer syscalls.
 *
 * @author theo
 *
 */

public interface GLIBC extends Library {

	int getpagesize();

	Pointer malloc(long size);

	void free(Pointer ptr);

	long syscall(int number, Object... args);

	String strerror(int errno);

	long sysconf(int name);

	long fpathconf(int fd, int name);

	long pathconf(Path path, int name);

}
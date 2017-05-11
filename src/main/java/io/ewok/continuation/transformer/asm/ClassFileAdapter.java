package io.ewok.continuation.transformer.asm;

import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import io.ewok.continuation.ContinuationTest;
import lombok.extern.slf4j.Slf4j;

/**
 * ClassLoader which adapts loaded classes on the fly.
 *
 * @author theo
 *
 */

@Slf4j
public class ClassFileAdapter extends ClassLoader {

	/**
	 *
	 * @param name
	 * @param resolve
	 * @return
	 * @throws ClassNotFoundException
	 */

	@Override
	protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {

		if (name.startsWith("java.")) {
			System.err.println("Adapt: loading class '" + name
					+ "' without on the fly adaptation");
			return super.loadClass(name, resolve);
		}

		log.debug("Adapting {} (resolve={})", name, resolve);

		//		byte[] b = generate();

		return super.loadClass(name, resolve);
		// returns the adapted class
		//		return defineClass(caller, b, 0, b.length);

	}

	public static void main(String[] args) {

		String resource = ContinuationTest.class.replace('.', '/') + ".class";

		InputStream is = this.getResourceAsStream(resource);

		byte[] buffer;
		// adapts the class on the fly
		try {
			ClassReader cr = new ClassReader(is);
			final ClassNode classNode = new ClassNode();
			cr.accept(classNode, 0);

			resource = caller.replace('.', '/') + ".class";
			is = this.getResourceAsStream(resource);
			cr = new ClassReader(is);
			final ClassWriter cw = new ClassWriter(0);
			final ClassVisitor visitor = new MergeAdapter(cw, classNode);
			cr.accept(visitor, 0);

			buffer = cw.toByteArray();

		} catch (final Exception e) {
			throw new ClassNotFoundException(caller, e);
		}

	}

}

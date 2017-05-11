package io.ewok.continuation.transformer;

import java.io.File;
import java.util.Arrays;

import com.google.common.collect.Lists;

import spoon.Launcher;
import spoon.OutputType;
import spoon.SpoonModelBuilder;

public class Transformer {

	public static void main(String[] args) {

		try {

			final Launcher l = new Launcher();
			final SpoonModelBuilder mb = l.createCompiler();

			System.err.println("Compiulgin");

			mb.addInputSource(new File(
					"/Users/theo/projects/eliza/ewok.gds.pages/src/test/java/io/ewok/continuation/ContinuationTest.java"));

			mb.setBinaryOutputDirectory(new File("/tmp/ewok-generated/bin"));
			mb.setSourceOutputDirectory(new File("/tmp/ewok-generated/src/main/java"));

			final String[] classpath = System.getProperty("java.class.path").split(":");

			Arrays.stream(classpath).forEach(System.err::println);

			mb.setSourceClasspath(classpath);

			mb.setBuildOnlyOutdatedFiles(false);

			mb.getFactory().getEnvironment().setSelfChecks(true);
			mb.getFactory().getEnvironment().setAutoImports(true);
			mb.getFactory().getEnvironment().setLevel("TRACE");

			if (!mb.build()) {
				System.err.println("Failed to build model");
				return;
			}

			mb.process(Lists.newArrayList(new AsyncProcessor()));

			mb.generateProcessedSourceFiles(OutputType.COMPILATION_UNITS);



		} catch (final Exception ex) {

			ex.printStackTrace();

		}

	}

}

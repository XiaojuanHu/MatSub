package de.unibonn.realkd.algorithms.emm;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntToDoubleFunction;

import org.junit.Before;
import org.junit.Test;

public class ConvexMinimiserTest {

	@Before
	public void setUp() throws Exception {
	}

	static class ConfigDCSM {
		public final IntToDoubleFunction fn;
		public final double fOpt;
		public final int a, b, idxOpt;
		public final String name;

		public ConfigDCSM(IntToDoubleFunction fn, int a, int b, int idxOpt, double fOpt, String name) {
			this.fn = fn;
			this.a = a;
			this.b = b;
			this.idxOpt = idxOpt;
			this.fOpt = fOpt;
			this.name = name;
		}

		public ConfigDCSM(IntToDoubleFunction fn, int a, int b, int idxOpt, String name) {
			this(fn, a, b, idxOpt, fn.applyAsDouble(idxOpt), name);
		}

		@Override
		public String toString() {
			return String.format("(%15s) over [%3d,%3d) (span=%4d)", name, a, b, b - a);
		}
	}
	@Test
	public void testDoubleConvexSequenceMinimizer() {
		TrackingDoubleConvexSequenceMinimiser opt = new TrackingDoubleConvexSequenceMinimiser(Algorithm.BINARY_CONVEX);
		List<Algorithm> algorithms = Arrays.asList(Algorithm.LINEAR, Algorithm.TERNARY_CONVEX);
		ConfigDCSM[] configs = {
				// Corner cases
				new ConfigDCSM(x -> x, 0, -1, -1, Double.POSITIVE_INFINITY, "x, negative span"),
				new ConfigDCSM(x -> Math.pow(x + 3.2, 2), 0, 0, -1, Double.POSITIVE_INFINITY, "(x-3.2)^2: zero-span"),
				new ConfigDCSM(x -> Math.pow(x + 3.2, 2), 0, 1, 0, "(x-3.2)^2: unit span)"),
				// new Config(x -> Double.NaN, 0, 10, -1, Double.POSITIVE_INFINITY, "NaNs)"),
				// Normal cases
				new ConfigDCSM(x -> Math.pow(x - 4.7, 2), 0, 100, 5, "(x - 4.7)^2"),
				new ConfigDCSM(x -> Math.abs(x - 4.5), -50, 51, 4, "|x - 4.5|"), new ConfigDCSM(x -> x, -50, 52, -50, "x"),
				new ConfigDCSM(x -> -x + 10, -52, 53, 52, "-x"), };
		for (int ci = 0; ci < configs.length; ++ci) {
			ConfigDCSM config = configs[ci];
			for (Algorithm algorithm : algorithms) {
				System.err.format("Optimising %s using algorithm %15s", config, algorithm);
				opt.reset();
				int idxOpt = opt.minimise(config.a, config.b, config.fn, algorithm);
				assertEquals("Index for " + config.name, config.idxOpt, idxOpt);
				assertEquals("Value for " + config.name, config.fOpt, opt.getOptimum(), Math.ulp(2 * config.fOpt));
				System.err.println(" Done in " + opt.getEvaluations() + " evaluations.");
			}
		}
	}

}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package de.unibonn.realkd.algorithms.emm;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import de.unibonn.realkd.algorithms.emm.BalancedCoveragePositiveMeanShiftOptimisticEstimator.SelectionEstimation;

/**
 * @author Janis Kalofolias
 *
 */
public class BalancedCoveragePositiveMeanShiftOptimisticEstimatorTest {
	private static final String TEST_DATA_PATH_STATISTICS = "/configurations/BCPMSOEstimatorTestStatistics.data";
	private static final String TEST_DATA_PATH_MEASURES = "/configurations/BCPMSOEstimatorTestMeasures.data";
	private static final String TEST_DATA_PATH_OPTIMA = "/configurations/BCPMSOEstimatorTestOptima.data";

	// @Before
	// public void setUp() {
	// this.workspace = Workspaces.workspace();
	//
	// attribute1 =
	// attributeFactory.getCategoricalAttributeWithUniformNonRedundantCategories("Attribute
	// 1",
	// Arrays.asList("Category 1", "Category 2"),
	// NUMBER_OF_ENTITIES_IN_TEST_DATA);
	// attribute2 =
	// attributeFactory.getCategoricalAttributeWithUniformNonRedundantCategories("Attribute
	// 2",
	// Arrays.asList("Category 1", "Category 2"),
	// NUMBER_OF_ENTITIES_IN_TEST_DATA);
	// attribute3 =
	// attributeFactory.getMetricAttributeWithUniformValues("Attribute 3", 1,
	// NUMBER_OF_ENTITIES_IN_TEST_DATA);
	// attribute4 =
	// attributeFactory.getMetricAttributeWithUniformValues("Attribute 4", 1,
	// NUMBER_OF_ENTITIES_IN_TEST_DATA);
	// attribute5 =
	// attributeFactory.getMetricAttributeWithUniformValues("Attribute 5", 1,
	// NUMBER_OF_ENTITIES_IN_TEST_DATA);
	//
	// List<Attribute<?>> attributes = ImmutableList.of(attribute1, attribute2,
	// attribute3, attribute4, attribute5);
	//
	// List<String> entityNames = new ArrayList<String>();
	// for (int i = 0; i < NUMBER_OF_ENTITIES_IN_TEST_DATA; i++) {
	// entityNames.add("Entity " + i);
	// }
	//
	//// AttributeGroupStore attributeGroupStore = new
	// AttributeGroupStore(ImmutableList
	//// .of(new JointMacroAttribute("Group of attributes 4 and 5",
	// ImmutableList.of(attribute4, attribute5))));
	// // attributeGroupStore.addAttributeGroup(
	// // );
	//
	// Population population = Populations.population("Entities", "Entities",
	// "", entityNames);
	//
	// DataTable dataTable = DataTables.table("testTable", "Test data table",
	// "Contains random data.",
	// population, attributes, ImmutableList
	// .of(AttributeGroups.functionalGroup("Group of attributes 4 and 5",
	// ImmutableList.of(attribute4, attribute5))));
	//
	// workspace.add(population);
	// workspace.add(dataTable);
	// workspace.add(new PropositionalLogicFromTableBuilder().build(dataTable));
	//
	// dataTableParameter = MiningParameters.dataTableParameter(workspace);
	// targetAttributeParameter =
	// EMMParameters.getEMMTargetAttributesParameter(dataTableParameter);
	// testParameter =
	// EMMParameters.getEMMDescriptorAttributesParameter(dataTableParameter,
	// targetAttributeParameter);
	// }
	// public static ContTableTest {
	// private static final double DOUBLE_COMPARISON_PRECISION2 = 0.005;
	//
	// private Population population1 =
	// Populations.population("test_population", 4);
	//
	// private CategoricAttribute<String> a =
	// Attributes.categoricalAttribute("A", "",
	// ImmutableList.of("a", "a", "b", "b"));
	//
	// private CategoricAttribute<String> b =
	// Attributes.categoricalAttribute("B", "",
	// ImmutableList.of("a", "b", "a", "b"));
	//
	// private CategoricAttribute<String> c =
	// Attributes.categoricalAttribute("C", "",
	// ImmutableList.of("a", "b", "a", "b"));
	//
	// private CategoricAttribute<String> d =
	// Attributes.categoricalAttribute("D", "",
	// ImmutableList.of("a", "b", "c", "d"));
	//
	// private CategoricAttribute<String> e =
	// Attributes.categoricalAttribute("E", "",
	// ImmutableList.of("a", "a", "b", "a"));
	//
	// private CategoricAttribute<String> f =
	// Attributes.categoricalAttribute("F", "",
	// ImmutableList.of("a", "a", "c", "c"));
	//
	// private List<Attribute<?>> attrList1 = ImmutableList.of(a, b, c, d, e,
	// f);
	//
	// private DataTable table1 = table("table", "table", "", population1,
	// attrList1);
	//
	//
	// private TwoDimensionalContingencyTable ctable1 =
	// ContingencyTables.contingencyTable(table1, a, b);
	//
	// private TwoDimensionalContingencyTable ctable2 =
	// ContingencyTables.contingencyTable(table1, a, e);
	//
	//
	// @Test
	// public void expMIFirstTest() {
	//
	// double result = ctable1.expectedMutualInformationUnderPermutationModel();
	// double trueResult = 0.333;
	//
	// assertEquals("Should be 0.3333.", trueResult, result,
	// DOUBLE_COMPARISON_PRECISION2);
	// }

	static class ConfigTestOptimizer {
		public final IntToDoubleFunction fn;
		public final double fOpt;
		public final int a, b, idxOpt;
		public final String name;
		
		public ConfigTestOptimizer(IntToDoubleFunction fn, int a, int b, int idxOpt, double fOpt, String name) {
			this.fn = fn;
			this.a = a;
			this.b = b;
			this.idxOpt = idxOpt;
			this.fOpt = fOpt;
			this.name = name;
		}
		
		public ConfigTestOptimizer(IntToDoubleFunction fn, int a, int b, int idxOpt, String name) {
			this(fn, a, b, idxOpt, fn.applyAsDouble(idxOpt), name);
		}
		
		@Override
		public String toString() {
			return String.format("(%15s) over [%3d,%3d) (span=%4d)", name, a, b, b - a);
		}
	}
	
	@Test
	public void testOptimizer() {
		Optimizer opt = new Optimizer();
		List<Optimizer.Algorithm> algorithms = Arrays.asList(Optimizer.Algorithm.values());
		ConfigTestOptimizer[] configs = {
				// Corner cases
				new ConfigTestOptimizer(x -> x, 0, -1, -1, Double.POSITIVE_INFINITY, "x, negative span"),
				new ConfigTestOptimizer(x -> Math.pow(x + 3.2, 2), 0, 0, -1, Double.POSITIVE_INFINITY, "(x-3.2)^2: zero-span"),
				new ConfigTestOptimizer(x -> Math.pow(x + 3.2, 2), 0, 1, 0, "(x-3.2)^2: unit span)"),
				new ConfigTestOptimizer(x -> Double.NaN, 0, 10, -1, Double.POSITIVE_INFINITY, "NaNs)"),
				// Normal cases
				new ConfigTestOptimizer(x -> Math.pow(x - 4.7, 2), 0, 100, 5, "(x - 4.7)^2"),
				new ConfigTestOptimizer(x -> Math.abs(x - 4.5), -50, 51, 4, "|x - 4.5|"), new ConfigTestOptimizer(x -> x, -50, 52, -50, "x"),
				new ConfigTestOptimizer(x -> -x + 10, -52, 53, 52, "-x"), };
		for (int ci = 0; ci < configs.length; ++ci) {
			ConfigTestOptimizer config = configs[ci];
			for (Optimizer.Algorithm algorithm : algorithms) {
				System.err.format("Optimising %s using algorithm %15s", config, algorithm);
				int idxOpt = opt.minSearchTrack(config.a, config.b, config.fn, algorithm);
				assertEquals("Index for " + config.name, config.idxOpt, idxOpt);
				assertEquals("Value for " + config.name, config.fOpt, opt.getOptimum(), Math.ulp(2 * config.fOpt));
				System.err.println(" Done in " + opt.getEvaluations() + " evaluations.");
			}
		}
	}

	static class ConfigTestStatistics {
		public String name;
		// Input Data
		double[] targetIn;
		int[] controlIn;
		int[] orderIn;
		int[] isValidIn;
		// Population Data
		double[] targetPop;
		int[] controlPop;
		int[] mapIdxP2F;
		int numCat;
		int[] cntCatPop;
		int numPop;
		int numFull;
		// Selection Data
		double[] targetSel;
		int[] controlSel;
		int[] isMemberSel;
		int[] cntCatSel;
		int numSel;
		// Population Statistics
		double[][] cumSum;
		double meanValue;
		double maxValue;
		double[] prbCatPop;
		// Selection Statistics
		int[][] cumCnt;
		int[][] mapC2S;
		int numCatSel; // Only for Population-agnostic initialisation
		
		@Override
		public String toString() {
			return String.format("Config[%s](%d/%d elements)", name, numPop, numFull);
		}
	}
	@Test
	public void testStatistics() throws NoSuchFieldException, IllegalAccessException, IOException {
		ConfigTestStatistics[] configs = SimpleConfigParser.readConfig(TEST_DATA_PATH_STATISTICS, ConfigTestStatistics.class, ConfigTestStatistics[]::new,
				ConfigTestStatistics::new);
		final double eps = Math.ulp(1); // Comparison precision, when math
										// operations
		// are expected.
		for (int ci = 0; ci < configs.length; ++ci) {
			ConfigTestStatistics c = configs[ci];
			System.err.format("** Testing statistics against config: %s\n", c.name);

			System.err.print("Testing PopulationData: ");
			PopulationData[] dataPops = {
					new PopulationData(i -> c.targetIn[i], i -> c.controlIn[i], IntStream.of(c.orderIn), c.numFull,
							c.numCat),
					new PopulationData(DoubleStream.of(c.targetIn), IntStream.of(c.controlIn),
							IntStream.of(c.isValidIn)) };
			for (int pi = 0; pi < dataPops.length; ++pi) {
				PopulationData dataPop = dataPops[pi];
				Assert.assertArrayEquals("PopulationData target " + pi, c.targetPop, dataPop.target, 0.0);
				Assert.assertArrayEquals("PopulationData control " + pi, c.controlPop, dataPop.control);
				Assert.assertArrayEquals("PopulationData mapIdxP2F " + pi, c.mapIdxP2F, dataPop.mapIdxP2F);
				Assert.assertArrayEquals("PopulationData cntCat " + pi, c.cntCatPop, dataPop.cntCat);
				Assert.assertEquals("PopulationData numCat " + pi, c.numCat, dataPop.numCat);
				Assert.assertEquals("PopulationData numFull " + pi, c.numFull, dataPop.numFull);
				Assert.assertEquals("PopulationData numPop " + pi, c.numPop, dataPop.numPop);
				System.err.print(".");
			}
			System.err.println(" Done");

			System.err.print("Testing SelectionData: ");
			SelectionData[] dataSels = { new SelectionData(dataPops[0], i -> c.isMemberSel[i] != 0, c.numSel),
					new SelectionData(dataPops[0], i -> c.isMemberSel[i] != 0) };
			for (int si = 0; si < dataSels.length; ++si) {
				SelectionData dataSel = dataSels[si];
				Assert.assertArrayEquals("Selection target " + si, c.targetSel, dataSel.target, 0.0);
				Assert.assertArrayEquals("Selection control " + si, c.controlSel, dataSel.control);
				Assert.assertEquals("Selection numCat " + si, c.numCat, dataSel.numCat);
				Assert.assertArrayEquals("Selection cntCat " + si, c.cntCatSel, dataSel.cntCat);
				Assert.assertEquals("Selection numSel " + si, c.numSel, dataSel.numSel);
				System.err.print(".");
			}
			System.err.println(" Done");

			System.err.print("Testing PopulationStats: ");
			PopulationStatistics[] statsPops = { new PopulationStatistics(dataPops[0]),
					new PopulationStatistics(c.targetPop, c.controlPop, c.cntCatPop),
					new PopulationStatistics(c.targetPop, c.controlPop) };
			for (int si = 0; si < statsPops.length; ++si) {
				PopulationStatistics statsPop = statsPops[si];
				Assert.assertEquals("Population stats mean " + si, c.meanValue, statsPop.meanValue, eps);
				Assert.assertEquals("Population stats maxValue " + si, c.maxValue, statsPop.maxValue, 0.0);
				Assert.assertArrayEquals("Population stats cntCat " + si, c.cntCatPop, statsPop.cntCat);
				Assert.assertArrayEquals("Population stats prbCat " + si, c.prbCatPop, statsPop.prbCat, eps);
				Assert.assertEquals("Population stats numItems " + si, c.numPop, statsPop.numItems);
				System.err.print(".");
			}
			System.err.println(" Done");

			System.err.print("Testing SelectionStats: ");
			SelectionStatistics[] statsSels;
			{
				double[][] cumSumAgn = Arrays.copyOfRange(c.cumSum, 0, c.numCatSel);
				int[][] cumCntAgn = Arrays.copyOfRange(c.cumCnt, 0, c.numCatSel);
				int[][] mapC2SAgn = Arrays.copyOfRange(c.mapC2S, 0, c.numCatSel);
				int[] cntCatSelAgn = Arrays.copyOfRange(c.cntCatSel, 0, c.numCatSel);
				statsSels = new SelectionStatistics[] { new SelectionStatistics(dataSels[0]),
						new SelectionStatistics(c.targetSel, c.controlSel),
						new SelectionStatistics(c.targetSel, c.controlSel, c.cntCatSel),
						new SelectionStatistics(cumSumAgn, cumCntAgn, mapC2SAgn),
						new SelectionStatistics(cumSumAgn, cumCntAgn, mapC2SAgn, cntCatSelAgn),
						new SelectionStatistics(cumSumAgn, cumCntAgn, mapC2SAgn, cntCatSelAgn, c.numSel) };

				int[] isPopAgnostic = { 0, 1, 0, 1, 1, 1 };
				for (int si = 0; si < statsSels.length; ++si) {
					// Handle case of missing some empty categories, when the
					// constructor is unaware of the original Population
					int numCat = isPopAgnostic[si] != 0 ? c.numCatSel : c.numCat;
					int[] cntCat = isPopAgnostic[si] != 0 ? cntCatSelAgn : c.cntCatSel;
					int[][] cumCnt = isPopAgnostic[si] != 0 ? cumCntAgn : c.cumCnt;
					int[][] mapC2S = isPopAgnostic[si] != 0 ? mapC2SAgn : c.mapC2S;
					SelectionStatistics statsSel = statsSels[si];
					for (int di = 0; di < numCat; ++di) {
						Assert.assertArrayEquals("Selection Stats cumSum " + si + " dimension: " + di, c.cumSum[di],
								statsSel.cumSum[di], eps * 10);
					}
					Assert.assertArrayEquals("Selection Stats cumCnt " + si, cumCnt, statsSel.cumCnt);
					Assert.assertArrayEquals("Selection Stats mapC2S " + si, mapC2S, statsSel.mapC2S);
					Assert.assertArrayEquals("Selection Stats cntCat " + si, cntCat, statsSel.cntCat);
					Assert.assertEquals("Selection Stats numSel" + si, c.numSel, statsSel.numSel);
					Assert.assertEquals("Selection Stats numCat" + si, numCat, statsSel.numCat);
					System.err.print(".");
				}
				System.err.println(" Done");
			}
		}
	}

	static class ConfigTestMeasures {
		public String name;
		// Input Data
		double[] targetIn;
		int[] controlIn;
		int[] orderIn;
		int[] isValidIn;
		int[] isMemberSel;
		// Validate config
		double[] targetPop;
		int[] controlPop;
		double[] targetSel;
		int[] controlSel;
		double meanPop;
		double maxPop;
		
		// Paths
		int[][] ctPath;
		// Measures
		double[] covNrmPath;
		double[] tendNrmPath;
		double[] reprNrmPath;
		
		@Override
		public String toString() {
			return String.format("Config[%s](%d/%d elements)", name, targetIn.length, ctPath.length);
		}
	}
	
	@Test
	public void testMeasures() throws NoSuchFieldException, IllegalAccessException, IOException {

		ConfigTestMeasures[] configs = SimpleConfigParser.readConfig(TEST_DATA_PATH_MEASURES, ConfigTestMeasures.class, ConfigTestMeasures[]::new,
				ConfigTestMeasures::new);
		final double eps = Math.ulp(1) * 8; // Comparison precision, when math
											// operations
		// are expected.
		for (int ci = 0; ci < configs.length; ++ci) {
			ConfigTestMeasures c = configs[ci];
			System.err.format("** Testing measures against config: %s\n", c.name);

			System.err.print("Creating Data and Statistics: ");
			PopulationData dataPop = new PopulationData(DoubleStream.of(c.targetIn), IntStream.of(c.controlIn),
					IntStream.of(c.isValidIn));
			PopulationStatistics statsPop = new PopulationStatistics(dataPop);
			SelectionData dataSel = new SelectionData(dataPop, i -> c.isMemberSel[i] != 0);
			SelectionStatistics statsSel = new SelectionStatistics(dataSel);
			Assert.assertArrayEquals("Loaded Population target", c.targetPop, dataPop.target, eps);
			Assert.assertArrayEquals("Loaded Population control", c.controlPop, dataPop.control);
			Assert.assertArrayEquals("Loaded Selection target", c.targetSel, dataSel.target, eps);
			Assert.assertArrayEquals("Loaded Selection control", c.controlSel, dataSel.control);
			System.err.println(" Done");

			System.err.print("Testing Measures: ");

			ClassCountSpaceMeasures m = new ClassCountSpaceMeasures(statsSel, statsPop);
			PopulationStatistics mStatsPop = m.getPopulationStats();
			Assert.assertEquals("CCSM Population Mean", c.meanPop, mStatsPop.meanValue, eps);
			Assert.assertEquals("CCSM Population Max", c.maxPop, mStatsPop.maxValue, eps);

			int[][] ctPath = IntStream.range(0, dataSel.numSel + 1).mapToObj(statsSel::getCTPathPoint)
					.toArray(i -> new int[i][]);
			Assert.assertArrayEquals("CT Path", c.ctPath, ctPath);

			double[] covNrmPath = Stream.of(ctPath)
					.mapToDouble(l -> m.computeMeasure(l, ClassCountSpaceMeasures.Type.MEASURE_NORMALIZED_COVERAGE))
					.toArray();
			Assert.assertArrayEquals("CT Path Coverage (Nrm)", c.covNrmPath, covNrmPath, eps);
			double[] tendNrmPath = Stream.of(ctPath)
					.mapToDouble(l -> m.computeMeasure(l, ClassCountSpaceMeasures.Type.MEASURE_NORMALIZED_MEAN))
					.toArray();
			Assert.assertArrayEquals("CT Path Mean (Nrm)", c.tendNrmPath, tendNrmPath, eps);
			double[] reprNrmPath = Stream.of(ctPath).mapToDouble(l -> m.computeMeasure(l,
					ClassCountSpaceMeasures.Type.MEASURE_NORMALIZED_TOTAL_VARIATION_SIMILARITY)).toArray();
			Assert.assertArrayEquals("CT Path Repres. (Nrm)", c.reprNrmPath, reprNrmPath, eps);
//			public class SelectionEstimation {
//				final int[] optCounts;
//				final double optValue;
//				final int[] ctOptCounts;
//				final double ctOptValue;
//				final int ctOptIndex;
//
//				class SSTOptimalValue extends SequenceValue<SSTOptimalValue> {

					
		}
	}

	static class ConfigTestOptima {
		public String name;
		// Input Data
		double[] targetIn;
		int[] controlIn;
		int[] isValidIn;
		int[] isMemberSel;
		int numCat;
		// optima specification
		double[] wReprBrk;
		int[][] optCntBrk;
		double[] optValBrk;
		int[][] ctPath;
		int[] ctOptCnt;
		double ctOptVal;
		
		@Override
		public String toString() {
			return String.format("Config[%s](%d/%d elements)", name, targetIn.length, ctPath.length);
		}
	}
	
//	@Test // TODO: Fix this bug
	public void testOptima() throws NoSuchFieldException, IllegalAccessException, IOException {

		ConfigTestOptima[] configs = SimpleConfigParser.readConfig(TEST_DATA_PATH_OPTIMA, ConfigTestOptima.class, ConfigTestOptima[]::new,
				ConfigTestOptima::new);
		final double eps = Math.ulp(1) * 8; // Comparison precision, when math
											// operations
		// are expected.
		for (int ci = 0; ci < configs.length; ++ci) {
			ConfigTestOptima c = configs[ci];
			System.err.format("** Testing optima against config: %s\n", c.name);

			for(int invert=0;invert<2;++invert) {
				final boolean mustInvert = invert==1;
				PopulationData dataPop;
				if (!mustInvert) {
					System.err.println("Creating Data: "+c.name);
					dataPop = new PopulationData(DoubleStream.of(c.targetIn), IntStream.of(c.controlIn),
							IntStream.of(c.isValidIn));
				} else {
					System.err.println("Creating Inverted Data: "+c.name);
					IntStream strCtrl = IntStream.of(c.controlIn).map(ctrl -> c.numCat-ctrl-1); 
					dataPop = new PopulationData(DoubleStream.of(c.targetIn), strCtrl,
							IntStream.of(c.isValidIn));
				}
				
				System.err.print("Testing weights assignments: ");
				{
					BalancedCoveragePositiveMeanShiftOptimisticEstimator oest = 
							new BalancedCoveragePositiveMeanShiftOptimisticEstimator(dataPop);
					Assert.assertEquals("Estimator Weight CovTend: ", 1.0, oest.getExponentCovTend(), eps);
					Assert.assertEquals("Estimator Weight Repr: ", 1.0, oest.getExponentRepr(), eps);
					oest.setExponentCovTend(2.0);
					Assert.assertEquals("Estimator Weight CovTend: ", 2.0, oest.getExponentCovTend(), eps);
					Assert.assertEquals("Estimator Weight Repr: ", 1.0, oest.getExponentRepr(), eps);
					oest.setExponentRepr(0.2);
					Assert.assertEquals("Estimator Weight CovTend: ", 2.0, oest.getExponentCovTend(), eps);
					Assert.assertEquals("Estimator Weight Repr: ", 0.2, oest.getExponentRepr(), eps);
				}
				System.err.println("Done");
				
				System.err.print("Testing weight values: ");
				BalancedCoveragePositiveMeanShiftOptimisticEstimator oest = 
						new BalancedCoveragePositiveMeanShiftOptimisticEstimator(dataPop);
				
				SelectionData dataSel = new SelectionData(dataPop, i -> c.isMemberSel[i] != 0);
				final int[] ctOptCnt = Arrays.copyOf(c.ctOptCnt, c.ctOptCnt.length);
				if(mustInvert) {
					reverseArray(ctOptCnt);
				}
				// oest.setOptimisationAlgorithm(Algorithm.LINEAR);
				for(int wit = 0; wit<c.wReprBrk.length;++wit) {
					final double wRepr = c.wReprBrk[wit];
					final int[] optCounts = c.optCntBrk[wit];
					final double optValue = c.optValBrk[wit];
					if(mustInvert) {
						reverseArray(optCounts);
					}
					oest.setExponentRepr(wRepr);
					{
						SelectionEstimation selEst = oest.new SelectionEstimation(dataSel);
						String tag = String.format("- weight(%d):%f",wit,wRepr); 
						Assert.assertArrayEquals("Selection Estimator ct optimal counts "+tag, ctOptCnt, selEst.ctOptCounts);
						Assert.assertEquals("Selection Estimator ct optimal value "+tag, c.ctOptVal, selEst.ctOptValue, eps);
						Assert.assertArrayEquals("Selection Estimator optimal counts "+tag, optCounts, selEst.optCounts);
						Assert.assertEquals("Selection Estimator optimal value "+tag, optValue, selEst.optValue, eps);
					}
					Assert.assertEquals("Estimator Operator Value", optValue, oest.applyAsDouble(dataSel), eps);
					System.err.print(".");
				}
				System.err.println(" Done");
			}
		}
	}
	public static void reverseArray(int[] array) {
	    for (int posL = 0, posR = array.length - 1; posL < posR; posL++, posR--) {
	        final int tmp = array[posL];
	        array[posL]  = array[posR];
	        array[posR] = tmp;
	    }
	}
}


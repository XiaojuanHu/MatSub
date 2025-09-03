package de.unibonn.realkd;

import static de.unibonn.realkd.algorithms.sampling.RareDistributionTimesPowerOfFrequencyFactory.FreqPower.TWO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.BeforeClass;
import org.junit.Test;

import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.algorithms.association.AssociationMiningBeamSearch;
import de.unibonn.realkd.algorithms.association.AssociationSampler;
import de.unibonn.realkd.algorithms.association.AssociationTargetFunctionParameter;
import de.unibonn.realkd.algorithms.emm.EMMParameters;
import de.unibonn.realkd.algorithms.emm.ExceptionalModelBeamSearch;
import de.unibonn.realkd.algorithms.emm.ExceptionalSubgroupSampler;
import de.unibonn.realkd.algorithms.emm.dssd.DiverseSubgroupSetDiscovery;
import de.unibonn.realkd.algorithms.outlier.OneClassModelMiner;
import de.unibonn.realkd.algorithms.sampling.DiscriminativityDistributionFactory.GlobalFreqPower;
import de.unibonn.realkd.algorithms.sampling.DiscriminativityDistributionFactory.NegInvFreqPower;
import de.unibonn.realkd.algorithms.sampling.DiscriminativityDistributionFactory.PosFreqPower;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;
import de.unibonn.realkd.common.testing.TestConstants;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.common.workspace.Workspaces;
import de.unibonn.realkd.data.propositions.PropositionalContextFromTableBuilder;
import de.unibonn.realkd.data.table.DataFormatException;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.DataTableFromCSVFileBuilder;
import de.unibonn.realkd.patterns.Frequency;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.QualityMeasureId;
import de.unibonn.realkd.patterns.association.Association;
import de.unibonn.realkd.patterns.emm.CumulativeJensenShannonDivergence;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;
import de.unibonn.realkd.patterns.emm.ManhattenMeanDistance;
import de.unibonn.realkd.patterns.emm.ModelDeviationMeasure;
import de.unibonn.realkd.patterns.emm.TotalVariationDistance;
import de.unibonn.realkd.patterns.logical.Lift;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistributionFactory;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;
import de.unibonn.realkd.patterns.outlier.Outlier;

public class IntegrationTest {

	private static Workspace workspace;

	@BeforeClass
	public static void loadData() throws DataFormatException {
		workspace = Workspaces.workspace();
		DataTableFromCSVFileBuilder builder = new DataTableFromCSVFileBuilder();
		builder.setAttributeGroupCSVFilename(TestConstants.GERMANY_GROUPS_TXT)
				.setDataCSVFilename(TestConstants.GERMANY_DATA_SAMPLE_TXT)
				.setAttributeMetadataCSVFilename(TestConstants.GERMANY_ATTRIBUTES_TXT);
		DataTable dataTable = builder.build();
		workspace.add(dataTable);
		workspace.add(new PropositionalContextFromTableBuilder().apply(dataTable));
	}

	@Test
	public void dssdTest() throws ValidationException {
		MiningAlgorithm dssd = DiverseSubgroupSetDiscovery.createStandardDiverseSubgroupSetDiscovery(workspace);
		dssd.findParameterByName("Target attributes").setByString("[Unemployed]");
		dssd.findParameterByName("Model class").setByString("Contingency table");
		Collection<? extends Pattern<?>> results = dssd.call();
		assertNotNull("result should be not null", results);
		assertFalse("result should be not empty", results.isEmpty());
	}

	@Test
	public void testSubspaceOutlierAlgorithm() throws ValidationException {
		OneClassModelMiner oneClassModelMiner = new OneClassModelMiner(workspace);
		oneClassModelMiner.getTargetAttributesParameter().setByString("[CDU 2005,CDU 2009]");
		Collection<Pattern<?>> resultPatterns = oneClassModelMiner.call();
		assertNotNull(resultPatterns);
		assertFalse(resultPatterns.isEmpty());
		Pattern<?> pattern = resultPatterns.iterator().next();
		assertTrue(pattern instanceof Outlier);
	}

	@Test
	public void testAssociationBeamSearchNegativeLift() throws ValidationException {
		AssociationMiningBeamSearch associationMiningBeamSearch = new AssociationMiningBeamSearch(workspace);

		associationMiningBeamSearch.findParameterByName("Number of results").setByString("3");
		// associationMiningBeamSearch.getOptimizationOrderParameter().set(
		// Association.NEGATIVE_LIFT_COMPARATOR);
		associationMiningBeamSearch.findParameterByName(AssociationTargetFunctionParameter.NAME)
				.setByString("negative lift");
		// .setComparator(Association.NEGATIVE_LIFT_COMPARATOR);
		Collection<Pattern<?>> resultPatterns = associationMiningBeamSearch.call();
		assertNotNull(resultPatterns);
		assertFalse(resultPatterns.isEmpty());
		assertTrue(resultPatterns.size() == 3);
		Pattern<?> pattern = resultPatterns.iterator().next();
		assertTrue(pattern instanceof Association);
		assertTrue(pattern.hasMeasure(QualityMeasureId.NEGATIVE_LIFT));
	}

	@Test
	public void testAssociationBeamSearchPositiveLift() throws ValidationException {
		AssociationMiningBeamSearch associationMiningBeamSearch = new AssociationMiningBeamSearch(workspace);
		associationMiningBeamSearch.findParameterByName("Number of results").setByString("8");
		// associationMiningBeamSearch.getOptimizationOrderParameter().set(
		// Association.POSITIVE_LIFT_COMPARATOR);
		associationMiningBeamSearch.findParameterByName(AssociationTargetFunctionParameter.NAME).setByString("lift");
		// .setComparator(Association.POSITIVE_LIFT_COMPARATOR);
		Collection<Pattern<?>> resultPatterns2 = associationMiningBeamSearch.call();
		assertTrue(resultPatterns2.size() == 8);
		Pattern<?> pattern2 = resultPatterns2.iterator().next();
		assertTrue("Pattern must have lift measure bound", pattern2.hasMeasure(Lift.LIFT));
	}

	@Test
	public void testEMMBeamsearchInvalidSetting() throws ValidationException {
		ExceptionalModelBeamSearch exceptionalModelBeamSearch = new ExceptionalModelBeamSearch(workspace);

		exceptionalModelBeamSearch.findParameterByName("Beam width").setByString("4");
		exceptionalModelBeamSearch.findParameterByName("Number of results").setByString("3");
		exceptionalModelBeamSearch.getTargetAttributesParameter().setByString("[Type,No school degree]");
		exceptionalModelBeamSearch.getModelClassParameter()
				.set(exceptionalModelBeamSearch.getModelClassParameter().empirical_distribution_option);
		exceptionalModelBeamSearch.getModelDistanceFunctionParameter()
				.set(TotalVariationDistance.TOTAL_VARIATION_DISTANCE);

		try {
			exceptionalModelBeamSearch.call();
		} catch (ValidationException e) {
			System.out.println(e.getMessage());
			assertTrue("expected exception", e != null);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidToModifyParameterDuringRuntime() throws InterruptedException, ExecutionException {
		ExceptionalModelBeamSearch exceptionalModelBeamSearch = new ExceptionalModelBeamSearch(workspace);

		exceptionalModelBeamSearch.findParameterByName("Beam width").setByString("2");
		exceptionalModelBeamSearch.findParameterByName("Number of results").setByString("3");
		exceptionalModelBeamSearch.getTargetAttributesParameter().setByString("[Type, No school degree]");
		exceptionalModelBeamSearch.getModelClassParameter().setByString("Contingency table");
		exceptionalModelBeamSearch.getModelDistanceFunctionParameter()
				.setByString(TotalVariationDistance.TOTAL_VARIATION_DISTANCE.toString());

		exceptionalModelBeamSearch.findParameterByName(EMMParameters.EMM_TARGET_FUNCTION_PARAMETER_NAME)
				.setByString("H(frequency) times deviation");

		Future<Collection<? extends Pattern<?>>> future = Executors.newSingleThreadExecutor().submit(exceptionalModelBeamSearch);
		while (!exceptionalModelBeamSearch.running()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
		exceptionalModelBeamSearch.findParameterByName("Beam width").setByString("5");
		future.get();
	}

	@Test
	public void testEMMBeamSearch() throws ValidationException {
		ExceptionalModelBeamSearch exceptionalModelBeamSearch = new ExceptionalModelBeamSearch(workspace);

		exceptionalModelBeamSearch.findParameterByName("Beam width").setByString("2");
		exceptionalModelBeamSearch.findParameterByName("Number of results").setByString("3");
		exceptionalModelBeamSearch.getTargetAttributesParameter().setByString("[Type, No school degree]");
		exceptionalModelBeamSearch.getModelClassParameter().setByString("Contingency table");
		exceptionalModelBeamSearch.getModelDistanceFunctionParameter()
				.setByString(TotalVariationDistance.TOTAL_VARIATION_DISTANCE.toString());

		exceptionalModelBeamSearch.findParameterByName(EMMParameters.EMM_TARGET_FUNCTION_PARAMETER_NAME)
				.setByString("sqrt(frequency) times deviation");

		Collection<Pattern<?>> resultPatterns = exceptionalModelBeamSearch.call();
		assertTrue(resultPatterns.size() == 3);
		Pattern<?> topPattern = resultPatterns.iterator().next();
		assertTrue(topPattern instanceof ExceptionalModelPattern);
		assertTrue(topPattern.hasMeasure(TotalVariationDistance.TOTAL_VARIATION_DISTANCE));
		assertFalse(topPattern.hasMeasure(ManhattenMeanDistance.MANHATTAN_MEAN_DISTANCE));
		assertTrue(topPattern.hasMeasurement(ModelDeviationMeasure.class));
		assertTrue(topPattern.measurement(ModelDeviationMeasure.class).isPresent());

		ExceptionalModelPattern topPatternAsEMM = (ExceptionalModelPattern) topPattern;
		assertTrue(topPatternAsEMM.descriptor().referenceModel() instanceof ContingencyTable);
	}

	@Test
	public void testEMMSamplingWithCJS() throws ValidationException {
		ExceptionalSubgroupSampler exceptionalModelSampler = ExceptionalSubgroupSampler.exceptionalSubgroupSampler(workspace);

		exceptionalModelSampler.findParameterByName("Number of results").setByString("10");
		exceptionalModelSampler.targetAttributesParameter().setByString("[No school degree]");
		exceptionalModelSampler.modelClassParameter()
				.setByString(MetricEmpiricalDistributionFactory.STRING_NAME);
		exceptionalModelSampler.modelDistanceFunctionParameter()
				.setByString(CumulativeJensenShannonDivergence.STRING_NAME);

		exceptionalModelSampler.findParameterByName(EMMParameters.EMM_TARGET_FUNCTION_PARAMETER_NAME)
				.setByString("frequency times deviation");

		assertTrue(exceptionalModelSampler.modelClassParameter().current() == exceptionalModelSampler
				.modelClassParameter().empirical_distribution_option);

		assertTrue(exceptionalModelSampler.modelDistanceFunctionParameter()
				.current() == CumulativeJensenShannonDivergence.CJS);
		//
		Collection<? extends Pattern<?>> resultPatterns = exceptionalModelSampler.call();
		assertTrue(resultPatterns.size() == 10);
		Pattern<?> topPattern = resultPatterns.iterator().next();
		assertTrue(topPattern instanceof ExceptionalModelPattern);
		assertTrue(topPattern.hasMeasure(CumulativeJensenShannonDivergence.CJS));
		assertTrue(topPattern.value(CumulativeJensenShannonDivergence.CJS) > 0);
	}

	@Test
	public void testEMMBeamSearchWithCJS() throws ValidationException {
		ExceptionalModelBeamSearch exceptionalModelBeamSearch = new ExceptionalModelBeamSearch(workspace);

		exceptionalModelBeamSearch.findParameterByName("Beam width").setByString("2");
		exceptionalModelBeamSearch.findParameterByName("Number of results").setByString("3");
		exceptionalModelBeamSearch.getTargetAttributesParameter().setByString("[No school degree]");
		exceptionalModelBeamSearch.getModelClassParameter()
				.setByString(MetricEmpiricalDistributionFactory.STRING_NAME);
		exceptionalModelBeamSearch.getModelDistanceFunctionParameter()
				.setByString(CumulativeJensenShannonDivergence.STRING_NAME);

		exceptionalModelBeamSearch.findParameterByName(EMMParameters.EMM_TARGET_FUNCTION_PARAMETER_NAME)
				.setByString("frequency times deviation");

		assertTrue(exceptionalModelBeamSearch.getModelClassParameter().current() == exceptionalModelBeamSearch
				.getModelClassParameter().empirical_distribution_option);

		assertTrue(exceptionalModelBeamSearch.getModelDistanceFunctionParameter()
				.current() == CumulativeJensenShannonDivergence.CJS);
		//
		Collection<Pattern<?>> resultPatterns = exceptionalModelBeamSearch.call();
		assertTrue(resultPatterns.size() == 3);
		Pattern<?> topPattern = resultPatterns.iterator().next();
		assertTrue(topPattern instanceof ExceptionalModelPattern);
		assertTrue(topPattern.hasMeasure(CumulativeJensenShannonDivergence.CJS));
		assertTrue(topPattern.value(CumulativeJensenShannonDivergence.CJS) > 0);
	}

	@Test
	public void testAssociationSampler() throws ValidationException {
		AssociationSampler associationSampler = new AssociationSampler(workspace);
		associationSampler.setNumberOfResults(23);
		associationSampler.rareOption().power(TWO);
		associationSampler.setRareOption();
		// associationSampler.setDistributionFactory(new
		// RareDistributionTimesPowerOfFrequencyFactory(2));

		Collection<Association> patterns = associationSampler.call();
		assertTrue(patterns.size() == 23);
	}

	@Test
	public void testEMMSampler() throws ValidationException {
		ExceptionalSubgroupSampler exceptionalModelSampler = ExceptionalSubgroupSampler.exceptionalSubgroupSampler(workspace);

		exceptionalModelSampler.numberOfResults(10);
		exceptionalModelSampler.findParameterByName("Number of results").setByString("12");
		exceptionalModelSampler.numberOfSeedsParameter().set(300);
		exceptionalModelSampler.targetAttributesParameter().setByString("[Type, No school degree]");
		// exceptionalModelSampler.setDistributionFactory(new
		// DiscriminativityDistributionFactory(
		// exceptionalModelSampler.getDataTableParameter(),
		// exceptionalModelSampler.getTargetAttributesParameter(),
		// exceptionalModelSampler.getDescriptionAttributesParameter(), 1, 1,
		// 1));
		exceptionalModelSampler.discriminativityOption().powerOfGlobalFrequency(GlobalFreqPower.ONE);
		exceptionalModelSampler.discriminativityOption().powerOfPositiveFrequency(PosFreqPower.ONE);
		exceptionalModelSampler.discriminativityOption().powerOfNegativeInverseFrequency(NegInvFreqPower.ONE);
		exceptionalModelSampler.useDiscriminativitySeedOption();
		// exceptionalModelSampler.setFreqTimesDiscriminativityOption();

		Parameter<?> posNegCreatorParameter = exceptionalModelSampler.findParameterByName("Splitting method");
		assertNotNull(posNegCreatorParameter);
		RangeEnumerableParameter<?> posNegCreatorAsRangeEnum = (RangeEnumerableParameter<?>) posNegCreatorParameter;
		assertTrue(posNegCreatorAsRangeEnum.getRange().size() == 2);
		posNegCreatorAsRangeEnum.setByString("Split on all attributes");
		assertTrue(posNegCreatorAsRangeEnum.current().toString().equals("Split on all attributes"));

		exceptionalModelSampler.modelClassParameter()
				.set(exceptionalModelSampler.modelClassParameter().contingency_table_option);

		Collection<ExceptionalModelPattern> resultPatterns = exceptionalModelSampler.call();
		assertEquals(resultPatterns.size(), 12);
		Pattern<?> topPattern = resultPatterns.iterator().next();
		assertTrue(topPattern instanceof ExceptionalModelPattern);
		assertTrue(topPattern.hasMeasure(TotalVariationDistance.TOTAL_VARIATION_DISTANCE));
		assertFalse(topPattern.hasMeasure(ManhattenMeanDistance.MANHATTAN_MEAN_DISTANCE));
		assertTrue(topPattern.value(TotalVariationDistance.TOTAL_VARIATION_DISTANCE) > 0);

		assertTrue(topPattern.hasMeasure(Frequency.FREQUENCY));
		assertTrue(topPattern.value(Frequency.FREQUENCY) > 0);

	}
}

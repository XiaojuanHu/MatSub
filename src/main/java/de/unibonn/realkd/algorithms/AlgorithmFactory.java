package de.unibonn.realkd.algorithms;

import static de.unibonn.realkd.common.base.Identifier.identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.association.AssociationMiningBeamSearch;
import de.unibonn.realkd.algorithms.association.AssociationSampler;
import de.unibonn.realkd.algorithms.association.DummyCrashMiner;
import de.unibonn.realkd.algorithms.derived.DerivedAlgorithms;
import de.unibonn.realkd.algorithms.derived.ParameterWrapper;
import de.unibonn.realkd.algorithms.derived.SimpleParameterAdapter;
import de.unibonn.realkd.algorithms.derived.StringValueTerminator;
import de.unibonn.realkd.algorithms.derived.TrivialParameterAdapter;
import de.unibonn.realkd.algorithms.emm.EMMParameters;
import de.unibonn.realkd.algorithms.emm.ExceptionalModelBeamSearch;
import de.unibonn.realkd.algorithms.emm.ExceptionalSubgroupBestFirstBranchAndBound;
import de.unibonn.realkd.algorithms.emm.ExceptionalSubgroupSampler;
import de.unibonn.realkd.algorithms.emm.dssd.DiverseSubgroupSetDiscovery;
import de.unibonn.realkd.algorithms.functional.OPUSFunctionalPatternSearch;
import de.unibonn.realkd.algorithms.outlier.OneClassModelMiner;
import de.unibonn.realkd.algorithms.pmm.PureModelBeamSearch;
import de.unibonn.realkd.algorithms.pmm.RandomizedLocalPureSubgroupSearch;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.emm.CumulativeJensenShannonDivergence;
import de.unibonn.realkd.patterns.models.ModelFactory;
import de.unibonn.realkd.patterns.models.gaussian.GaussianModelFactory;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistributionFactory;
import de.unibonn.realkd.patterns.models.weibull.FixedShapeWeibullModelFactory;

/**
 * <p>
 * NOTE: The following is important Creedo documentation, which is likely not at
 * the right place here. Changing, e.g., names in this collection will break
 * Creedo (but has no effect on realKD, because this collection is not used
 * anywhere in realKD itself at the moment).
 * </p>
 * <p>
 * Enum of all configured algorithms. This type is used to deserialize
 * MiningAlgorithmFactories that are stored in the DB. This means they are of
 * type AlgorithmFactory in the corresponding classes. This will probably be
 * replaced by a more robust system in the future.
 * </p>
 * 
 * @author Björn Jacobs
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 * 
 */
public enum AlgorithmFactory implements MiningAlgorithmFactory {

	// core
	EXCEPTIONAL_MODEL_BEAMSEARCH {
		@Override
		public MiningAlgorithm create(Workspace dataTub) {
			return new ExceptionalModelBeamSearch(dataTub);
		}
	},
	FUNCTIONAL_PATTERN_DISCOVERY_OPUS {
		@Override
		public MiningAlgorithm create(Workspace workspace) {
			return new OPUSFunctionalPatternSearch(workspace);
		}
	},
	PURE_MODEL_BEAMSEARCH {
		@Override
		public MiningAlgorithm create(Workspace workspace) {
			return new PureModelBeamSearch(workspace);
		}
	},

	// core
	ASSOCIATION_BEAMSEARCH {
		@Override
		public MiningAlgorithm create(Workspace dataTub) {
			AssociationMiningBeamSearch algorithm = new AssociationMiningBeamSearch(dataTub);
			// algorithm.setComparator(Association.POSITIVE_LIFT_COMPARATOR);
			// algorithm.setComparator(Association.ABSOLUTE_LIFT_COMPARATOR);
			return algorithm;
		}
	},

	/*
	 * FREQUENCY_ASSOCIATION_SAMPLER {
	 * 
	 * @Override public MiningAlgorithm create(DataWorkspace dataTub) {
	 * AssociationSampler sampler = new AssociationSampler(dataTub);
	 * sampler.setDistributionFactory(new FrequencyDistributionFactory(1));
	 * List<ParameterSelector> parameterSelectors = new ArrayList<>();
	 * parameterSelectors .add(new
	 * ParameterSelector.FixAssociationOptimizationFunctionToAbsoluteLift(
	 * sampler.getOptimizationOrderParameter())); ParameterChoosingMetaAlgorithm
	 * algorithm = new ParameterChoosingMetaAlgorithm( dataTub, sampler,
	 * parameterSelectors); return algorithm; }
	 * 
	 * },
	 */

	/*
	 * ABSOLUTE_LIFT_ASSOCIATION_BEAMSEARCH {
	 * 
	 * @Override public MiningAlgorithm create(DataWorkspace dataTub) {
	 * AssociationMiningBeamSearch beamSearch = new AssociationMiningBeamSearch(
	 * dataTub); List<ParameterSelector> parameterSelectors = new ArrayList<>();
	 * parameterSelectors .add(new
	 * ParameterSelector.FixAssociationOptimizationFunctionToAbsoluteLift(
	 * beamSearch.getOptimizationFunctionParameter()));
	 * ParameterChoosingMetaAlgorithm algorithm = new
	 * ParameterChoosingMetaAlgorithm( dataTub, beamSearch, parameterSelectors);
	 * return algorithm; } },
	 */

	// "core"
	// RARE_TIMES_FREQ_2_SAMPLER {
	// @Override
	// public MiningAlgorithm create(DataWorkspace dataTub) {
	// AssociationSampler algorithm = new AssociationSampler(dataTub);
	// algorithm
	// .setDistributionFactory(new RareDistributionTimesPowerOfFrequencyFactory(
	// 2));
	// return algorithm;
	// }
	// },

	// core
	EMM_SAMPLER {
		@Override
		public StoppableMiningAlgorithm create(Workspace workspace) {
			return ExceptionalSubgroupSampler.exceptionalSubgroupSampler(workspace);
		}
	},

	EXCEPTIONAL_SUBGROUP_BESTFIRST_BRANCHANDBOUND {
		@Override
		public ExceptionalSubgroupBestFirstBranchAndBound create(Workspace workspace) {
			return new ExceptionalSubgroupBestFirstBranchAndBound(workspace);
		}
	},

	PMM_SAMPLER {

		@Override
		public MiningAlgorithm create(Workspace workspace) {
			return new RandomizedLocalPureSubgroupSearch(workspace);
		}

	},

	// core
	ASSOCIATION_SAMPLER {
		@Override
		public MiningAlgorithm create(Workspace workspace) {
			return new AssociationSampler(workspace);
		}

	},

	FUNCTIONAL_PATTERN_OPUS {

		@Override
		public MiningAlgorithm create(Workspace workspace) {
			return new OPUSFunctionalPatternSearch(workspace);
		}

	},

	/*
	 * DISCRIMINATIVITY_TIMES_POS_FREQ_2_SAMPLER {
	 * 
	 * @Override public MiningAlgorithm create(DataWorkspace workspace) { final
	 * ExceptionalModelSampler modelSampler = new ExceptionalModelSampler(
	 * workspace); // DiscriminativityDistributionFactory distributionFactory = new
	 * // DiscriminativityDistributionFactory( //
	 * modelSampler.getPropositionalLogicParameter() // .getCurrentValue(),
	 * modelSampler // .getTargetAttributesParameter().getCurrentValue(), // 0, 2,
	 * 1); // modelSampler.setDistributionFactory(distributionFactory);
	 * 
	 * ParameterSelector.ExponentialSingleTargetProposer
	 * exponentialSingleTargetProposer = new
	 * ParameterSelector.ExponentialSingleTargetProposer(
	 * modelSampler.getTargetAttributesParameter());
	 * 
	 * ParameterSelector.SuitableSingleTargetEMFactorySelector
	 * suitableSingleTargetEMFactorySelector = new
	 * ParameterSelector.SuitableSingleTargetEMFactorySelector(
	 * modelSampler.getModelClassParameter());
	 * 
	 * ParameterSelector.FirstInRageSelector<ModelDistanceFunction>
	 * firstDistanceFunctionInRangeSelector = new
	 * ParameterSelector.FirstInRageSelector<>(
	 * modelSampler.getModelDistanceFunctionParameter());
	 * 
	 * List<ParameterSelector> list = new ArrayList<>();
	 * list.add(exponentialSingleTargetProposer); list.add(new ParameterSelector() {
	 * 
	 * @Override public void setParameter(PatternUtilityModel patternUtilityModel) {
	 * DiscriminativityDistributionFactory distributionFactory = new
	 * DiscriminativityDistributionFactory(
	 * modelSampler.getPropositionalLogicParameter() .getCurrentValue(),
	 * modelSampler .getTargetAttributesParameter() .getCurrentValue(), 0, 2, 1);
	 * modelSampler.setDistributionFactory(distributionFactory); }
	 * 
	 * @Override public Parameter<?> getParameter() { return
	 * modelSampler.getDistributionFactoryParameter(); } });
	 * list.add(suitableSingleTargetEMFactorySelector);
	 * list.add(firstDistanceFunctionInRangeSelector);
	 * 
	 * ParameterChoosingMetaAlgorithm algorithm = new
	 * ParameterChoosingMetaAlgorithm( workspace, modelSampler, list);
	 * 
	 * return algorithm; } },
	 */

	/*
	 * DEFAULT_SUBGROUP_BEAMSEARCH {
	 * 
	 * @Override public MiningAlgorithm create(DataWorkspace dataTub) {
	 * 
	 * ExceptionalModelBeamSearch beamSearch = new ExceptionalModelBeamSearch(
	 * dataTub); beamSearch.getOptimizationFunctionParameter().set(
	 * ExceptionalModelPattern.FREQUENCYDEVIATION_COMPARATOR); //
	 * .setComparator(ExceptionalModelPattern.FREQUENCYDEVIATION_COMPARATOR);
	 * 
	 * ParameterSelector.ExponentialSingleTargetProposer
	 * exponentialSingleTargetProposer = new
	 * ParameterSelector.ExponentialSingleTargetProposer(
	 * beamSearch.getTargetAttributesParameter());
	 * 
	 * ParameterSelector.SuitableSingleTargetEMFactorySelector
	 * suitableSingleTargetEMFactorySelector = new
	 * ParameterSelector.SuitableSingleTargetEMFactorySelector(
	 * beamSearch.getModelClassParameter());
	 * 
	 * ParameterSelector.FirstInRageSelector<ModelDistanceFunction>
	 * firstDistanceFunctionInRangeSelector = new
	 * ParameterSelector.FirstInRageSelector<>(
	 * beamSearch.getModelDistanceFunctionParameter());
	 * 
	 * List<ParameterSelector> list = new ArrayList<>(); // the next two lines are
	 * switched in order to provoke an error in // the old implementation of
	 * ParameterChoosing Meta list.add(suitableSingleTargetEMFactorySelector);
	 * list.add(exponentialSingleTargetProposer);
	 * list.add(firstDistanceFunctionInRangeSelector);
	 * 
	 * ParameterChoosingMetaAlgorithm algorithm = new
	 * ParameterChoosingMetaAlgorithm( dataTub, beamSearch, list);
	 * 
	 * return algorithm; } },
	 */

	/*
	 * DEFAULT_EMM_BEAMSEARCH {
	 * 
	 * @Override public MiningAlgorithm create(DataWorkspace dataTub) {
	 * ExceptionalModelBeamSearch beamSearch = new ExceptionalModelBeamSearch(
	 * dataTub); beamSearch.getOptimizationFunctionParameter().set(
	 * ExceptionalModelPattern.FREQUENCYDEVIATION_COMPARATOR);
	 * 
	 * ParameterSelector.ExponentialTwoTargetsProposer proposer = new
	 * ParameterSelector.ExponentialTwoTargetsProposer(
	 * beamSearch.getTargetAttributesParameter());
	 * 
	 * ParameterSelector.SuitableDoubleTargetEMFactorySelector emFactorySelector =
	 * new ParameterSelector.SuitableDoubleTargetEMFactorySelector(
	 * beamSearch.getModelClassParameter());
	 * 
	 * ParameterSelector.FirstInRageSelector<ModelDistanceFunction>
	 * firstDistanceFunctionInRangeSelector = new
	 * ParameterSelector.FirstInRageSelector<>(
	 * beamSearch.getModelDistanceFunctionParameter());
	 * 
	 * List<ParameterSelector> list = new ArrayList<>(); list.add(proposer);
	 * list.add(emFactorySelector); list.add(firstDistanceFunctionInRangeSelector);
	 * 
	 * ParameterChoosingMetaAlgorithm algorithm = new
	 * ParameterChoosingMetaAlgorithm( dataTub, beamSearch, list);
	 * 
	 * return algorithm; } },
	 */

	/*
	 * DISCRIMINATIVITY_TIMES_POS_FREQ_2_SAMPLER_DOUBLE_TARGET {
	 * 
	 * @Override public MiningAlgorithm create(DataWorkspace dataWorkspace) { final
	 * ExceptionalModelSampler modelSampler = new ExceptionalModelSampler(
	 * dataWorkspace); // WeightedDiscriminativityDistributionFactory factory = new
	 * // WeightedDiscriminativityDistributionFactory(modelSampler.
	 * getPropositionalLogicParameter() // .getCurrentValue(), modelSampler //
	 * .getTargetAttributesParameter().getCurrentValue(), // 0, 2, 1, new
	 * RowWeightComputer.UniformRowWeightComputer()); // //
	 * factory.setRowWeightComputer(new // //
	 * RowWeightComputer.UniformRowWeightComputer()); // //
	 * modelSampler.setDistributionFactory(factory);
	 * 
	 * ParameterSelector.ExponentialTwoTargetsProposer proposer = new
	 * ParameterSelector.ExponentialTwoTargetsProposer(
	 * modelSampler.getTargetAttributesParameter());
	 * 
	 * ParameterSelector.SuitableDoubleTargetEMFactorySelector selector = new
	 * ParameterSelector.SuitableDoubleTargetEMFactorySelector(
	 * modelSampler.getModelClassParameter());
	 * 
	 * ParameterSelector.FirstInRageSelector<ModelDistanceFunction>
	 * firstDistanceFunctionInRangeSelector = new
	 * ParameterSelector.FirstInRageSelector<>(
	 * modelSampler.getModelDistanceFunctionParameter());
	 * 
	 * List<ParameterSelector> list = new ArrayList<>(); list.add(proposer);
	 * list.add(new ParameterSelector() {
	 * 
	 * @Override public void setParameter(PatternUtilityModel patternUtilityModel) {
	 * WeightedDiscriminativityDistributionFactory factory = new
	 * WeightedDiscriminativityDistributionFactory(
	 * modelSampler.getPropositionalLogicParameter() .getCurrentValue(),
	 * modelSampler .getTargetAttributesParameter() .getCurrentValue(), 0, 2, 1, new
	 * RowWeightComputer.UniformRowWeightComputer());
	 * 
	 * modelSampler.setDistributionFactory(factory); }
	 * 
	 * @Override public Parameter<?> getParameter() { return
	 * modelSampler.getDistributionFactoryParameter(); } }); list.add(selector);
	 * list.add(firstDistanceFunctionInRangeSelector);
	 * 
	 * ParameterChoosingMetaAlgorithm algorithm = new
	 * ParameterChoosingMetaAlgorithm( dataWorkspace, modelSampler, list);
	 * 
	 * return algorithm; } },
	 */

	// core
	ONE_CLASS_OUTLIER_DETECTION {

		@Override
		public MiningAlgorithm create(Workspace dataTub) {
			return new OneClassModelMiner(dataTub);
		}

	},

	/*
	 * ONE_CLICK_ONE_CLASS_OUTLIER_DETECTION {
	 * 
	 * @Override public MiningAlgorithm create(DataWorkspace dataTub) {
	 * OneClassModelMiner entailedAlgorithm = new OneClassModelMiner( dataTub);
	 * 
	 * List<ParameterSelector> parameterSelectors = new ArrayList<>();
	 * parameterSelectors .add(new
	 * ParameterSelector.ExponentialTwoNumericTargetsProposer(
	 * entailedAlgorithm.getTargetAttributesParameter()));
	 * parameterSelectors.add(new ParameterSelector.RandomPercentChooser(
	 * entailedAlgorithm.getFractionOfOutlierParameter())); return new
	 * ParameterChoosingMetaAlgorithm(dataTub, entailedAlgorithm,
	 * parameterSelectors); } },
	 */

	DIVERSE_SUBGROUP_SET_DISCOVERY {
		@Override
		public MiningAlgorithm create(Workspace workspace) {
			return DiverseSubgroupSetDiscovery.createStandardDiverseSubgroupSetDiscovery(workspace);
		}

	},

	DUMMY_CRASH_MINER {
		@Override
		public MiningAlgorithm create(Workspace dataTub) {
			return new DummyCrashMiner(dataTub);
		}
	},

	NOMAD_OCTED_BINARY_TUTORIAL_EMM_MINER {

		@Override
		public MiningAlgorithm create(Workspace workspace) {
			ExceptionalSubgroupSampler emmSampler = (ExceptionalSubgroupSampler) EMM_SAMPLER.create(workspace);

			ArrayList<ParameterWrapper> wrappers = new ArrayList<ParameterWrapper>();
			RangeEnumerableParameter<List<Attribute<? extends Object>>> targetAttributeParameter = Parameters
					.rangeEnumerableParameter(identifier("target"), "Target variable",
							"Variable with respect to which subgroup utility is determined.", List.class,
							() -> ImmutableList.of(
									ImmutableList.of(emmSampler.dataTableParameter().current().attribute(1)),
									ImmutableList.of(emmSampler.dataTableParameter().current().attribute(2)),
									ImmutableList.of(emmSampler.dataTableParameter().current().attribute(0))),
							emmSampler.dataTableParameter());

			RangeEnumerableParameter<Supplier<ModelFactory<?>>> simplifiedModelClassParam = Parameters
					.rangeEnumerableParameter(emmSampler.modelClassParameter().id(),
							emmSampler.modelClassParameter().getName(),
							emmSampler.modelClassParameter().getDescription(),
							emmSampler.modelClassParameter().getType(),
							() -> (targetAttributeParameter.current().get(0) instanceof MetricAttribute)
									? ImmutableList.of(emmSampler.modelClassParameter().empirical_distribution_option)
									: ImmutableList.of(emmSampler.modelClassParameter().contingency_table_option),
							() -> true, targetAttributeParameter);

			wrappers.add(new SimpleParameterAdapter<List<Attribute<?>>>(emmSampler.targetAttributesParameter(),
					targetAttributeParameter, SimpleParameterAdapter.INNERCHANGEOPTION_ISSUE_WARNING));
			wrappers.add(new SimpleParameterAdapter<>(emmSampler.modelClassParameter(), simplifiedModelClassParam,
					SimpleParameterAdapter.INNERCHANGEOPTION_PROPAGATE_TO_OUTER));
			wrappers.add(new TrivialParameterAdapter(emmSampler.descriptionAttributesParameter()));
			wrappers.add(new TrivialParameterAdapter(emmSampler.modelDistanceFunctionParameter()));
			wrappers.add(new TrivialParameterAdapter(emmSampler.objectiveFunctionParameter()));
			wrappers.add(new TrivialParameterAdapter(emmSampler.numberOfResultsParameter()));
			wrappers.add(new TrivialParameterAdapter(emmSampler.numberOfSeedsParameter()));
			MiningAlgorithm result = DerivedAlgorithms.getAlgorithmWithWrappedParameters(emmSampler, wrappers,
					DerivedAlgorithms.HIDE_AND_WARN, "Randomized Exceptional Subgroup Discovery");
			result.findParameterByName("Number of seeds").setByString("2000");
			result.findParameterByName("Attribute filter").setByString(
					"[(EA(B)-IP(B))/rp(A)^2,|rs(A)-rp(B)|/exp(rs(A)),|rp(B)-rs(B)|/exp(rd(A)+rs(B)),r_sigma,r_pi]");
			return result;
		}

	},

	NOMAD_OCTED_BINARY_TUTORIAL_PMM_MINER {

		@Override
		public MiningAlgorithm create(Workspace workspace) {
			RandomizedLocalPureSubgroupSearch pmmSampler = (RandomizedLocalPureSubgroupSearch) PMM_SAMPLER
					.create(workspace);

			ArrayList<ParameterWrapper> wrappers = new ArrayList<ParameterWrapper>();
			RangeEnumerableParameter<List<Attribute<? extends Object>>> targetAttributeParameter = Parameters
					.rangeEnumerableParameter(identifier("target"), "Target variable",
							"Variable with respect to which subgroup utility is determined.", List.class,
							() -> ImmutableList.of(ImmutableList.of(pmmSampler.datatable().current().attribute(1)),
									ImmutableList.of(pmmSampler.datatable().current().attribute(2)),
									ImmutableList.of(pmmSampler.datatable().current().attribute(0))),
							pmmSampler.datatable());

			RangeEnumerableParameter<Supplier<ModelFactory<?>>> simplifiedModelClassParam = Parameters
					.rangeEnumerableParameter(pmmSampler.model().id(), pmmSampler.model().getName(),
							pmmSampler.model().getDescription(), pmmSampler.model().getType(),
							() -> (targetAttributeParameter.current().get(0) instanceof MetricAttribute)
									? ImmutableList.of(pmmSampler.model().empirical_distribution_option)
									: ImmutableList.of(pmmSampler.model().contingency_table_option),
							() -> true, targetAttributeParameter);

			wrappers.add(new SimpleParameterAdapter<List<Attribute<?>>>(pmmSampler.targets(), targetAttributeParameter,
					SimpleParameterAdapter.INNERCHANGEOPTION_ISSUE_WARNING));
			wrappers.add(new SimpleParameterAdapter<>(pmmSampler.model(), simplifiedModelClassParam,
					SimpleParameterAdapter.INNERCHANGEOPTION_PROPAGATE_TO_OUTER));
			wrappers.add(new TrivialParameterAdapter(pmmSampler.descriptorAttributeFilter()));
			wrappers.add(new TrivialParameterAdapter(pmmSampler.purityMeasure()));
			wrappers.add(new TrivialParameterAdapter(pmmSampler.targetFunction()));
			wrappers.add(new TrivialParameterAdapter(pmmSampler.numberOfResults()));
			wrappers.add(new TrivialParameterAdapter(pmmSampler.numberOfSeeds()));
			MiningAlgorithm result = DerivedAlgorithms.getAlgorithmWithWrappedParameters(pmmSampler, wrappers,
					DerivedAlgorithms.HIDE_AND_WARN, "Randomized Pure Subgroup Discovery");
			result.findParameterByName("Number of seeds").setByString("2000");
			result.findParameterByName("Attribute filter").setByString(
					"[(EA(B)-IP(B))/rp(A)^2,|rs(A)-rp(B)|/exp(rs(A)),|rp(B)-rs(B)|/exp(rd(A)+rs(B)),r_sigma,r_pi]");
			return result;
		}

	},

	NOMAD_GOLD_TUTORIAL_PMM_MINER {

		@Override
		public MiningAlgorithm create(Workspace workspace) {
			RandomizedLocalPureSubgroupSearch pmmSampler = (RandomizedLocalPureSubgroupSearch) PMM_SAMPLER
					.create(workspace);

			ArrayList<ParameterWrapper> wrappers = new ArrayList<ParameterWrapper>();
			RangeEnumerableParameter<List<Attribute<? extends Object>>> targetAttributeParameter = Parameters
					.rangeEnumerableParameter(identifier("target"), "Target variable",
							"Variable with respect to which subgroup utility is determined.", List.class, () -> {
								DataTable table = pmmSampler.datatable().current();
								return ImmutableList.of(ImmutableList.of(table.attribute(13)),
										ImmutableList.of(table.attribute(12)), ImmutableList.of(table.attribute(19)),
										ImmutableList.of(table.attribute(11), table.attribute(19)),
										ImmutableList.of(table.attribute(1), table.attribute(20)));
							}, pmmSampler.datatable());

			RangeEnumerableParameter<Supplier<ModelFactory<?>>> simplifiedModelClassParam = Parameters
					.rangeEnumerableParameter(pmmSampler.model().id(), pmmSampler.model().getName(),
							pmmSampler.model().getDescription(), pmmSampler.model().getType(),
							() -> (targetAttributeParameter.current().get(0) instanceof MetricAttribute)
									? ImmutableList.of(pmmSampler.model().empirical_distribution_option)
									: ImmutableList.of(pmmSampler.model().contingency_table_option),
							() -> true, targetAttributeParameter);

			wrappers.add(new SimpleParameterAdapter<List<Attribute<?>>>(pmmSampler.targets(), targetAttributeParameter,
					SimpleParameterAdapter.INNERCHANGEOPTION_ISSUE_WARNING));
			wrappers.add(new SimpleParameterAdapter<>(pmmSampler.model(), simplifiedModelClassParam,
					SimpleParameterAdapter.INNERCHANGEOPTION_PROPAGATE_TO_OUTER));
			wrappers.add(new TrivialParameterAdapter(pmmSampler.descriptorAttributeFilter()));
			wrappers.add(new TrivialParameterAdapter(pmmSampler.purityMeasure()));
			wrappers.add(new TrivialParameterAdapter(pmmSampler.targetFunction()));
			wrappers.add(new TrivialParameterAdapter(pmmSampler.numberOfResults()));
			wrappers.add(new TrivialParameterAdapter(pmmSampler.numberOfSeeds()));
			MiningAlgorithm result = DerivedAlgorithms.getAlgorithmWithWrappedParameters(pmmSampler, wrappers,
					DerivedAlgorithms.HIDE_AND_WARN, "Randomized Pure Subgroup Discovery");
			result.findParameterByName("Number of results").setByString("50");
			result.findParameterByName("Number of seeds").setByString("500");

			// result.findParameterByName("Attribute filter").setByString(
			// "[(EA(B)-IP(B))/rp(A)^2,|rs(A)-rp(B)|/exp(rs(A)),|rp(B)-rs(B)|/exp(rd(A)+rs(B)),r_sigma,r_pi]");
			return result;
		}

	},

	/**
	 * Constructs simple version of emm sampler where all algorithmic parameters are
	 * fixed and the only visible application parameters are the selection of the
	 * target and the descriptor attributes.
	 * 
	 * WARNING: this is specifically constructed for the bike dataset
	 */
	CJS_DEMO_MINER {

		@Override
		public MiningAlgorithm create(Workspace workspace) {
			ExceptionalSubgroupSampler emmSampler = (ExceptionalSubgroupSampler) EMM_SAMPLER.create(workspace);
			ArrayList<ParameterWrapper> wrappers = new ArrayList<ParameterWrapper>();
			wrappers.add(new SimpleParameterAdapter<List<Attribute<?>>>(emmSampler.targetAttributesParameter(),
					EMMParameters.getEMMTargetAttributesParameter(emmSampler.dataTableParameter(),
							attribute -> attribute.caption().equals("casual")
									|| attribute.caption().equals("registered")),
					SimpleParameterAdapter.INNERCHANGEOPTION_PROPAGATE_TO_OUTER));
			wrappers.add(new TrivialParameterAdapter(emmSampler.descriptionAttributesParameter()));
			wrappers.add(new StringValueTerminator(emmSampler.modelClassParameter(),
					() -> MetricEmpiricalDistributionFactory.STRING_NAME));
			wrappers.add(new StringValueTerminator(emmSampler.modelDistanceFunctionParameter(),
					() -> CumulativeJensenShannonDivergence.STRING_NAME));
			wrappers.add(new StringValueTerminator(emmSampler.objectiveFunctionParameter(),
					() -> "H(frequency) times deviation"));
			return DerivedAlgorithms.getAlgorithmWithWrappedParameters(emmSampler, wrappers,
					DerivedAlgorithms.HIDE_AND_WARN, "Randomized CJS-based Subgroup Discovery");
		}
	},

	CJS_TARGET_STUDY_SAMPLER {

		@Override
		public MiningAlgorithm create(Workspace workspace) {
			ExceptionalSubgroupSampler emmSampler = (ExceptionalSubgroupSampler) EMM_SAMPLER.create(workspace);
			ArrayList<ParameterWrapper> wrappers = new ArrayList<ParameterWrapper>();
			wrappers.add(new SimpleParameterAdapter<List<Attribute<?>>>(emmSampler.targetAttributesParameter(),
					EMMParameters.getEMMTargetAttributesParameter(emmSampler.dataTableParameter(),
							attribute -> attribute.caption().equals("casual")
									|| attribute.caption().equals("registered")),
					SimpleParameterAdapter.INNERCHANGEOPTION_PROPAGATE_TO_OUTER));
			wrappers.add(new TrivialParameterAdapter(emmSampler.descriptionAttributesParameter()));
			wrappers.add(new StringValueTerminator(emmSampler.modelClassParameter(),
					() -> MetricEmpiricalDistributionFactory.STRING_NAME));
			wrappers.add(new StringValueTerminator(emmSampler.modelDistanceFunctionParameter(),
					() -> CumulativeJensenShannonDivergence.STRING_NAME));
			wrappers.add(new TrivialParameterAdapter(emmSampler.numberOfResultsParameter()));
			wrappers.add(new TrivialParameterAdapter(emmSampler.objectiveFunctionParameter()));
			return DerivedAlgorithms.getAlgorithmWithWrappedParameters((StoppableMiningAlgorithm) emmSampler, wrappers,
					DerivedAlgorithms.HIDE_AND_WARN, "Randomized Subgroup Discovery");
		}

	},

	CJS_CONTROL_STUDY_SAMPLER {

		@Override
		public MiningAlgorithm create(Workspace workspace) {
			ExceptionalSubgroupSampler emmSampler = (ExceptionalSubgroupSampler) EMM_SAMPLER.create(workspace);
			ArrayList<ParameterWrapper> wrappers = new ArrayList<ParameterWrapper>();
			wrappers.add(new SimpleParameterAdapter<List<Attribute<?>>>(emmSampler.targetAttributesParameter(),
					EMMParameters.getEMMTargetAttributesParameter(emmSampler.dataTableParameter(),
							attribute -> attribute.caption().equals("casual")
									|| attribute.caption().equals("registered")),
					SimpleParameterAdapter.INNERCHANGEOPTION_PROPAGATE_TO_OUTER));
			wrappers.add(new TrivialParameterAdapter(emmSampler.descriptionAttributesParameter()));
			wrappers.add(new SimpleParameterAdapter<>(emmSampler.modelClassParameter(),
					Parameters.filteredRangeEnumerableParameter(emmSampler.modelClassParameter(), ImmutableList
							.of(GaussianModelFactory.INSTANCE.toString(), FixedShapeWeibullModelFactory.STRING_NAME))));

			wrappers.add(new TrivialParameterAdapter(emmSampler.modelDistanceFunctionParameter()));
			wrappers.add(new TrivialParameterAdapter(emmSampler.numberOfResultsParameter()));
			wrappers.add(new TrivialParameterAdapter(emmSampler.objectiveFunctionParameter()));
			return DerivedAlgorithms.getAlgorithmWithWrappedParameters((StoppableMiningAlgorithm) emmSampler, wrappers,
					DerivedAlgorithms.HIDE_AND_WARN, "Randomized Subgroup Discovery*");
		}

	}

	/*
	 * DUMMY_CRASH_PARAMCHOOSING_MINER {
	 * 
	 * @Override public MiningAlgorithm create(DataWorkspace dataTub) {
	 * List<ParameterSelector> list = new ArrayList<>(); return new
	 * ParameterChoosingMetaAlgorithm(dataTub, new DummyCrashMiner(dataTub), list);
	 * } };
	 */
	;

	@Override
	public abstract MiningAlgorithm create(Workspace workspace);

}

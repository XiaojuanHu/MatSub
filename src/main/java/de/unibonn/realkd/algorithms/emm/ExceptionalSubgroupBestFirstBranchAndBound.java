/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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

import static de.unibonn.realkd.algorithms.branchbound.BranchAndBoundSearch.closedDescriptorsExpander;
import static de.unibonn.realkd.algorithms.branchbound.BranchAndBoundSearch.minimalGeneratorsExpander;
import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.parameter.Parameters.doubleParameter;
import static de.unibonn.realkd.patterns.emm.ExceptionalModelMining.extensionDescriptorToEmmPatternMap;
import static de.unibonn.realkd.patterns.emm.NormalizedAbsoluteMedianShift.NORMALIZED_ABSOLUTE_MEDIAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedNegativeMeanShift.NORMALIZED_NEGATIVE_MEAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedNegativeMedianShift.NORMALIZED_NEGATIVE_MEDIAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedPositiveMedianShift.NORMALIZED_POSITIVE_MEDIAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.PositiveProbabilityShift.POSITIVE_PROBABILITY_SHIFT;
import static de.unibonn.realkd.patterns.emm.ReliableConditionalEffect.RELIABLE_CONDITIONAL_EFFECT;
import static de.unibonn.realkd.patterns.subgroups.MedianDeviationReduction.AVERAGE_ABSOLUTE_MEDIAN_DEVIATION_REDUCTION;
import static de.unibonn.realkd.util.Predicates.inClosedRange;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.util.stream.IntStream.iterate;
import static java.util.stream.IntStream.range;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.branchbound.BestFirstBranchAndBound;
import de.unibonn.realkd.algorithms.branchbound.BranchAndBoundSearch;
import de.unibonn.realkd.algorithms.branchbound.BranchAndBoundSearch.BranchAndBoundSearchNode;
import de.unibonn.realkd.algorithms.branchbound.BranchAndBoundSearch.LcmSearchNode;
import de.unibonn.realkd.algorithms.branchbound.BranchAndBoundSearch.LogicalDescriptorWithValidAugmentationsNode;
import de.unibonn.realkd.algorithms.branchbound.OptimisticEstimators;
import de.unibonn.realkd.algorithms.common.MiningParameters;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;
import de.unibonn.realkd.common.parameter.SubCollectionParameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.computations.dag.DagSearch;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.propositions.Propositions;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.Frequency;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.emm.ExceptionalModelMining;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;
import de.unibonn.realkd.patterns.emm.ModelDeviationMeasure;
import de.unibonn.realkd.patterns.emm.NormalizedNegativeMedianShift;
import de.unibonn.realkd.patterns.emm.NormalizedPositiveMeanShift;
import de.unibonn.realkd.patterns.emm.ReliableConditionalEffect;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.models.Model;
import de.unibonn.realkd.patterns.models.ModelFactory;
import de.unibonn.realkd.patterns.models.bernoulli.BernoulliDistribution;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistributionFactory;
import de.unibonn.realkd.patterns.models.table.ContingencyTableModelFactory;
import de.unibonn.realkd.patterns.subgroups.ReferenceDescriptor;
import de.unibonn.realkd.patterns.subgroups.RepresentativenessMeasure;

/**
 *
 * 
 * @author Mario Boley
 * 
 * @author Janis Kalofolias
 * 
 * @author Kailash Budhathoki
 * 
 * @since 0.4.0
 * 
 * @since 0.5.1
 *
 */
public class ExceptionalSubgroupBestFirstBranchAndBound extends AbstractMiningAlgorithm<ExceptionalModelPattern>
		implements DagSearch<Collection<? extends Pattern<?>>> {
	
	private static final Logger LOGGER=Logger.getLogger(ExceptionalSubgroupBestFirstBranchAndBound.class.getName());

	private static final List<Optional<Integer>> DEPTH_LIMIT_OPTIONS = IntStream.rangeClosed(0, 20)
			.mapToObj((IntFunction<Optional<Integer>>) i -> i == 0 ? Optional.empty() : Optional.of(i))
			.collect(Collectors.toList());
	private final Parameter<DataTable> dataTable;
	private final Parameter<PropositionalContext> propLogic;
	private final Parameter<List<Attribute<? extends Object>>> targets;

	private final ModelClassParameter modelClass;
	private final RangeEnumerableParameter<ModelDeviationMeasure> distanceFunction;
	private final RangeEnumerableParameter<Option<ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>>>> coverageFunction;
	private final Parameter<Optional<? extends Attribute<?>>> controlVariable;
	private final Parameter<Double> coverageWeight;
	private final Parameter<Double> controlWeight;
	private final SubCollectionParameter<Attribute<?>, Set<Attribute<?>>> filteredOutDescriptorAttributes;
	private final RangeEnumerableParameter<PatternLanguageOption<? extends BranchAndBoundSearchNode<ExceptionalModelPattern>>> language;
	private final Parameter<Integer> numberOfResults;
	private final RangeEnumerableParameter<OptimisticEstimators.OptimisticEstimatorOption> optimisticEstimator;
	private final RangeEnumerableParameter<Optional<Integer>> depthLimit;
	private final Parameter<Double> approximationFactor;

	private BestFirstBranchAndBound<ExceptionalModelPattern, ? extends BranchAndBoundSearchNode<ExceptionalModelPattern>> bestFirstBranchAndBound;

	public ExceptionalSubgroupBestFirstBranchAndBound(Workspace workspace) {
		this.dataTable = MiningParameters.dataTableParameter(workspace);
		this.targets = EMMParameters.getEMMTargetAttributesParameter(dataTable);
		this.modelClass = new ModelClassParameter(targets);
		this.distanceFunction = EMMParameters.distanceFunctionParameter(modelClass);
		this.coverageFunction = Parameters.rangeEnumerableParameter(id("cov_func"), "Coverage function",
				"The function used to measure the size of sub-populations.", OptimisticEstimators.OptimisticEstimatorOption.class,
				() -> validCoverageFunctionOptions(),
				() -> !distanceFunction.isValid() || validCoverageFunctionOptions().size() == 1 ? true : false,
				distanceFunction);
		this.controlVariable = EMMParameters.controlAttributeParameter(dataTable, targets);
		this.coverageWeight = doubleParameter(id("cov_weight"), "Coverage weight",
				"Power of the coverage factor in the optimization function.",
				() -> (distanceFunction.current().equals(RELIABLE_CONDITIONAL_EFFECT) ? 0.0 : 1.0),
				inClosedRange(0.0, 2.0)
						.and(d -> !distanceFunction.current().equals(RELIABLE_CONDITIONAL_EFFECT) || d == 0.0),
				"Must be between 0 and 2 (or 0 for reliable conditional effect measure).",
				() -> distanceFunction.isValid() && distanceFunction.current().equals(RELIABLE_CONDITIONAL_EFFECT),
				distanceFunction);
		this.controlWeight = doubleParameter(id("ctr_weight"), "Control weight",
				"Power of the control factor in the optimization function.", 1.0, x -> x >= 0, "Must be positive.",
				() -> !controlVariable.isValid() || !controlVariable.current().isPresent());

		this.filteredOutDescriptorAttributes = EMMParameters.getEMMDescriptorAttributesParameter(dataTable, targets,
				controlVariable);
		this.propLogic = MiningParameters.matchingPropositionalLogicParameter(workspace, dataTable);
		this.language = Parameters.rangeEnumerableParameter(id("descr_lang"), "Descriptor language",
				"Descriptor language to be searched by algorithm.", PatternLanguageOption.class,
				() -> ImmutableList.of(languageOptionClosed, languageOptionGenerators, languageOptionAll), () -> true);
		this.numberOfResults = Parameters.integerParameter(id("num_res"), "Number of results",
				"The number of optimal subgroups to be found. No subgroup outside results that will have larger objective value than pattern within result set (subject to approximation factor).",
				1, n -> n > 0, "Choose positive integer.");
		this.optimisticEstimator = Parameters.rangeEnumerableParameter(id("oest"), "Optimistic estimator",
				"Function used for pruning the search space.", OptimisticEstimators.OptimisticEstimatorOption.class, () -> validOptimisticEstimatorOptions(),
				distanceFunction);
		this.approximationFactor = doubleParameter(id("apx_fac"), "Approximation factor",
				"For approximation factor x algorithm guarantees to return solution with optimization value at least x times the optimal value.",
				1.0, a -> a > 0 && a <= 1, "Value must be between 0 (exclusive) and 1 (inclusive).");
		this.depthLimit = Parameters.rangeEnumerableParameter(id("max_depth"), "Depth limit",
				"The maximum depth in the refinement tree to be expanded by the algorithm.", Optional.class,
				() -> DEPTH_LIMIT_OPTIONS);
		registerParameter(dataTable);
		registerParameter(targets);
		registerParameter(modelClass);
		registerParameter(distanceFunction);
		registerParameter(coverageFunction);
		registerParameter(controlVariable);
		registerParameter(coverageWeight);
		registerParameter(controlWeight);
		registerParameter(propLogic);
		registerParameter(language);
		registerParameter(filteredOutDescriptorAttributes);
		registerParameter(numberOfResults);
		registerParameter(optimisticEstimator);
		registerParameter(approximationFactor);
		registerParameter(depthLimit);
	}

	@Override
	public String caption() {
		return "Exhaustive Exceptional Subgroup Discovery";
	}

	@Override
	public String description() {
		return "";
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.EXCEPTIONAL_SUBGROUP_DISCOVERY;
	}

	private static interface Option<T> {

		public boolean valid();

		public T get();

	}
	
	

	// public abstract class ReferenceOption {
	//
	// public abstract Function<LogicalDescriptor, ReferenceDescriptor>
	// selectorToReferenceDescriptor();
	//
	// public abstract Function<LogicalDescriptor, Model>
	// selectorToReferenceModel();
	//
	// }
	//
	// public RangeEnumerableParameter<ReferenceChoice> referenceParameter() {
	// return null;
	// }

	private final OptimisticEstimators.OptimisticEstimatorOption oestOptionCoverage = new OptimisticEstimators.OptimisticEstimatorOption() {

		@Override
		public boolean valid(ModelDeviationMeasure deviationMeasure) {
			return true;
		}

		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			return n -> pow(((ExceptionalModelPattern) n.content).value(Frequency.FREQUENCY), coverageWeight.current());
		}

		@Override
		public String toString() {
			return "coverage only";
		}

	};

	private final OptimisticEstimators.OptimisticEstimatorOption oestBalancedCoveragePositiveMeanShift = new OptimisticEstimators.OptimisticEstimatorOption() {

		@Override
		public boolean valid(ModelDeviationMeasure deviationMeasure) {
			if (!distanceFunction.current().getMeasure()
					.equals(NormalizedPositiveMeanShift.NORMALIZED_POSITIVE_MEAN_SHIFT))
				return false;
			if (!(controlVariable.current().isPresent()
					&& controlVariable.current().get() instanceof CategoricAttribute<?>))
				return false;
			CategoricAttribute<?> control = (CategoricAttribute<?>) controlVariable.current().get();
			List<Double> prbCat = control.categoryFrequencies();
			if (prbCat.size() != 2)
				return false;
			if (coverageWeight.current().doubleValue() != 1.0)
				return false;
			return true;
		}

		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			MetricAttribute target = (MetricAttribute) targets.current().get(0);
			CategoricAttribute<?> control = (CategoricAttribute<?>) controlVariable.current().get();
			PopulationData dataPop = BalancedCoveragePositiveMeanShiftOptimisticEstimator.makePopulationData(target,
					control);
			BalancedCoveragePositiveMeanShiftOptimisticEstimator oeBCPMS = new BalancedCoveragePositiveMeanShiftOptimisticEstimator(
					dataPop);
			oeBCPMS.setExponentRepr(controlWeight.current());
			OptimisticEstimatorAdaptor oeAdaptor = new OptimisticEstimatorAdaptor(dataPop, oeBCPMS);
			return oeAdaptor;
		}

		@Override
		public String toString() {
			return "balanced coverage and positive mean shift";
		}

	};

	private final OptimisticEstimators.OptimisticEstimatorOption oestOptionCoveragePosMeanShift = new OptimisticEstimators.OptimisticEstimatorOption() {

		@Override
		public boolean valid(ModelDeviationMeasure deviationMeasure) {
			return distanceFunction.current().getMeasure()
					.equals(NormalizedPositiveMeanShift.NORMALIZED_POSITIVE_MEAN_SHIFT);
		}

		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			return weightedPositiveMeanShiftOptimisticEstimator(dataTable.current().population(),
					(MetricAttribute) targets.current().get(0), coverageWeight.current());
		}

		@Override
		public String toString() {
			return "coverage and positive mean shift";
		}

	};

	private final OptimisticEstimators.OptimisticEstimatorOption oestOptionCoverageNegMeanShift = new OptimisticEstimators.OptimisticEstimatorOption() {

		@Override
		public boolean valid(ModelDeviationMeasure deviationMeasure) {
			return distanceFunction.current().getMeasure().equals(NORMALIZED_NEGATIVE_MEAN_SHIFT);
		}

		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			return weightedNegativeMeanShiftOptimisticEstimator(dataTable.current().population(),
					(MetricAttribute) targets.current().get(0), coverageWeight.current());
		}

		@Override
		public String toString() {
			return "coverage and negative mean shift";
		}

	};

	private final OptimisticEstimators.OptimisticEstimatorOption oestOptionCoverageNegMedianShift = new OptimisticEstimators.OptimisticEstimatorOption() {

		@Override
		public boolean valid(ModelDeviationMeasure deviationMeasure) {
			return distanceFunction.current().getMeasure().equals(NORMALIZED_NEGATIVE_MEDIAN_SHIFT);
		}

		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			return weightedNegativeMedianShiftOptimisticEstimator(dataTable.current().population(),
					(MetricAttribute) targets.current().get(0), coverageWeight.current());
		}

		@Override
		public String toString() {
			return "coverage and negative median shift";
		}

	};

	private final OptimisticEstimators.OptimisticEstimatorOption oestOptionCoveragePosMedianShift = new OptimisticEstimators.OptimisticEstimatorOption() {

		@Override
		public boolean valid(ModelDeviationMeasure deviationMeasure) {
			return distanceFunction.current().getMeasure().equals(NORMALIZED_POSITIVE_MEDIAN_SHIFT);
		}

		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			return weightedPositiveMedianShiftOptimisticEstimator(dataTable.current().population(),
					(MetricAttribute) targets.current().get(0), coverageWeight.current());
		}

		@Override
		public String toString() {
			return "coverage and positive median shift";
		}

	};

	private final OptimisticEstimators.OptimisticEstimatorOption oestOptionCoveragePosMedianShiftAmdReduction = new OptimisticEstimators.OptimisticEstimatorOption() {

		@Override
		public boolean valid(ModelDeviationMeasure deviationMeasure) {
			return distanceFunction.current().getMeasure().equals(NORMALIZED_POSITIVE_MEDIAN_SHIFT)
					&& coverageFunction.current().equals(coverageFunctionOptionAamdCorrectedCoverage);
		}

		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			return weightedAmdCorrectedPositiveMedianShiftOptimisticEstimator(dataTable.current().population(),
					(MetricAttribute) targets.current().get(0), coverageWeight.current());
		}

		@Override
		public String toString() {
			return "corrected coverage and positive median shift";
		}

	};

	private final OptimisticEstimators.OptimisticEstimatorOption oestOptionCoverageNegMedianShiftAmdReduction = new OptimisticEstimators.OptimisticEstimatorOption() {

		@Override
		public boolean valid(ModelDeviationMeasure deviationMeasure) {
			return distanceFunction.current().getMeasure()
					.equals(NormalizedNegativeMedianShift.NORMALIZED_NEGATIVE_MEDIAN_SHIFT)
					&& coverageFunction.current().equals(coverageFunctionOptionAamdCorrectedCoverage);
		}

		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			return weightedAmdCorrectedNegativeMedianShiftOptimisticEstimator(dataTable.current().population(),
					(MetricAttribute) targets.current().get(0), coverageWeight.current());
		}

		@Override
		public String toString() {
			return "corrected coverage and negative median shift";
		}

	};
	
	private final OptimisticEstimators.OptimisticEstimatorOption rateTightOest = new OptimisticEstimators.RceTightOptimisticEstimator();
	
	private final OptimisticEstimators.OptimisticEstimatorOption oestFuccap = new OptimisticEstimators.RceLooseOptimisticEstimator();
			
	private final OptimisticEstimators.OptimisticEstimatorOption oestOptionBinaryTarget = new OptimisticEstimators.OptimisticEstimatorOption() {

		@Override
		public boolean valid(ModelDeviationMeasure deviationMeasure) {
			return distanceFunction.current().getMeasure().equals(POSITIVE_PROBABILITY_SHIFT);
		}

		private double bound(ExceptionalModelPattern pattern) {
			BernoulliDistribution localModel = (BernoulliDistribution) pattern.descriptor().localModel();
			BernoulliDistribution globalModel = (BernoulliDistribution) pattern.descriptor().referenceModel();
			double posCount = (pattern.descriptor().supportSet().size() * localModel.probability());
			return (1 - globalModel.probability()) * pow(
					posCount / pattern.descriptor().getTargetTable().population().size(), coverageWeight.current());
		}

		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			return n -> bound(n.content);
		}

		@Override
		public String toString() {
			return "coverage and pos. prob.";
		}

	};

	private final List<OptimisticEstimators.OptimisticEstimatorOption> optimisticEstimatorOptions = ImmutableList
			.of(oestOptionBinaryTarget, rateTightOest, oestFuccap, oestOptionCoveragePosMedianShiftAmdReduction,
					oestOptionCoverageNegMedianShiftAmdReduction, oestOptionCoveragePosMeanShift,
					oestOptionCoverageNegMeanShift, oestBalancedCoveragePositiveMeanShift,
					oestOptionCoveragePosMedianShift, oestOptionCoverageNegMedianShift, oestOptionCoverage);

	private List<OptimisticEstimators.OptimisticEstimatorOption> validOptimisticEstimatorOptions() {
		return optimisticEstimatorOptions.stream().filter(o -> o.valid(distanceFunction.current())).collect(Collectors.toList());
	}

	private final Option<ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>>> coverageFunctionOptionCoverage = new Option<ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>>>() {

		@Override
		public boolean valid() {
			return true;
		}

		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			return n -> n.content.value(Frequency.FREQUENCY);
		}

		@Override
		public String toString() {
			return "coverage";
		}

	};

	private final Option<ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>>> coverageFunctionOptionAamdCorrectedCoverage = new Option<ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>>>() {

		@Override
		public String toString() {
			return "amd-corrected coverage";
		}

		@Override
		public boolean valid() {
			return ImmutableSet.of(NORMALIZED_ABSOLUTE_MEDIAN_SHIFT, NORMALIZED_POSITIVE_MEDIAN_SHIFT,
					NORMALIZED_NEGATIVE_MEDIAN_SHIFT).contains(distanceFunction.current().getMeasure());
		}

		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			return n -> {
				double aamdGain = n.content.value(AVERAGE_ABSOLUTE_MEDIAN_DEVIATION_REDUCTION);
				return n.content.value(Frequency.FREQUENCY) * aamdGain;
			};
		}

	};

	private final List<Option<ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>>>> coverageFunctionOptions = ImmutableList
			.of(coverageFunctionOptionCoverage, coverageFunctionOptionAamdCorrectedCoverage);

	private List<Option<ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>>>> validCoverageFunctionOptions() {
		return coverageFunctionOptions.stream().filter(o -> o.valid()).collect(Collectors.toList());
	}

	private interface PatternLanguageOption<T extends BranchAndBoundSearchNode<ExceptionalModelPattern>> {

		BestFirstBranchAndBound<ExceptionalModelPattern, ? extends BranchAndBoundSearchNode<ExceptionalModelPattern>> branchAndBoundSearch(
				Function<LogicalDescriptor, ExceptionalModelPattern> toEmmPattern, Predicate<Proposition> filter,
				ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> f,
				ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> oest);

	}

	private PatternLanguageOption<LcmSearchNode<ExceptionalModelPattern>> languageOptionClosed = new PatternLanguageOption<LcmSearchNode<ExceptionalModelPattern>>() {

		@Override
		public BestFirstBranchAndBound<ExceptionalModelPattern, ? extends BranchAndBoundSearchNode<ExceptionalModelPattern>> branchAndBoundSearch(
				Function<LogicalDescriptor, ExceptionalModelPattern> toEmmPattern, Predicate<Proposition> filter,
				ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> f,
				ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> oest) {
			Function<LcmSearchNode<ExceptionalModelPattern>, Collection<LcmSearchNode<ExceptionalModelPattern>>> refinementOperator = closedDescriptorsExpander(
					propLogic.current(), filter, toEmmPattern);
			LcmSearchNode<ExceptionalModelPattern> rootNode = BranchAndBoundSearch.lcmRootNode(propLogic.current(),
					filter, toEmmPattern);
			return new BestFirstBranchAndBound<>(n -> n.content, refinementOperator, rootNode, f, oest,
					numberOfResults.current(), approximationFactor.current(), depthLimit.current());
		}

		public String toString() {
			return "Closed conjunctions";
		}

	};

	private PatternLanguageOption<LogicalDescriptorWithValidAugmentationsNode<ExceptionalModelPattern>> languageOptionGenerators = new PatternLanguageOption<LogicalDescriptorWithValidAugmentationsNode<ExceptionalModelPattern>>() {

		@Override
		public BestFirstBranchAndBound<ExceptionalModelPattern, ? extends BranchAndBoundSearchNode<ExceptionalModelPattern>> branchAndBoundSearch(
				Function<LogicalDescriptor, ExceptionalModelPattern> toEmmPattern, Predicate<Proposition> filter,
				ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> f,
				ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> oest) {
			Function<LogicalDescriptorWithValidAugmentationsNode<ExceptionalModelPattern>, Collection<LogicalDescriptorWithValidAugmentationsNode<ExceptionalModelPattern>>> expander = minimalGeneratorsExpander(
					propLogic.current(), toEmmPattern);
			LogicalDescriptorWithValidAugmentationsNode<ExceptionalModelPattern> rootNode = BranchAndBoundSearch
					.minimalGeneratorRootNode(propLogic.current(), filter, toEmmPattern);
			return new BestFirstBranchAndBound<>(n -> n.content, expander, rootNode, f, oest, numberOfResults.current(),
					approximationFactor.current(), depthLimit.current());
		}

		public String toString() {
			return "Minimal conjunctions";
		}

	};

	private PatternLanguageOption<BranchAndBoundSearchNode<ExceptionalModelPattern>> languageOptionAll = new PatternLanguageOption<BranchAndBoundSearchNode<ExceptionalModelPattern>>() {

		@Override
		public BestFirstBranchAndBound<ExceptionalModelPattern, ? extends BranchAndBoundSearchNode<ExceptionalModelPattern>> branchAndBoundSearch(
				Function<LogicalDescriptor, ExceptionalModelPattern> toEmmPattern, Predicate<Proposition> filter,
				ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> f,
				ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> oest) {
			Function<LcmSearchNode<ExceptionalModelPattern>, Collection<LcmSearchNode<ExceptionalModelPattern>>> succ = BranchAndBoundSearch
					.allDescriptorsExpander(propLogic.current(), filter, toEmmPattern);
			LcmSearchNode<ExceptionalModelPattern> rootNode = BranchAndBoundSearch
					.allDescriptorRootNode(propLogic.current(), toEmmPattern);
			return new BestFirstBranchAndBound<>(n -> n.content, succ, rootNode, f, oest, numberOfResults.current(),
					approximationFactor.current(), depthLimit.current());
		}

		public String toString() {
			return "All conjunctions";
		}

	};

	private ExceptionalModelPattern simplification(ExceptionalModelPattern x) {
		return ExceptionalModelMining.emmPattern(x.descriptor().greedySimplification(), distanceFunction.current(),
				ImmutableList.of());
	}

	public double objectiveValue(BranchAndBoundSearchNode<ExceptionalModelPattern> node) {
		final double covRaw = coverageFunction.current().get().applyAsDouble(node);
		final double covValue = pow(covRaw, coverageWeight.current());
		final double tendValue = node.content.value(node.content.getDeviationMeasure());
		final double reprRaw = representativenessValue(node);
		final double reprValue = pow(reprRaw, controlWeight.current());
		final double value = covValue * tendValue * reprValue;
		// System.err.println("Found objective value: "+value);
		return value;
	}

	@Override
	protected Collection<ExceptionalModelPattern> concreteCall() throws ValidationException {
		Function<LogicalDescriptor, ReferenceDescriptor> descriptorToReferenceDescriptor;
		Function<ReferenceDescriptor, Model> selectorToReferenceModel;

		if (distanceFunction.current().equals(ReliableConditionalEffect.RELIABLE_CONDITIONAL_EFFECT)) {
			descriptorToReferenceDescriptor = d -> ReferenceDescriptor.complement(d);
			selectorToReferenceModel = r -> modelClass.current().get().getModel(dataTable.current(), targets.current(),
					r.supportSet());
		} else {
			Model globalModel = modelClass.current().get().getModel(dataTable.current(), targets.current());
			descriptorToReferenceDescriptor = d -> ReferenceDescriptor.global(dataTable.current().population());
			selectorToReferenceModel = r -> globalModel;
		}

		Function<LogicalDescriptor, ExceptionalModelPattern> toEmmPattern;
		if (controlVariable.current().isPresent()) {
			ModelFactory<?> controlModelFactory = (controlVariable.current().get() instanceof MetricAttribute)
					? MetricEmpiricalDistributionFactory.INSTANCE
					: ContingencyTableModelFactory.INSTANCE;
			toEmmPattern = ExceptionalModelMining.extensionDescriptorToControlledEmmPatternMap(dataTable.current(),
					targets.current(), modelClass.current().get(), distanceFunction.current(),
					ImmutableList.of(controlVariable.current().get()), controlModelFactory);
		} else {
			toEmmPattern = extensionDescriptorToEmmPatternMap(dataTable.current(), targets.current(),
					modelClass.current().get(), selectorToReferenceModel, descriptorToReferenceDescriptor,
					distanceFunction.current(), ImmutableList.of());
		}

		Predicate<Proposition> additionalPropFilter = prop -> !((prop instanceof AttributeBasedProposition)
				&& filteredOutDescriptorAttributes.current()
						.contains(((AttributeBasedProposition<?>) prop).attribute()));

		Predicate<Proposition> filter = additionalPropFilter
				.and(Propositions.isNotRelatedTo(dataTable.current(), targets.current()));

		// TODO: Exported as member. Ask Mario if good practice.
		// ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> f
		// = n -> {
		// final double value =
		// pow(coverageFunction.current().get().applyAsDouble(n),
		// coverageWeight.current())
		// * n.content.value(n.content.getDeviationMeasure())
		// * pow(representativenessValue(n), controlWeight.current());
		// return value;
		// };

		bestFirstBranchAndBound = language.current().branchAndBoundSearch(toEmmPattern, filter, this::objectiveValue,
				optimisticEstimator.current().get());

		Collection<ExceptionalModelPattern> result = bestFirstBranchAndBound.call();

		if (language.current() == languageOptionClosed) {
			LOGGER.info("Start post-processing");
			result = result.stream().map(p -> (ExceptionalModelPattern) p).map(this::simplification)
					.collect(Collectors.toList());
			LOGGER.info("Done post-processing");
		}

		return result;
	}

	public Double representativenessValue(BranchAndBoundSearchNode<ExceptionalModelPattern> n) {
		final Optional<Measurement> measurement = n.content.measurement(RepresentativenessMeasure.class);
		return measurement.map(m -> m.value()).orElse(1.0);
	}

	@Override
	protected void onStopRequest() {
		if (bestFirstBranchAndBound != null) {
			bestFirstBranchAndBound.requestStop();
		}
	}

	private static ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> weightedPositiveMeanShiftOptimisticEstimator(
			Population globalPopulation, MetricAttribute target, double alpha) {
		IntToDoubleFunction h = powerScaledCoverageFunction(globalPopulation, alpha);
		DoubleUnaryOperator u = normalizedPositiveMeanShift(target);
		return new TopKMeanOptimisticEstimator(h, u);
	}

	private static ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> weightedPositiveMedianShiftOptimisticEstimator(
			Population globalPopulation, MetricAttribute target, double alpha) {
		IntToDoubleFunction h = powerScaledCoverageFunction(globalPopulation, alpha);
		DoubleUnaryOperator u = normalizedPositiveMedianShift(target);
		return new TopKMedianOptimisticEstimator(h, u);
	}

	private static ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> weightedAmdCorrectedPositiveMedianShiftOptimisticEstimator(
			Population globalPopulation, MetricAttribute target, double alpha) {
		IntToDoubleFunction g = powerScaledCoverageFunction(globalPopulation, alpha);
		DoubleUnaryOperator h = powerScaledNormalizedAverageAbsoluteMedianDeviationReduction(target, alpha);
		DoubleUnaryOperator u = normalizedPositiveMedianShift(target);
		MedianSequenceOptimisticEstimator.ScanOrder order = MedianSequenceOptimisticEstimator.ScanOrder.DECREASING;
		return new MedianSequenceOptimisticEstimator(g, u, h, order);
	}

	private static ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> weightedAmdCorrectedNegativeMedianShiftOptimisticEstimator(
			Population globalPopulation, MetricAttribute target, double alpha) {
		IntToDoubleFunction g = powerScaledCoverageFunction(globalPopulation, alpha);
		DoubleUnaryOperator h = powerScaledNormalizedAverageAbsoluteMedianDeviationReduction(target, alpha);
		DoubleUnaryOperator u = normalizedNegativeMedianShift(target);
		MedianSequenceOptimisticEstimator.ScanOrder order = MedianSequenceOptimisticEstimator.ScanOrder.INCREASING;
		return new MedianSequenceOptimisticEstimator(g, u, h, order);
	}

	private static ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> weightedNegativeMedianShiftOptimisticEstimator(
			Population globalPopulation, MetricAttribute target, double alpha) {
		IntToDoubleFunction h = powerScaledCoverageFunction(globalPopulation, alpha);
		DoubleUnaryOperator u = normalizedNegativeMedianShift(target);
		return new BottomKMedianOptimisticEstimator(h, u);
	}

	private static ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> weightedNegativeMeanShiftOptimisticEstimator(
			Population globalPopulation, MetricAttribute target, double alpha) {
		IntToDoubleFunction h = powerScaledCoverageFunction(globalPopulation, alpha);
		DoubleUnaryOperator u = normalizedNegativeMeanShift(target);
		return new BottomKMeanOptimisticEstimator(h, u);
	}

	private static final class TopKMeanOptimisticEstimator
			implements ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> {

		private final IntToDoubleFunction h;

		private final DoubleUnaryOperator u;

		public TopKMeanOptimisticEstimator(IntToDoubleFunction h, DoubleUnaryOperator u) {
			this.h = h;
			this.u = u;
		}

		@Override
		public double applyAsDouble(BranchAndBoundSearchNode<ExceptionalModelPattern> node) {
			MetricAttribute target = (MetricAttribute) node.content.descriptor().targetAttributes().get(0);
			double[] valuesIncreasing = target.sortedNonMissingRowIndices().stream()
					.filter(i -> node.content.descriptor().supportSet().contains(i)).mapToDouble(i -> target.value(i))
					.toArray();
			double incrementalAverage = 0;
			double best = Double.NEGATIVE_INFINITY;
			for (int i = 1; i <= valuesIncreasing.length; i++) {
				incrementalAverage = ((i - 1) * incrementalAverage + valuesIncreasing[valuesIncreasing.length - i]) / i;
				best = max(best, h.applyAsDouble(i) * u.applyAsDouble(incrementalAverage));
			}
			return best;
		}

	}

	private static final class BottomKMeanOptimisticEstimator
			implements ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> {

		private final IntToDoubleFunction h;

		private final DoubleUnaryOperator u;

		public BottomKMeanOptimisticEstimator(IntToDoubleFunction h, DoubleUnaryOperator u) {
			this.h = h;
			this.u = u;
		}

		@Override
		public double applyAsDouble(BranchAndBoundSearchNode<ExceptionalModelPattern> node) {
			MetricAttribute target = (MetricAttribute) node.content.descriptor().targetAttributes().get(0);
			double[] valuesIncreasing = target.sortedNonMissingRowIndices().stream()
					.filter(i -> node.content.descriptor().supportSet().contains(i)).mapToDouble(i -> target.value(i))
					.toArray();
			double incrementalAverage = 0;
			double best = Double.NEGATIVE_INFINITY;
			for (int i = 1; i <= valuesIncreasing.length; i++) {
				incrementalAverage = ((i - 1) * incrementalAverage + valuesIncreasing[i - 1]) / i;
				best = max(best, h.applyAsDouble(i) * u.applyAsDouble(incrementalAverage));
			}
			return best;
		}

	}

	private static final class BottomKMedianOptimisticEstimator
			implements ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> {

		private final IntToDoubleFunction h;

		private final DoubleUnaryOperator u;

		public BottomKMedianOptimisticEstimator(IntToDoubleFunction h, DoubleUnaryOperator u) {
			this.h = h;
			this.u = u;
		}

		@Override
		public double applyAsDouble(BranchAndBoundSearchNode<ExceptionalModelPattern> node) {
			MetricAttribute target = (MetricAttribute) node.content.descriptor().targetAttributes().get(0);
			double best = Double.NEGATIVE_INFINITY;
			double[] orderedValues = target.sortedNonMissingRowIndices().stream()
					.filter(i -> node.content.descriptor().supportSet().contains(i)).mapToDouble(i -> target.value(i))
					.toArray();
			for (int i = 1; i <= orderedValues.length / 2; i++) {
				double median = orderedValues[i - 1];
				int size = 2 * i - 1;
				double value = h.applyAsDouble(size) * u.applyAsDouble(median);
				best = max(best, value);
			}
			return best;
		}

	}

	private static final class TopKMedianOptimisticEstimator
			implements ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> {

		private final IntToDoubleFunction h;

		private final DoubleUnaryOperator u;

		public TopKMedianOptimisticEstimator(IntToDoubleFunction h, DoubleUnaryOperator u) {
			this.h = h;
			this.u = u;
		}

		@Override
		public double applyAsDouble(BranchAndBoundSearchNode<ExceptionalModelPattern> node) {
			MetricAttribute target = (MetricAttribute) node.content.descriptor().targetAttributes().get(0);
			double best = Double.NEGATIVE_INFINITY;
			double[] orderedValues = target.sortedNonMissingRowIndices().stream()
					.filter(i -> node.content.descriptor().supportSet().contains(i)).mapToDouble(i -> target.value(i))
					.toArray();
			for (int i = 1; i <= orderedValues.length / 2; i++) {
				double ithMedianFromTop = orderedValues[orderedValues.length - i];
				int maxSizeOfSetWithMedianI = 2 * i - 1;
				double value = h.applyAsDouble(maxSizeOfSetWithMedianI) * u.applyAsDouble(ithMedianFromTop);
				best = max(best, value);
			}
			return best;
		}

	}

	private static final class MedianSequenceOptimisticEstimator
			implements ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> {

		public static enum ScanOrder implements IntFunction<OfInt> {

			DECREASING {
				@Override
				public OfInt apply(int m) {
					return iterate(m - 1, z -> z - 1).limit(m).iterator();
				}
			},
			INCREASING {
				@Override
				public OfInt apply(int m) {
					return range(0, m).iterator();
				}
			};

			@Override
			public abstract OfInt apply(int m);

		}

		private final IntToDoubleFunction g;

		private final DoubleUnaryOperator u;

		private final DoubleUnaryOperator h;

		private final ScanOrder order;

		public MedianSequenceOptimisticEstimator(IntToDoubleFunction g, DoubleUnaryOperator u, DoubleUnaryOperator h,
				ScanOrder order) {
			this.g = g;
			this.u = u;
			this.h = h;
			this.order = order;
		}

		@Override
		public double applyAsDouble(BranchAndBoundSearchNode<ExceptionalModelPattern> node) {
			MetricAttribute target = (MetricAttribute) node.content.descriptor().targetAttributes().get(0);
			double best = NEGATIVE_INFINITY;
			double[] orderedValues = target.sortedNonMissingRowIndices().stream()
					.filter(i -> node.content.descriptor().supportSet().contains(i)).mapToDouble(i -> target.value(i))
					.toArray();

			if (orderedValues.length == 0) {
				return NEGATIVE_INFINITY;
			}

			double[] leftDeviations = leftDeviations(orderedValues);
			double[] rightDeviations = rightDeviations(orderedValues);
			int kStar = 0; // size of opt set,,, should be 1???
			int m = orderedValues.length;
			// iterate over all relevant median indices
			for (OfInt medianIndices = order.apply(m); medianIndices.hasNext();) {
				int z = medianIndices.nextInt();
				double median = orderedValues[z];
				double medianUtility = u.applyAsDouble(median);
				if (medianUtility == 0) {
					return best;
				}
				int kStarOld = kStar;
				// (z+1) elements are contained in the set up to including z.
				// (m-z-1) elements are in the set from to down to excluding z.
				int maxK = min(2 * (z + 1), 2 * (m - z - 1) + 1);
				double bestForI = NEGATIVE_INFINITY;

				// iterate over all relevant subset sizes
				for (int k = max(kStarOld - 3, 1); k <= min(kStarOld + 3, maxK); k++) {
					int a = z - (k - 1) / 2;
					int b = z + (int) Math.ceil((k - 1) / 2.0);
					double smd = sumOfMedianDeviations(z, a, b, orderedValues, leftDeviations, rightDeviations);
					double value = g.applyAsDouble(k) * medianUtility * h.applyAsDouble(smd / k);
					kStar = (value > bestForI) ? k : kStar;
					bestForI = max(bestForI, value);
					best = max(best, value);
				}
			}
			return best;
		}

		private double sumOfMedianDeviations(int i, int a, int b, double[] orderedVals, double[] leftDevs,
				double[] rightDevs) {
			int m = orderedVals.length;
			double d_ai = orderedVals[i] - orderedVals[a];
			double d_ib = orderedVals[b] - orderedVals[i];
			return leftDevs[i] - leftDevs[a] - (a - 1) * d_ai + rightDevs[i] - rightDevs[b] - (m - b) * d_ib;
		}

		private double[] leftDeviations(double[] orderedValues) {
			double[] leftDeviations = new double[orderedValues.length];
			leftDeviations[0] = 0;
			for (int i = 1; i < leftDeviations.length; i++) {
				// note that factor i in last term of following line has to be i
				// (instead of i-1 in the paper) because we have 0-based
				// indexing here; factor has to be the number of elements left
				// to index i
				leftDeviations[i] = leftDeviations[i - 1] + i * (orderedValues[i] - orderedValues[i - 1]);
			}
			return leftDeviations;
		}

		private double[] rightDeviations(double[] orderedValues) {
			double[] rightDeviations = new double[orderedValues.length];
			rightDeviations[rightDeviations.length - 1] = 0;
			for (int i = rightDeviations.length - 2; i >= 0; i--) {
				// factor of last term looks different than in paper; see
				// comment above
				rightDeviations[i] = rightDeviations[i + 1]
						+ (rightDeviations.length - i - 1) * (orderedValues[i + 1] - orderedValues[i]);
			}
			return rightDeviations;
		}

	}

	private static DoubleUnaryOperator normalizedPositiveMeanShift(MetricAttribute target) {
		double a = target.mean();
		double b = target.max() - target.mean();
		return x -> max((x - a) / b, 0);
	}

	private static DoubleUnaryOperator normalizedNegativeMeanShift(MetricAttribute target) {
		double a = target.mean();
		double b = target.mean() - target.min();
		return x -> max((a - x) / b, 0);
	}

	private static DoubleUnaryOperator normalizedNegativeMedianShift(MetricAttribute target) {
		double a = target.median();
		double b = target.median() - target.min();
		return x -> max((a - x) / b, 0);
	}

	private static DoubleUnaryOperator normalizedPositiveMedianShift(MetricAttribute target) {
		double a = target.median();
		double b = target.max() - target.median();
		return x -> max((x - a) / b, 0);
	}

	private static DoubleUnaryOperator powerScaledNormalizedAverageAbsoluteMedianDeviationReduction(
			MetricAttribute target, double alpha) {
		double a = target.averageAbsoluteMedianDeviation();
		return x -> Math.pow(max((a - x) / a, 0), alpha);
	}

	private static IntToDoubleFunction powerScaledCoverageFunction(Population globalPopulation, double alpha) {
		int m = globalPopulation.size();
		return x -> pow((double) x / m, alpha);
	}

	@Override
	public int nodesCreated() {
		return bestFirstBranchAndBound.nodesCreated();
	}

	@Override
	public int nodesDiscarded() {
		return bestFirstBranchAndBound.nodesDiscarded();
	}

	@Override
	public int boundarySize() {
		return bestFirstBranchAndBound.boundarySize();
	}

	@Override
	public int maxAttainedBoundarySize() {
		return bestFirstBranchAndBound.maxAttainedBoundarySize();
	}

	@Override
	public int maxAttainedDepth() {
		return bestFirstBranchAndBound.maxAttainedDepth();
	}

	@Override
	public int bestDepth() {
		return bestFirstBranchAndBound.bestDepth();
	}

}

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
package de.unibonn.realkd.algorithms.functional;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.parameter.Parameters.rangeEnumerableParameter;
import static de.unibonn.realkd.common.parameter.Parameters.subSetParameter;
import static de.unibonn.realkd.patterns.functional.CoDomainAmbiguityCount.CODOMAIN_AMBIGUITY_COUNT;
import static de.unibonn.realkd.patterns.functional.ExpectedMutualInformation.EXPECTED_MUTUAL_INFORMATION;
import static de.unibonn.realkd.patterns.functional.FunctionalPatterns.binaryAttributeSetRelation;
import static de.unibonn.realkd.patterns.functional.FunctionalPatterns.functionalPattern;
import static de.unibonn.realkd.patterns.functional.ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION;
import static de.unibonn.realkd.util.Arrays.filteredRange;
import static de.unibonn.realkd.util.Predicates.notSatisfied;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.branchbound.OPUS;
import de.unibonn.realkd.algorithms.branchbound.OPUS.OperatorOrder;
import de.unibonn.realkd.algorithms.branchbound.OPUS.TraverseOrder;
import de.unibonn.realkd.algorithms.common.MiningParameters;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.computations.dag.DagSearch;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.functional.BinaryAttributeSetRelation;
import de.unibonn.realkd.patterns.functional.FunctionalPattern;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;
import de.unibonn.realkd.patterns.models.table.ContingencyTables;

/**
 * @author Panagiotis Mandros
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public class OPUSFunctionalPatternSearch extends AbstractMiningAlgorithm<FunctionalPattern>
		implements FunctionalPatternSearch, DagSearch<Collection<? extends Pattern<?>>> {

	private static final Logger LOGGER = Logger.getLogger(OPUSFunctionalPatternSearch.class.getName());

	private OPUS<FunctionalPattern, SearchNode> opus;

	private List<Attribute<?>> listOfAttributes;

	private final Parameter<DataTable> datatableParameter;

	private final Parameter<Attribute<?>> targetAttributeParameter;

	private final Parameter<Set<Attribute<?>>> attributeFilter;

	private final Parameter<Integer> numberOfResults;

	private final Parameter<Double> alpha;

	private final Parameter<TraverseOrder> traverseOrderOption;

	private final Parameter<OperatorOrder> operatorOrder;

	private final Parameter<LanguageOption> languageOption;

	private final Parameter<OptimisticEstimatorOption> optimisticOption;

	public OPUSFunctionalPatternSearch(Workspace workspace) {
		datatableParameter = MiningParameters.dataTableParameter(workspace);

		targetAttributeParameter = rangeEnumerableParameter(id("targets"), "Target attribute",
				"The target to find dependencies on", Attribute.class,
				() -> datatableParameter.current().attributes().stream()
						.filter(a -> a instanceof CategoricAttribute || a instanceof OrdinalAttribute<?>)
						.collect(Collectors.toList()),
				datatableParameter);

		attributeFilter = subSetParameter(id("attr_filter"), "Attribute filter",
				"Attributes that are ignored during search.",
				() -> datatableParameter.current().attributes().stream()
						.filter(a -> a != targetAttributeParameter.current() && !datatableParameter.current()
								.containsDependencyBetween(a, targetAttributeParameter.current()))
						.collect(toSet()),
				targetAttributeParameter);

		numberOfResults = Parameters.integerParameter(id("num_res"), "Number of results",
				"Number of results, i.e., the size of the result queue.", 1, n -> n > 0, "Specify positive integer.");

		alpha = Parameters.doubleParameter(id("apx_fac"), "alpha", "alpha-approximation of the best possible solution",
				1, n -> n > 0 && n <= 1, "Specify number greater than 0, and smaller or equal to 1");

		traverseOrderOption = rangeEnumerableParameter(id("search_order"), "Traverse order",
				"Which traverse order to use", TraverseOrder.class, () -> asList(TraverseOrder.values()));

		operatorOrder = rangeEnumerableParameter(id("op_assignment"), "Operator assignment",
				"The way in which refinement operators are assigned to search nodes.", OPUS.OperatorOrder.class,
				() -> asList(OperatorOrder.values()));

		languageOption = rangeEnumerableParameter(id("lang"), "Language",
				"The language of function domains to be searched by algorithm", LanguageOption.class,
				() -> asList(LanguageOption.values()));

		optimisticOption = rangeEnumerableParameter(id("oest"), "Optimistic estimator",
				"Which optimistic estimator to use", OptimisticEstimatorOption.class,
				() -> asList(OptimisticEstimatorOption.values()));

		registerParameter(datatableParameter);
		registerParameter(targetAttributeParameter);
		registerParameter(attributeFilter);
		registerParameter(numberOfResults);
		registerParameter(alpha);
		registerParameter(traverseOrderOption);
		registerParameter(operatorOrder);
		registerParameter(languageOption);
		registerParameter(optimisticOption);
	}

	@Override
	public String caption() {
		return "Functional Pattern Discovery with OPUS";
	}

	@Override
	public String description() {
		return "";
	}

	@Override
	protected void onStopRequest() {
		if (opus != null) {
			opus.requestStop();
		}
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.FUNCTIONAL_PATTERN_DISCOVERY;
	}

	@Override
	public void target(Attribute<?> value) {
		targetAttributeParameter.set(value);
	}

	@Override
	public Attribute<?> target() {
		return targetAttributeParameter.current();
	}

	public DataTable dataTable() {
		return datatableParameter.current();
	}

	@Override
	public void topK(int k) {
		numberOfResults.set(k);
	}

	@Override
	public Integer topK() {
		return numberOfResults.current();
	}

	@Override
	public void alpha(double alpha) {
		this.alpha.set(alpha);
	}

	@Override
	public Double alpha() {
		return alpha.current();
	}

	public void traverseOrderOption(TraverseOrder order) {
		this.traverseOrderOption.set(order);
	}

	public TraverseOrder traverseOrderOption() {
		return traverseOrderOption.current();
	}

	public void operatorOrder(OperatorOrder order) {
		this.operatorOrder.set(order);
	}

	public OperatorOrder operatorOrder() {
		return operatorOrder.current();
	}

	public void languageOption(LanguageOption langOption) {
		this.languageOption.set(langOption);
	}

	public LanguageOption languageOption() {
		return languageOption.current();
	}

	public void optimisticOption(OptimisticEstimatorOption optimisticOption) {
		this.optimisticOption.set(optimisticOption);
	}

	public OptimisticEstimatorOption optimisticOption() {
		return optimisticOption.current();
	}

	@Override
	protected Collection<FunctionalPattern> concreteCall() throws ValidationException {

		// get the parameters
		DataTable dataTable = datatableParameter.current();
		Attribute<?> target = targetAttributeParameter.current();

		listOfAttributes = dataTable.attributes().stream()
				.filter(a -> (a instanceof CategoricAttribute || a instanceof OrdinalAttribute<?>) && !(a == target)
						&& !dataTable.containsDependencyBetween(a, target) && !attributeFilter.current().contains(a))
				.collect(Collectors.toList());

		Set<Function<? super SearchNode, ? extends SearchNode>> ops = IntStream.range(0, listOfAttributes.size())
				.mapToObj(i -> (Function<SearchNode, SearchNode>) (n -> n.refine(i))).collect(Collectors.toSet());

		FunctionalPattern rootPattern = functionalPattern(
				binaryAttributeSetRelation(dataTable, ImmutableSet.of(), ImmutableSet.of(target())));
		ContingencyTable marginalY = rootPattern.descriptor().contingencyTable().marginal(0);

		double entropyY = marginalY.entropy();
		opus = new OPUS<FunctionalPattern, SearchNode>(n -> n.pattern, ops,
				new SearchNode(rootPattern, marginalY, entropyY), f, optimisticOption().fOEst(), topK(), alpha(),
				Optional.empty(), operatorOrder(), traverseOrderOption.current(),
				languageOption.current().additionalPruningCriterion());

		Collection<FunctionalPattern> result = opus.call();
		return result;
	}

	private class SearchNode {

		public final FunctionalPattern pattern;
		public final ContingencyTable marginalY;
		public final double entropyY;

		public SearchNode(FunctionalPattern pattern, ContingencyTable marginalY, double entropyY) {
			this.pattern = pattern;
			this.marginalY = marginalY;
			this.entropyY = entropyY;
		}

		public String toString() {
			return pattern.toString();
		}

		public SearchNode refine(int i) {
			Set<Attribute<?>> domainAttributes = pattern.descriptor().domain();
			FunctionalPattern newPattern;
			BinaryAttributeSetRelation newRelation;
			SearchNode newSearchNode;
			Builder<Attribute<?>> toBuild = ImmutableSet.<Attribute<?>>builder().addAll(domainAttributes)
					.add(listOfAttributes.get(i));
			ImmutableSet<Attribute<?>> newDomain = toBuild.build();
			newRelation = binaryAttributeSetRelation(dataTable(), newDomain, ImmutableSet.of(target()));
			Measurement[] additionalMeasurements = languageOption.current().additionalMeasurementProcedures().stream()
					.map(p -> p.perform(newRelation)).toArray(n -> new Measurement[n]);

			newPattern = functionalPattern(newRelation,
					RELIABLE_FRACTION_OF_INFORMATION.perform(newRelation, entropyY, marginalY), additionalMeasurements);
			newSearchNode = new SearchNode(newPattern, marginalY, entropyY);
			return newSearchNode;
		}
	}

	public static enum LanguageOption {

		ALL {
			@Override
			Predicate<SearchNode> additionalPruningCriterion() {
				return notSatisfied();
			}

			@Override
			ImmutableCollection<MeasurementProcedure<? extends Measure, ? super BinaryAttributeSetRelation>> additionalMeasurementProcedures() {
				return ImmutableList.of();
			}
		},

		// INTERMEDIATE {
		// private boolean canBePruned(SearchNode node) {
		// FunctionalPattern pattern = node.pattern;
		// double parentAmbiguityCount;
		// if (node.parentPattern.descriptor().domain().isEmpty()) {
		// List<Integer> partitionSizesY =
		// node.marginalY.nonZeroCells().stream()
		// .map(n -> node.marginalY.count(n)).collect(Collectors.toList());
		// parentAmbiguityCount =
		// Combinatorics.kPartiteEdgeCount(partitionSizesY);
		// } else {
		// parentAmbiguityCount =
		// node.parentPattern.value(CODOMAIN_AMBIGUITY_COUNT);
		// }
		//
		// if (pattern.value(CODOMAIN_AMBIGUITY_COUNT) % 1 == 0) {
		// if (parentAmbiguityCount == pattern.value(CODOMAIN_AMBIGUITY_COUNT))
		// {
		// return true;
		// }
		//
		// } else {
		// LOGGER.warning("CODOMAIN_AMBIGUITY_COUNT was not an integer");
		// }
		// return false;
		// }
		//
		// private final ImmutableCollection<MeasurementProcedure<? extends
		// Measure, ? super CorrelationDescriptor>> measurementProcedures =
		// ImmutableList
		// .of(CODOMAIN_AMBIGUITY_COUNT);
		//
		// @Override
		// Predicate<SearchNode> additionalPruningCriterion() {
		// return this::canBePruned;
		// }
		//
		// @Override
		// ImmutableCollection<MeasurementProcedure<? extends Measure, ? super
		// CorrelationDescriptor>> additionalMeasurementProcedures() {
		// return measurementProcedures;
		// }
		// },

		MINIMAL_DISAMBIGUATION {
			private boolean canBePruned(SearchNode node) {
				FunctionalPattern pattern = node.pattern;
				BinaryAttributeSetRelation descriptor = pattern.descriptor();
				ContingencyTable table = descriptor.contingencyTable();
				// for every X in curly X
				for (int i = 0; i < descriptor.domain().size(); i++) {
					final int l = i;
					// the indices to project on, i.e., exclude one X from curly
					// X and Y
					int[] projectionIndices = filteredRange(0,
							descriptor.domain().size() + descriptor.coDomain().size(), j -> j != l);
					// compute the projection
					ContingencyTable projection = table.marginal(projectionIndices);
					// remove Y
					int[] disambiguationIndices = IntStream.range(0, descriptor.domain().size() - 1).toArray();
					int projectionAmbiguityCount = projection.ambiguityCount(disambiguationIndices);
					if (pattern.value(CODOMAIN_AMBIGUITY_COUNT) % 1 == 0) {
						if (projectionAmbiguityCount == pattern.value(CODOMAIN_AMBIGUITY_COUNT)) {
							return true;
						}

					} else {
						LOGGER.warning("CODOMAIN_AMBIGUITY_COUNT was not an integer");
					}

				}
				return false;
			}

			private final ImmutableCollection<MeasurementProcedure<? extends Measure, ? super BinaryAttributeSetRelation>> measurementProcedures = ImmutableList
					.of(CODOMAIN_AMBIGUITY_COUNT);

			@Override
			Predicate<SearchNode> additionalPruningCriterion() {
				return this::canBePruned;
			}

			@Override
			ImmutableCollection<MeasurementProcedure<? extends Measure, ? super BinaryAttributeSetRelation>> additionalMeasurementProcedures() {
				return measurementProcedures;
			}
		};

		abstract Predicate<SearchNode> additionalPruningCriterion();

		abstract ImmutableCollection<MeasurementProcedure<? extends Measure, ? super BinaryAttributeSetRelation>> additionalMeasurementProcedures();

	}

	public static enum OptimisticEstimatorOption {

		MON {

			@Override
			List<ToDoubleFunction<? super SearchNode>> fOEst() {
				return ImmutableList.of(OptimisticEstimatorOption::simpleEstimator);
			}

		},
		SPC {

			@Override
			List<ToDoubleFunction<? super SearchNode>> fOEst() {
				return ImmutableList.of(OptimisticEstimatorOption::allPureCutEstimator);
			}
		},
		CHAIN {
			@Override
			List<ToDoubleFunction<? super SearchNode>> fOEst() {
				return ImmutableList.of(OptimisticEstimatorOption::simpleEstimator,
						OptimisticEstimatorOption::allPureCutEstimator);
			}
		};

		abstract List<ToDoubleFunction<? super SearchNode>> fOEst();

		private static double simpleEstimator(SearchNode node) {
			double expectedMI = node.pattern.value(EXPECTED_MUTUAL_INFORMATION);
			double entropyOfY = node.entropyY;
			return 1 - expectedMI / entropyOfY;
		}

		private static double allPureCutEstimator(SearchNode node) {
			BinaryAttributeSetRelation descriptor = node.pattern.descriptor();
			ContingencyTable table = descriptor.contingencyTable();
			return 1 - ContingencyTables.parallelExpectedMutualInformationUnderPermutationModel(node.marginalY, table)
					/ node.entropyY;
		}

	}

	private ToDoubleFunction<SearchNode> f = p -> {
		return p.pattern.value(p.pattern.functionalityMeasure());
	};

	@Override
	public int nodesCreated() {
		return opus.nodesCreated();
	}

	@Override
	public int nodesDiscarded() {
		return opus.nodesDiscarded();
	}

	public int nodesDiscardedPruningRules() {
		return opus.nodesDiscardedPruningRules();
	}

	@Override
	public int boundarySize() {
		return opus.boundarySize();
	}

	@Override
	public int maxAttainedBoundarySize() {
		return opus.maxAttainedBoundarySize();
	}

	@Override
	public int maxAttainedDepth() {
		return opus.maxAttainedDepth();
	}

	@Override
	public int bestDepth() {
		return opus.bestDepth();
	}

	public long runningTime() {
		long start = opus.startTime().orElse((long) 0);
		long end = opus.terminationTime().orElse((long) 0);
		return end - start;
	}

}

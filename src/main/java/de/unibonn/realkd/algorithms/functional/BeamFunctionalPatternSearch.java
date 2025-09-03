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
import static de.unibonn.realkd.patterns.functional.ExpectedMutualInformation.EXPECTED_MUTUAL_INFORMATION;
import static de.unibonn.realkd.patterns.functional.FunctionalPatterns.binaryAttributeSetRelation;
import static de.unibonn.realkd.patterns.functional.FunctionalPatterns.functionalPattern;
import static de.unibonn.realkd.patterns.functional.ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.beamsearch.NewBeamSearch;
import de.unibonn.realkd.algorithms.beamsearch.NewBeamSearch.RefinementPropagation;
import de.unibonn.realkd.algorithms.common.MiningParameters;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.computations.dag.DagSearch;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;
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
public class BeamFunctionalPatternSearch extends AbstractMiningAlgorithm<FunctionalPattern>
		implements DagSearch<Collection<? extends Pattern<?>>> {

	private NewBeamSearch<FunctionalPattern, SearchNode> beamSearch;

	private List<Attribute<?>> listOfAttributes;

	private final Parameter<DataTable> datatableParameter;

	private final Parameter<Attribute<?>> targetAttributeParameter;

	private final Parameter<Set<Attribute<?>>> attributeFilter;

	private final Parameter<Integer> numberOfResults;

	private final Parameter<Integer> beamWidth;

	private final Parameter<OptimisticEstimatorOption> optimisticOption;

	private final Parameter<RefinementPropagation> refPropOption;

	public BeamFunctionalPatternSearch(Workspace workspace) {
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

		beamWidth = Parameters.integerParameter(id("beam_width"), "beamWidth", "size of the beam", 5, n -> n > 0,
				"Specify positive integer.");

		optimisticOption = rangeEnumerableParameter(id("oest"), "Optimistic estimator",
				"Which optimistic estimator to use", OptimisticEstimatorOption.class,
				() -> asList(OptimisticEstimatorOption.values()));

		refPropOption = rangeEnumerableParameter(id("oest"), "Refinement propagation",
				"Which refinement propagation strategy to use", RefinementPropagation.class,
				() -> asList(RefinementPropagation.values()));

		registerParameter(datatableParameter);
		registerParameter(targetAttributeParameter);
		registerParameter(attributeFilter);
		registerParameter(numberOfResults);
		registerParameter(beamWidth);
		registerParameter(optimisticOption);
		registerParameter(refPropOption);
	}

	@Override
	public String caption() {
		return "Heuristic Functional Pattern Discovery";
	}

	@Override
	public String description() {
		return "";
	}

	@Override
	protected void onStopRequest() {
		if (beamSearch != null) {
			beamSearch.requestStop();
		}
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.FUNCTIONAL_PATTERN_DISCOVERY;
	}

	public void target(Attribute<?> value) {
		targetAttributeParameter.set(value);
	}

	public Attribute<?> target() {
		return targetAttributeParameter.current();
	}

	public DataTable dataTable() {
		return datatableParameter.current();
	}

	public void topK(int k) {
		numberOfResults.set(k);
	}

	public Integer topK() {
		return numberOfResults.current();
	}

	public void beamWidth(int beamWidth) {
		this.beamWidth.set(beamWidth);
	}

	public Integer beamWidth() {
		return beamWidth.current();
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
		beamSearch = new NewBeamSearch<FunctionalPattern, SearchNode>(n -> n.pattern, ops,
				new SearchNode(rootPattern, marginalY, entropyY), f, optimisticOption().fOEst(), topK(), beamWidth(),
				Optional.empty(), refPropOption.current());

		Collection<FunctionalPattern> result = beamSearch.call();
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

			newPattern = functionalPattern(newRelation,
					RELIABLE_FRACTION_OF_INFORMATION.perform(newRelation, entropyY, marginalY));

			newSearchNode = new SearchNode(newPattern, marginalY, entropyY);
			return newSearchNode;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof SearchNode)) {
				return false;
			}

			SearchNode that = (SearchNode) o;
			return this.pattern.equals(that.pattern);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.pattern);
		}

	}

	public static enum OptimisticEstimatorOption {
		CHAIN {
			@Override
			List<ToDoubleFunction<? super SearchNode>> fOEst() {
				return ImmutableList.of(OptimisticEstimatorOption::simpleEstimator,
						OptimisticEstimatorOption::allPureCutEstimator);
			}
		},
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
		NONE {
			@Override
			List<ToDoubleFunction<? super SearchNode>> fOEst() {
				return ImmutableList.of();
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
		return beamSearch.nodesCreated();
	}

	@Override
	public int nodesDiscarded() {
		return beamSearch.nodesDiscarded();
	}

	@Override
	public int boundarySize() {
		return beamSearch.boundarySize();
	}

	@Override
	public int maxAttainedBoundarySize() {
		return beamSearch.maxAttainedBoundarySize();
	}

	@Override
	public int maxAttainedDepth() {
		return beamSearch.maxAttainedDepth();
	}

	@Override
	public int bestDepth() {
		return beamSearch.bestDepth();
	}

	public long runningTime() {
		long start = beamSearch.startTime().orElse((long) 0);
		long end = beamSearch.terminationTime().orElse((long) 0);
		return end - start;
	}

}

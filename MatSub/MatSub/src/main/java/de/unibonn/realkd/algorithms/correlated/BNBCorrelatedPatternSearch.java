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
package de.unibonn.realkd.algorithms.correlated;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.parameter.Parameters.rangeEnumerableParameter;
import static de.unibonn.realkd.patterns.correlated.CorrelationPatterns.attributeSetRelation;
import static de.unibonn.realkd.patterns.correlated.CorrelationPatterns.correlationPattern;
import static de.unibonn.realkd.patterns.correlated.JointEntropy.JOINT_ENTROPY;
import static de.unibonn.realkd.patterns.correlated.ReliableNormalizedTotalCorrelation.RELIABLE_NORMALIZED_TOTAL_CORRELATION;
import static de.unibonn.realkd.patterns.correlated.ReliableNormalizedTotalCorrelationCorrectionTerm.RELIABLE_NORMALIZED_TOTAL_CORRELATION_CORRECTION_TERM;
import static de.unibonn.realkd.patterns.correlated.SumOfEntropies.SUM_OF_ENTROPIES;
import static de.unibonn.realkd.patterns.correlated.SumOfMutualInformations.SUM_OF_MUTUAL_INFORMATIONS;
import static de.unibonn.realkd.patterns.correlated.TotalCorrelationNormalizer.TOTAL_CORRELATION_NORMALIZER;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
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
import de.unibonn.realkd.algorithms.branchbound.BranchAndBound;
import de.unibonn.realkd.algorithms.branchbound.BranchAndBound.TraverseOrder;
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
import de.unibonn.realkd.patterns.correlated.AttributeSetRelation;
import de.unibonn.realkd.patterns.correlated.CorrelationPattern;
import de.unibonn.realkd.patterns.correlated.CorrelationPatterns;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;
import de.unibonn.realkd.patterns.models.table.ContingencyTables;

/**
 * @author Panagiotis Mandros
 * 
 *
 */
public class BNBCorrelatedPatternSearch extends AbstractMiningAlgorithm<CorrelationPattern>
		implements DagSearch<Collection<? extends Pattern<?>>> {

	private BranchAndBound<CorrelationPattern, SearchNode> bnb;

	private List<Attribute<?>> listOfAttributes;

	private List<Double> entropies;

	private final Parameter<DataTable> datatableParameter;

	private final Parameter<Integer> numberOfResults;

	private final Parameter<Double> alpha;

	private final Parameter<TraverseOrder> traverseOrderOption;

	private final Parameter<OptimisticEstimatorOption> optimisticOption;

	public BNBCorrelatedPatternSearch(Workspace workspace) {
		datatableParameter = MiningParameters.dataTableParameter(workspace);

		numberOfResults = Parameters.integerParameter(id("num_res"), "Number of results",
				"Number of results, i.e., the size of the result queue.", 1, n -> n > 0, "Specify positive integer.");

		alpha = Parameters.doubleParameter(id("apx_fac"), "alpha", "alpha-approximation of the best possible solution",
				1, n -> n > 0 && n <= 1, "Specify number greater than 0, and smaller or equal to 1");

		traverseOrderOption = rangeEnumerableParameter(id("search_order"), "Traverse order",
				"Which traverse order to use", TraverseOrder.class, () -> asList(TraverseOrder.values()));

		optimisticOption = rangeEnumerableParameter(id("oest"), "Optimistic estimator",
				"Which optimistic estimator to use", OptimisticEstimatorOption.class,
				() -> asList(OptimisticEstimatorOption.values()));

		registerParameter(datatableParameter);
		registerParameter(numberOfResults);
		registerParameter(alpha);
		registerParameter(traverseOrderOption);
		registerParameter(optimisticOption);
	}

	@Override
	public String caption() {
		return "Correlated pattern Discovery with OPUS";
	}

	@Override
	public String description() {
		return "";
	}

	@Override
	protected void onStopRequest() {
		if (bnb != null) {
			bnb.requestStop();
		}
	}

	@Override
	public AlgorithmCategory getCategory() {
		return null;
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

	public void alpha(double alpha) {
		this.alpha.set(alpha);
	}

	public double alpha() {
		return alpha.current();
	}

	public void traverseOrderOption(TraverseOrder order) {
		this.traverseOrderOption.set(order);
	}

	public TraverseOrder traverseOrderOption() {
		return traverseOrderOption.current();
	}

	public void optimisticOption(OptimisticEstimatorOption optimisticOption) {
		this.optimisticOption.set(optimisticOption);
	}

	public OptimisticEstimatorOption optimisticOption() {
		return optimisticOption.current();
	}

	@Override
	protected Collection<CorrelationPattern> concreteCall() throws ValidationException {
		DataTable dataTable = datatableParameter.current();

		// get attribute list
		listOfAttributes = dataTable.attributes().stream()
				.filter(a -> (a instanceof CategoricAttribute || a instanceof OrdinalAttribute<?>))
				.collect(Collectors.toList());
		int numAttributes = listOfAttributes.size();

		// get contingency table (to sort according to entropy)
		ContingencyTable nWayContingencyTable = ContingencyTables.contingencyTable(dataTable, listOfAttributes);
		entropies = IntStream.range(0, numAttributes).mapToObj(i -> nWayContingencyTable.marginal(i).entropy())
				.collect(Collectors.toList());
		int[] sortedIndices = IntStream.range(0, numAttributes).boxed()
				.sorted((i, j) -> entropies.get(j).compareTo(entropies.get(i))).mapToInt(ele -> ele).toArray();

		// get domain sizes (for the correction term)
		ArrayList<Integer> domainSizes = IntStream.range(0, numAttributes)
				.mapToObj(i -> nWayContingencyTable.marginal(i).nonZeroCells().size())
				.collect(Collectors.toCollection(ArrayList::new));

		// sort attributes and entropies in decreasing order of entropy
		listOfAttributes = returnSorted(listOfAttributes, sortedIndices);
		entropies = returnSorted(entropies, sortedIndices);

		// create operators with decreasing entropy order (that is why linkedhashset)
		LinkedHashSet<Function<? super SearchNode, ? extends SearchNode>> ops = IntStream.range(0, numAttributes)
				.mapToObj(i -> (Function<SearchNode, SearchNode>) (n -> n.refine(i)))
				.collect(Collectors.toCollection(LinkedHashSet::new));

		// create root patterns (all singletons)
		// ordered according to increasing entropy
		List<CorrelationPattern> rootPatterns = IntStream.range(0, numAttributes)
				.mapToObj(i -> correlationPattern(
						attributeSetRelation(dataTable, ImmutableSet.of(listOfAttributes.get(i))),
						RELIABLE_NORMALIZED_TOTAL_CORRELATION))
				.collect(Collectors.toList());

		// in order to get info for the refined bound
		double sum = entropies.stream().mapToDouble(Double::doubleValue).sum();
		double runningSum = 0;

		// cast them into search nodes
		int i = 0;
		List<SearchNode> searchNodes = new ArrayList<>();
		for (CorrelationPattern pattern : rootPatterns) {
			int attrIndex = sortedIndices[i];
			ArrayList<Integer> domSizes = new ArrayList<>();
			domSizes.add(domainSizes.get(attrIndex));
			runningSum += entropies.get(i);

			searchNodes.add(
					new SearchNode(pattern, pattern.value(SUM_OF_ENTROPIES), pattern.value(SUM_OF_MUTUAL_INFORMATIONS),
							domSizes, entropies.get(i), pattern.value(JOINT_ENTROPY), sum - runningSum));
			i++;
		}

		bnb = new BranchAndBound<CorrelationPattern, SearchNode>(n -> n.pattern, ops, searchNodes, f,
				optimisticOption().fOEst(), topK(), alpha(), Optional.empty(), traverseOrderOption.current());

		Collection<CorrelationPattern> result = bnb.call();
		return result;
	}

	private class SearchNode {
		public final CorrelationPattern pattern;
		public final double sumOfEntropies;
		public final double sumMutualInfos;
		public final ArrayList<Integer> domSizes;
		public final double maxEntropy;
		public final double jointEntropy;
		public final double remainingEntropiesSum;

		public SearchNode(CorrelationPattern pattern, double sumOfEntropies, double sumMutualInfos,
				ArrayList<Integer> domSizes, double maxEntropy, double jointEntropy, double remainingEntropiesSum) {
			this.pattern = pattern;
			this.sumOfEntropies = sumOfEntropies;
			this.sumMutualInfos = sumMutualInfos;
			this.domSizes = domSizes;
			this.maxEntropy = maxEntropy;
			this.jointEntropy = jointEntropy;
			this.remainingEntropiesSum = remainingEntropiesSum;
		}

		public String toString() {
			return pattern.toString();
		}

		//
		public SearchNode refine(int i) {
			Set<Attribute<?>> domainAttributes = pattern.descriptor().attributeSet();
			CorrelationPattern newPattern;
			AttributeSetRelation newRelation;
			Builder<Attribute<?>> toBuild = ImmutableSet.<Attribute<?>>builder().addAll(domainAttributes)
					.add(listOfAttributes.get(i));
			ImmutableSet<Attribute<?>> newAttributeSet = toBuild.build();
			newRelation = CorrelationPatterns.attributeSetRelation(dataTable(), newAttributeSet);

			// get table of last added variable
			ContingencyTable marginalTable = newRelation.nWayContingencyTable().marginal(domainAttributes.size());
			// get dom size of last added variable
			int newDomSize = marginalTable.domainSize();
			// get entropy of last added variable
			double newEntropy = marginalTable.entropy();

			// make the new list of domain sizes and sort
			ArrayList<Integer> newDomSizes = new ArrayList<>(domSizes);
			newDomSizes.add(newDomSize);
			Collections.sort(newDomSizes, Collections.reverseOrder());

			// get the sum of remaining entropies in branch (for the refined bound)
			double entropySumInBetween = 0;
			for (int j = i + 1; j < listOfAttributes.size(); j++) {
				entropySumInBetween += entropies.get(j);
			}

			newPattern = CorrelationPatterns.correlationPattern(newRelation,
					RELIABLE_NORMALIZED_TOTAL_CORRELATION.perform(newRelation, sumMutualInfos, sumOfEntropies,
							newDomSizes, newEntropy, maxEntropy, jointEntropy));

			SearchNode newSearchNode = new SearchNode(newPattern, newPattern.value(SUM_OF_ENTROPIES),
					newPattern.value(SUM_OF_MUTUAL_INFORMATIONS), newDomSizes, maxEntropy,
					newPattern.value(JOINT_ENTROPY), entropySumInBetween);

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

		MON {
			@Override
			List<ToDoubleFunction<? super SearchNode>> fOEst() {
				return ImmutableList.of(OptimisticEstimatorOption::trivialEstimator);
			}
		},
		CHAIN {
			@Override
			List<ToDoubleFunction<? super SearchNode>> fOEst() {
				return ImmutableList.of(OptimisticEstimatorOption::trivialEstimator,
						OptimisticEstimatorOption::refinedEstimator);
			}
		},

		NONE {
			@Override
			List<ToDoubleFunction<? super SearchNode>> fOEst() {
				return ImmutableList.of();
			}
		};

		abstract List<ToDoubleFunction<? super SearchNode>> fOEst();

		private static double trivialEstimator(SearchNode node) {
			if (node.pattern.descriptor().attributeSet().size() == 1) {
				return 1;
			}

			return 1 - node.pattern.value(RELIABLE_NORMALIZED_TOTAL_CORRELATION_CORRECTION_TERM);
		}

		private static double refinedEstimator(SearchNode node) {
			if (node.pattern.descriptor().attributeSet().size() == 1) {
				return 1;
			}

			double toreturn = (node.pattern.value(SUM_OF_MUTUAL_INFORMATIONS) + node.remainingEntropiesSum)
					/ (node.pattern.value(TOTAL_CORRELATION_NORMALIZER) + node.remainingEntropiesSum)
					- node.pattern.value(RELIABLE_NORMALIZED_TOTAL_CORRELATION_CORRECTION_TERM);
			return toreturn;
		}
	}

	private ToDoubleFunction<SearchNode> f = p -> {
		return p.pattern.value(p.pattern.correlationMeasure());
	};

	@Override
	public int nodesCreated() {
		return bnb.nodesCreated();
	}

	@Override
	public int nodesDiscarded() {
		return bnb.nodesDiscarded();
	}

	public int nodesDiscardedPruningRules() {
		return bnb.nodesDiscardedPruningRules();
	}

	@Override
	public int boundarySize() {
		return bnb.boundarySize();
	}

	@Override
	public int maxAttainedBoundarySize() {
		return bnb.maxAttainedBoundarySize();
	}

	@Override
	public int maxAttainedDepth() {
		return bnb.maxAttainedDepth();
	}

	@Override
	public int bestDepth() {
		return bnb.bestDepth();
	}

	public long runningTime() {
		long start = bnb.startTime().orElse((long) 0);
		long end = bnb.terminationTime().orElse((long) 0);
		return end - start;
	}

	private static <T> List<T> returnSorted(List<T> list, int[] indices) {
		return IntStream.range(0, list.size()).mapToObj(i -> list.get(indices[i])).collect(Collectors.toList());
	}

}

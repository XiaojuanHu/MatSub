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
import static de.unibonn.realkd.patterns.functional.CoDomainEntropy.CODOMAIN_ENTROPY;
import static de.unibonn.realkd.patterns.functional.ExpectedMutualInformation.EXPECTED_MUTUAL_INFORMATION;
import static de.unibonn.realkd.patterns.functional.FunctionalPatterns.binaryAttributeSetRelation;
import static de.unibonn.realkd.patterns.functional.FunctionalPatterns.functionalPattern;
import static de.unibonn.realkd.patterns.functional.ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.branchbound.BestFirstBranchAndBound;
import de.unibonn.realkd.algorithms.common.MiningParameters;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;
import de.unibonn.realkd.patterns.functional.BinaryAttributeSetRelation;
import de.unibonn.realkd.patterns.functional.FunctionalPattern;

/**
 * @author Panagiotis Mandros
 * 
 * @since 0.4.0
 * 
 * @version 0.5.0
 */
public class BranchAndBoundFunctionalPatternSearch extends AbstractMiningAlgorithm<FunctionalPattern>
		implements FunctionalPatternSearch {

	private BestFirstBranchAndBound<FunctionalPattern, SearchNode> branchAndBound;

	private List<Attribute<?>> listOfAttributes;

	private final Parameter<DataTable> datatableParameter;

	private final Parameter<Attribute<?>> targetAttributeParameter;

	private final Parameter<Integer> numberOfResults;

	private final Parameter<Double> alpha;

	public BranchAndBoundFunctionalPatternSearch(Workspace workspace) {
		datatableParameter = MiningParameters.dataTableParameter(workspace);

		targetAttributeParameter = rangeEnumerableParameter(id("targets"),"Target attribute", "", CategoricAttribute.class,
				() -> datatableParameter.current().attributes().stream()
						.filter(a -> (a instanceof CategoricAttribute || a instanceof OrdinalAttribute<?>))
						.collect(Collectors.toList()),
				datatableParameter);

		numberOfResults = Parameters.integerParameter(id("num_res"), "Number of results",
				"Number of results, i.e., the size of the result queue.", 1, n -> n > 0, "Specify positive integer.");

		alpha = Parameters.doubleParameter(id("apx_fac"), "alpha", "alpha-approximation of the best possible solution",
				1, n -> n > 0 && n <= 1, "Specify number greater than 0, and smaller or equal to 1");

	}

	@Override
	public String caption() {
		return "Branch and Bound Functional Pattern Discovery";
	}

	@Override
	public String description() {
		return "";
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

	@Override
	protected Collection<FunctionalPattern> concreteCall() throws ValidationException {

		// get the parameters
		DataTable dataTable = datatableParameter.current();
		Attribute<?> target = targetAttributeParameter.current();

		// filter out all non categorical attributes and the class variable
		// listOfAttributes = dataTable.attributes().stream()
		// .filter(a -> a instanceof CategoricAttribute && !(a == target)
		// && !dataTable.containsDependencyBetween(a, target))
		// .map(a -> (CategoricAttribute<?>) a).collect(Collectors.toList());
		listOfAttributes = dataTable.attributes().stream()
				.filter(a -> (a instanceof CategoricAttribute || a instanceof OrdinalAttribute<?>) && !(a == target))
				.collect(Collectors.toList());

		FunctionalPattern rootPattern = functionalPattern(
				binaryAttributeSetRelation(dataTable, ImmutableSet.of(), ImmutableSet.of(target())));
		branchAndBound = new BestFirstBranchAndBound<>(n -> n.pattern, refinementOperator,
				new SearchNode(0, rootPattern), f, fOEst, topK(), alpha(), Optional.empty());
		Collection<FunctionalPattern> result = branchAndBound.call();
		// System.out.println(branchAndBound.value);
		return result;
	}

	private class SearchNode {

		public final int min_augm_index;
		public final FunctionalPattern pattern;

		public SearchNode(int min_augm_index, FunctionalPattern pattern) {
			this.min_augm_index = min_augm_index;
			this.pattern = pattern;
		}

		public String toString() {
			return pattern.toString();
		}

		public Collection<SearchNode> expand() {
			int numberOfAttributes = listOfAttributes.size();
			Collection<SearchNode> allExpansions = new ArrayList<SearchNode>();
			Set<Attribute<?>> domainAttributes = pattern.descriptor().domain();
			FunctionalPattern newPattern;
			BinaryAttributeSetRelation newRelation;
			SearchNode newSearchNode;
			for (int i = min_augm_index; i < numberOfAttributes; i++) {
				Builder<Attribute<?>> toBuild = ImmutableSet.<Attribute<?>>builder();
				toBuild.addAll(domainAttributes);
				toBuild.add(listOfAttributes.get(i));
				ImmutableSet<Attribute<?>> newDomain = toBuild.build();
				newRelation = binaryAttributeSetRelation(dataTable(), newDomain, ImmutableSet.of(target()));
				newPattern = functionalPattern(newRelation, RELIABLE_FRACTION_OF_INFORMATION);
				newSearchNode = new SearchNode(i + 1, newPattern);
				allExpansions.add(newSearchNode);
			}

			return allExpansions;
		}
	}

	private Function<SearchNode, Collection<SearchNode>> refinementOperator = p -> {
		return p.expand();
	};

	private ToDoubleFunction<SearchNode> f = p -> {
		return p.pattern.value(p.pattern.functionalityMeasure());
	};

	private ToDoubleFunction<SearchNode> fOEst = p -> {
		double expectedMI = p.pattern.value(EXPECTED_MUTUAL_INFORMATION);
		double entropyOfY = p.pattern.value(CODOMAIN_ENTROPY);
		return 1 - expectedMI / entropyOfY;
	};

}

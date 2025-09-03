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

import static de.unibonn.realkd.algorithms.common.MiningParameters.dataTableParameter;
import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.parameter.Parameters.doubleParameter;
import static de.unibonn.realkd.common.parameter.Parameters.integerParameter;
import static de.unibonn.realkd.common.parameter.Parameters.rangeEnumerableParameter;
import static de.unibonn.realkd.patterns.functional.FractionOfInformation.FRACTION_OF_INFORMATION;
import static de.unibonn.realkd.patterns.functional.FunctionalPatterns.functionalPattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.patterns.functional.BinaryAttributeSetRelation;
import de.unibonn.realkd.patterns.functional.FunctionalPattern;
import de.unibonn.realkd.patterns.functional.FunctionalPatterns;

/**
 * @author Mario Boley
 * @author Panagiotis Mandros
 * 
 * @since 0.3.1
 * 
 * @version 0.3.1
 *
 */
public class BottomUpFunctionalPatternSearchWithSupportPruning extends AbstractMiningAlgorithm<FunctionalPattern>
		implements MiningAlgorithm {

	public static final String MINIMUM_FRACTION_OF_INFORMATION_PARAMETER_NAME = "Minimum fraction of information";

	private final Parameter<DataTable> datatableParameter;

	private final Parameter<CategoricAttribute<?>> targetAttributeParameter;

	private final Parameter<Double> fractionOfInformationThresholdParameter;

	private final Parameter<Integer> maxLatticeLevelParameter;

	public BottomUpFunctionalPatternSearchWithSupportPruning(Workspace workspace) {
		datatableParameter = dataTableParameter(workspace);

		targetAttributeParameter = rangeEnumerableParameter(id("targets"), "Target attribute", "",
				CategoricAttribute.class,
				() -> datatableParameter.current().attributes().stream().filter(a -> a instanceof CategoricAttribute)
						.map(a -> (CategoricAttribute<?>) a).collect(Collectors.toList()),
				datatableParameter);

		fractionOfInformationThresholdParameter = doubleParameter(id("min_frac_inf"),
				MINIMUM_FRACTION_OF_INFORMATION_PARAMETER_NAME,
				"The minimal value for the fraction of information of the domain about the codomain.", 0.8,
				t -> t > 0.0 && t <= 1.0, "Needs to be between 0 and 1.");

		maxLatticeLevelParameter = integerParameter(id("max_level"), "Max lattice search level",
				"Level up to which the search is allowed to go", workspace.datatables().get(0).numberOfAttributes() - 1,
				t -> t >= 1, "Needs to be greater or equal to 1.");

	}

	@Override
	public String caption() {
		return "Exhaustive Minimal Functional Pattern Discovery";
	}

	@Override
	public String description() {
		return "";
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.OTHER;
	}

	public void fractionOfInformationThreshold(double threshold) {
		this.fractionOfInformationThresholdParameter.set(threshold);
	}

	public void target(CategoricAttribute<?> value) {
		targetAttributeParameter.set(value);
	}

	public CategoricAttribute<?> target() {
		return targetAttributeParameter.current();
	}

	public void maxLatticeLevel(int value) {
		maxLatticeLevelParameter.set(value);
	}

	public DataTable dataTable() {
		return datatableParameter.current();
	}

	@Override
	protected List<FunctionalPattern> concreteCall() {
		// get the parameters
		DataTable dataTable = datatableParameter.current();
		CategoricAttribute<?> target = targetAttributeParameter.current();
		Double fractionOfInformationThreshold = fractionOfInformationThresholdParameter.current();
		Integer maxLatticeLevel = maxLatticeLevelParameter.current();

		// filter out all non categorical attributes and the class variable
		List<CategoricAttribute<?>> listOfAttributes = dataTable.attributes().stream()
				.filter(a -> a instanceof CategoricAttribute && !(a == target)).map(a -> (CategoricAttribute<?>) a)
				.collect(Collectors.toList());
		int numOfAttributes = listOfAttributes.size();

		Stack<ImmutableSet<Integer>> toCheck = new Stack<>();
		List<FunctionalPattern> solutions = new ArrayList<>();

		// add the singletons in the stack
		// we add the indices (zero indexed)
		for (int i = numOfAttributes - 1; i >= 0; i--) {
			toCheck.push(ImmutableSet.of(i));
		}
		// do the dfs
		Set<Integer> attributeSetToCheck;
		BinaryAttributeSetRelation relationToCheck;
		FunctionalPattern patternToCheck;
		double sigma;
		int max;
		while (!toCheck.isEmpty()) {
			// get the set to check by popping the stack
			attributeSetToCheck = toCheck.pop();

			// turn into a relation
			relationToCheck = createAttributeRelationFromIntegerSet(attributeSetToCheck, listOfAttributes);
			// make it a pattern
			patternToCheck = functionalPattern(relationToCheck, FRACTION_OF_INFORMATION);
			// calculate the sigma
			sigma = patternToCheck.value(patternToCheck.functionalityMeasure());

			// if bigger than threshold, then it is a minimal
			// one, and we add it to the solution set
			if (sigma >= fractionOfInformationThreshold) {
				solutions.add(patternToCheck);
			} else { // otherwise we have to expand (if it can be expanded)
				// if max level reached, stop expanding
				if (attributeSetToCheck.size() + 1 > maxLatticeLevel) {
					continue;
				} else {
					// get the maximum in lexicographic
					// order (here we have integers)
					max = Collections.max(attributeSetToCheck);
					if (max < (numOfAttributes - 1)) { // can be expanded
						// for every number between the max and the number of
						// features, create a new set (old+number) and push it
						for (int i = max + 1; i < numOfAttributes; i++) {
							ImmutableSet<Integer> newSetToCheck = ImmutableSet.<Integer>builder()
									.addAll(attributeSetToCheck).add(i).build();
							toCheck.push(newSetToCheck);
						}
					} else { // cannot be expanded, do nothing
					}
				}
			}
		}
		return solutions;
	}

	// @Override
	// protected List<Pattern<?>> concreteCall() {
	// // get the parameters
	// DataTable dataTable = datatableParameter.current();
	// CategoricAttribute<?> target = targetAttributeParameter.current();
	// Integer maxLatticeLevel = maxLatticeLevelParameter.current();
	//
	// // filter out all non categorical attributes and the class variable
	// List<CategoricAttribute<?>> listOfAttributes =
	// dataTable.attributes().stream()
	// .filter(a -> a instanceof CategoricAttribute && !(a == target)).map(a ->
	// (CategoricAttribute<?>) a)
	// .collect(Collectors.toList());
	// int numOfAttributes = listOfAttributes.size();
	//
	// // index the attributes with a hashmap
	// HashMap<Integer, CategoricAttribute<?>> indexToAttribute = new
	// HashMap<>();
	// for (int i = 0; i < numOfAttributes; i++) {
	// indexToAttribute.put(i, listOfAttributes.get(i));
	// }
	//
	// Stack<ImmutableSet<Integer>> toCheck = new Stack<>();
	// List<Pattern<?>> solutions = new ArrayList<>();
	//
	//// // add the singletons in the stack
	//// // we add the indices (zero indexed)
	//// for (int i = numOfAttributes - 1; i >= 0; i--) {
	//// toCheck.push(ImmutableSet.of(i));
	//// }
	// toCheck.push(ImmutableSet.of(5));
	// // do the dfs
	// AttributeRelation relationToCheck;
	// FunctionalPattern patternToCheck;
	// double sigma;
	// int max;
	//
	// Set<Integer> attributeSetToCheck;
	//
	// int q=7;
	// while (q<20) {
	// // get the set to check by popping the stack
	// attributeSetToCheck = toCheck.pop();
	// System.out.println(attributeSetToCheck.toString());
	// // turn into a relation
	// relationToCheck =
	// createAttributeRelationFromIntegerSet(attributeSetToCheck,
	// indexToAttribute);
	// // make it a pattern
	// patternToCheck = FunctionalPatterns.functionalPattern(relationToCheck);
	// // calculate the sigma
	//// sigma =
	// patternToCheck.value(FunctionalDependencyMeasure.FRACTION_OF_INFORMATION_CORRECTED);
	// sigma = patternToCheck.value(patternToCheck.functionalityMeasure());
	//
	// System.out.println("Sigma is: " + sigma);
	// // if bigger than threshold, then it is a minimal
	// // one, and we add it to the solution set
	//
	// solutions.add(patternToCheck);
	//
	//
	// // get the maximum in lexicographic
	//
	//
	// ImmutableSet<Integer> newSetToCheck = ImmutableSet.<Integer>builder()
	// .addAll(attributeSetToCheck).add(q).build();
	// toCheck.push(newSetToCheck);
	//
	//
	//
	// q++;
	// }
	// return solutions;
	// }

	/**
	 * 
	 * @param set
	 *            the set to turn into a domain
	 * @param listOfAttributes
	 *            the listOfAttributes which acts as an indexer (index to attribute)
	 * @return an attribute relation
	 */
	private BinaryAttributeSetRelation createAttributeRelationFromIntegerSet(Set<Integer> set,
			List<CategoricAttribute<?>> listOfAttributes) {

		Builder<Attribute<?>> toBuild = ImmutableSet.<Attribute<?>>builder();
		Iterator<Integer> iter = set.iterator();
		while (iter.hasNext()) {
			toBuild.add(listOfAttributes.get(iter.next()));
		}
		ImmutableSet<Attribute<?>> domain = toBuild.build();
		return FunctionalPatterns.binaryAttributeSetRelation(dataTable(), domain, ImmutableSet.of(target()));
	}

}

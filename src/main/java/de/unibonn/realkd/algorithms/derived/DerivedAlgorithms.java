/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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
package de.unibonn.realkd.algorithms.derived;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.algorithms.StoppableMiningAlgorithm;
import de.unibonn.realkd.common.parameter.Parameter;

/**
 * Provides factory methods for constructing mining algorithms derived from some
 * underlying base algorithm.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1
 *
 */
public final class DerivedAlgorithms {

	private interface RemainingParameterOption {
		public void perform(List<ParameterWrapper> parameterWrapperResultList,
				Parameter<?> candidateElement, MiningAlgorithm container);
	}

	public static final RemainingParameterOption EXPOSE = new RemainingParameterOption() {
		@Override
		public void perform(List<ParameterWrapper> resultList,
				Parameter<?> candidateElement, MiningAlgorithm container) {
			resultList.add(new TrivialParameterAdapter(candidateElement));
		}
	};

	public static final RemainingParameterOption HIDE_AND_WARN = new RemainingParameterOption() {
		@Override
		public void perform(List<ParameterWrapper> resultList,
				Parameter<?> candidate, MiningAlgorithm container) {
			System.out.println("WARNING: Hiding parameter "
					+ candidate.getName() + " of " + container.caption()
					+ " (relying on auto init).");
		}
	};

	/**
	 * <p>
	 * Factory method for constructing wrapper algorithm that provide a
	 * simplified/modified interface (in terms of its registered parameters) to
	 * some wrapped core algorithm.
	 * </p>
	 * <p>
	 * The parameter interface of the wrapper algorithm can be determined by
	 * providing {@link ParameterWrapper} objects. There are two types of
	 * parameter wrappers:
	 * </p>
	 * <ul>
	 * <li>{@link ParameterAdapter} objects, which are parameter containers
	 * themselves, and hide some parameter of the core algorithm behind their
	 * own parameters, and</li>
	 * <li>{@link ParameterTerminator} objects, which automatically set the
	 * value for some encapsulated parameter of the core algorithm.</li>
	 * </ul>
	 * <p>
	 * The behavior of the wrapper algorithm for parameter setting is as
	 * follows:
	 * </P>
	 * <ul>
	 * <li>
	 * All parameter terminators that transitively influence an exposed
	 * parameter (i.e., a parameter that is covered by an adapter) are activated
	 * on algorithm construction.</li>
	 * <li>
	 * All parameter terminators that are transitively influenced by some
	 * exposed parameter are activated whenever the algorithm is executed.</li>
	 * </ul>
	 * <p>
	 * These two groups have to be disjoint. Should there be a terminator in both
	 * groups, an illegal argument exception is thrown on construction.
	 * </p>
	 * 
	 * @param entailedAlgorithm
	 *            the core algorithm
	 * @param parameterWrappers
	 *            the wrapper objects that define the interface of the derived
	 *            algorithm
	 * @param remainingParameterOption
	 *            option specifying whether parameters of the core algorithm
	 *            that are not explicitly wrapped will show up in the derived
	 *            algorithm (EXPOSE) or will simply be hidden (HIDE_AND_WARN)
	 * @return algorithm that wraps entailed algorithm
	 * 
	 * @since 0.1.1
	 * 
	 * @version 0.1.1
	 * 
	 */
	public static final MiningAlgorithm getAlgorithmWithWrappedParameters(
			StoppableMiningAlgorithm entailedAlgorithm,
			List<ParameterWrapper> parameterWrappers,
			RemainingParameterOption remainingParameterOption) {
		List<ParameterWrapper> orderedWrapperList = getWrapperListBasedOnDependencyOrder(
				entailedAlgorithm, parameterWrappers);
		return getAlgorithmWithWrappedParameters(
				entailedAlgorithm,
				parameterWrappers,
				remainingParameterOption,
				computeDefaultStringRepresentation(entailedAlgorithm,
						orderedWrapperList));
	}

	/**
	 * Same as
	 * {@link #getAlgorithmWithWrappedParameters(StoppableMiningAlgorithm, List, RemainingParameterOption)}
	 * but allows to provide custom name for wrapper algorithm.
	 * 
	 */
	public static final MiningAlgorithm getAlgorithmWithWrappedParameters(
			StoppableMiningAlgorithm entailedAlgorithm,
			List<ParameterWrapper> parameterWrappers,
			RemainingParameterOption remainingParameterOption, String name) {
		List<ParameterWrapper> orderedWrapperList = compileAndFillUpOrderedWrapperList(
				entailedAlgorithm, parameterWrappers, remainingParameterOption);

		return new AlgorithmWithParameterFacade(entailedAlgorithm,
				orderedWrapperList, computeDefaultStringRepresentation(
						entailedAlgorithm, orderedWrapperList), name);
	}

	private static final List<ParameterWrapper> getWrapperListBasedOnDependencyOrder(
			StoppableMiningAlgorithm entailedAlgorithm,
			List<ParameterWrapper> parameterWrappers) {
		List<ParameterWrapper> result = new ArrayList<>();
		List<ParameterWrapper> remainingSelectors = new ArrayList<>(
				parameterWrappers);
		for (Parameter<?> parameter : entailedAlgorithm.getTopLevelParameters()) {
			parameterWrappers.forEach(new Consumer<ParameterWrapper>() {
				@Override
				public void accept(ParameterWrapper selector) {
					if (selector.getWrappedParameter() == parameter) {
						result.add(selector);
						remainingSelectors.remove(selector);
					}
				}
			});
		}
		if (!remainingSelectors.isEmpty()) {
			System.out
					.println("WARNING: The following parameter selectors are not linked to algorithm: "
							+ remainingSelectors);
		}
		return result;
	}

	private static final List<ParameterWrapper> compileAndFillUpOrderedWrapperList(
			StoppableMiningAlgorithm entailedAlgorithm,
			List<ParameterWrapper> parameterSelectors,
			RemainingParameterOption remainingParameterOption) {
		List<ParameterWrapper> result = new ArrayList<>();
		for (Parameter<?> parameter : entailedAlgorithm.getTopLevelParameters()) {
			boolean covered = false;
			for (ParameterWrapper selector : parameterSelectors) {
				if (selector.getWrappedParameter() == parameter) {
					covered = true;
					result.add(selector);
					break;
				}
			}
			if (!covered) {
				remainingParameterOption.perform(result, parameter,
						entailedAlgorithm);
			}
		}
		return result;
	}

	private static final String computeDefaultStringRepresentation(
			MiningAlgorithm entailedAlgorithm,
			List<ParameterWrapper> orderedParameterWrappers) {
		StringBuilder resultBuilder = new StringBuilder(
				entailedAlgorithm.caption());
		for (ParameterWrapper selector : orderedParameterWrappers) {
			resultBuilder.append("|" + selector.toString());
		}
		return resultBuilder.toString();
	}

}

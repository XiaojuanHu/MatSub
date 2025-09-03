/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
 */

package de.unibonn.realkd.algorithms.association;

import static com.google.common.base.Preconditions.checkArgument;
import static de.unibonn.realkd.common.base.Identifier.identifier;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.realkd.algorithms.common.PatternOptimizationFunction;
import de.unibonn.realkd.common.parameter.DefaultRangeEnumerableParameter;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;
import de.unibonn.realkd.patterns.Frequency;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.association.Association;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;

/**
 * Range enumerable parameter that provides a static range of optimization
 * functions that can be used for association discovery.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.1.0
 * 
 */
public final class AssociationTargetFunctionParameter
		extends DefaultRangeEnumerableParameter<PatternOptimizationFunction>
		implements RangeEnumerableParameter<PatternOptimizationFunction> {

	private static final List<PatternOptimizationFunction> OPTIONS = new ArrayList<>();
	static {
		OPTIONS.add(new PatternOptimizationFunction() {
			@Override
			public String toString() {
				return "lift";
			}

			@Override
			public Double apply(Pattern<?> pattern) {
				checkArgument((pattern instanceof Association), "Optimization function only defined for associations");

				return ((Association) pattern).getLift();
			}
		});
		OPTIONS.add(new PatternOptimizationFunction() {

			@Override
			public String toString() {
				return "negative lift";
			}

			@Override
			public Double apply(Pattern<?> pattern) {
				checkArgument((pattern instanceof Association), "Optimization function only defined for associations");

				return (-1) * ((Association) pattern).getLift();
			}
		});
		OPTIONS.add(new PatternOptimizationFunction() {

			@Override
			public String toString() {
				return "absolute lift";
			}

			@Override
			public Double apply(Pattern<?> pattern) {
				checkArgument((pattern instanceof Association), "Optimization function only defined for associations");

				return Math.abs(((Association) pattern).getLift());
			}
		});
		OPTIONS.add(new PatternOptimizationFunction() {

			@Override
			public String toString() {
				return "area";
			}

			@Override
			public Double apply(Pattern<?> pattern) {
				checkArgument((pattern instanceof Association), "Optimization function only defined for Associations.");
				checkArgument((pattern.descriptor() instanceof LogicalDescriptor),
						"Optimization function currently assumes that association has logical descriptor.");

				LogicalDescriptor description = (LogicalDescriptor) ((Association) pattern).descriptor();
				// .getDescription();
				return Double.valueOf(description.supportSet().size() * description.size());
			}
		});
		OPTIONS.add(new PatternOptimizationFunction() {

			@Override
			public String toString() {
				return "frequency";
			}

			@Override
			public Double apply(Pattern<?> pattern) {
				checkArgument((pattern instanceof Association) && pattern.hasMeasure(Frequency.FREQUENCY),
						"Optimization function only defined for associations with frequency measure bound");

				return pattern.value(Frequency.FREQUENCY);
			}

		});

		// OPTIONS.add(FunctionUtil.wrapFunctionWithToString(
		// pattern -> pattern.getValue(InterestingnessMeasure.FREQUENCY),
		// "frequency"));
	}

	private static final Class<PatternOptimizationFunction> TYPE = PatternOptimizationFunction.class;
	private static final String DESCRIPTION = "The function which will be optimized by the algorithm";
	public static final String NAME = "Target function";

	public AssociationTargetFunctionParameter() {
		super(identifier("obj_func"), NAME, DESCRIPTION, TYPE, new RangeComputer<PatternOptimizationFunction>() {

			@Override
			public List<PatternOptimizationFunction> get() {
				return OPTIONS;
			}
		});
	}

}

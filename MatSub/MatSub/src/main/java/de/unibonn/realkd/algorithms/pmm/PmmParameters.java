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
package de.unibonn.realkd.algorithms.pmm;

import static com.google.common.base.Preconditions.checkArgument;
import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.parameter.Parameters.rangeEnumerableParameter;
import static de.unibonn.realkd.patterns.subgroups.AbsolutePearsonCorrelationGain.ABSOLUTE_PEARSON_GAIN;
import static de.unibonn.realkd.patterns.subgroups.EntropyReduction.ENTROPY_REDUCTION;
import static de.unibonn.realkd.patterns.subgroups.MutualInformationGain.MUTUAL_INFORMATION_GAIN;
import static de.unibonn.realkd.patterns.subgroups.RootMeanSquaredErrorReduction.RMSE_REDUCTION;
import static de.unibonn.realkd.patterns.subgroups.StandardDeviationReduction.STD_REDUCTION;
import static de.unibonn.realkd.util.Lists.listOrEmpty;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.common.PatternOptimizationFunction;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.Frequency;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.models.ModelFactory;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistributionFactory;
import de.unibonn.realkd.patterns.models.regression.LinearRegressionModel;
import de.unibonn.realkd.patterns.models.table.ContingencyTableModelFactory;
import de.unibonn.realkd.patterns.pmm.PureModelSubgroup;
import de.unibonn.realkd.patterns.subgroups.Subgroup;

/**
 * Provides static factory methods for the construction of parameters of pure
 * model mining algorithms.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class PmmParameters {

	private PmmParameters() {
		;
	}

	/**
	 * @return the name of the parameters created by
	 *         {@link #purityMeasureParameter(RangeEnumerableParameter, Parameter)}
	 */
	public static String purityMeasureParameterName() {
		return "Purity measure";
	}

	/**
	 * 
	 * @param modelFactory
	 *            the model factory parameter that the resulting distance function
	 *            parameter depends on
	 * @return a parameter for choosing a model distance function that matches an
	 *         already selected model class
	 */
	public static RangeEnumerableParameter<MeasurementProcedure<? extends Measure, ? super Subgroup<?>>> purityMeasureParameter(
			final RangeEnumerableParameter<Supplier<ModelFactory<?>>> modelFactory,
			final Parameter<List<Attribute<?>>> targetsParameter) {

		String description = "The function for measuring the deviation of the local population from the global population.";

		List<Function<RangeEnumerableParameter<Supplier<ModelFactory<?>>>, List<MeasurementProcedure<? extends Measure, ? super Subgroup<?>>>>> providers = ImmutableList
				.of(factory -> listOrEmpty(factory.current().get() instanceof ContingencyTableModelFactory,
						ENTROPY_REDUCTION),
						factory -> listOrEmpty(factory.current().get() instanceof ContingencyTableModelFactory
								&& targetsParameter.current().size() == 2, MUTUAL_INFORMATION_GAIN),
						factory -> listOrEmpty(factory.current().get() instanceof MetricEmpiricalDistributionFactory
								&& targetsParameter.current().size() == 2, ABSOLUTE_PEARSON_GAIN),
						factory -> listOrEmpty(
								LinearRegressionModel.class.isAssignableFrom(factory.current().get().modelClass()),
								RMSE_REDUCTION),
						factory -> listOrEmpty(factory.current().get() instanceof MetricEmpiricalDistributionFactory,
								STD_REDUCTION));

		return Parameters.dependentRangeEnumerableParameter(id("purity_measure"), purityMeasureParameterName(),
				description, MeasurementProcedure.class, modelFactory, providers);
	}

	public static RangeEnumerableParameter<PatternOptimizationFunction> targetFunctionParameter() {

		List<PatternOptimizationFunction> options = ImmutableList.of(new PatternOptimizationFunction() {

			@Override
			public Double apply(Pattern<?> pattern) {
				checkArgument(pattern instanceof PureModelSubgroup);
				PureModelSubgroup pmSubgroup = (PureModelSubgroup) pattern;
				return (pmSubgroup.value(pmSubgroup.purityGainMeasure())) * (pmSubgroup.value(Frequency.FREQUENCY));
			}

			@Override
			public Target optimizationTarget() {
				return Target.MAXIMIZATION;
			}

			@Override
			public String toString() {
				return "frequency*gain";
			}

		}, new PatternOptimizationFunction() {

			@Override
			public Double apply(Pattern<?> pattern) {
				checkArgument(pattern instanceof PureModelSubgroup);
				PureModelSubgroup pmSubgroup = (PureModelSubgroup) pattern;
				return (pmSubgroup.value(pmSubgroup.purityGainMeasure()))
						* (Math.sqrt(pmSubgroup.value(Frequency.FREQUENCY)));
			}

			@Override
			public Target optimizationTarget() {
				return Target.MAXIMIZATION;
			}

			@Override
			public String toString() {
				return "sqrt(frequency)*gain";
			}
		});

		return rangeEnumerableParameter(id("obj_func"), "Objective function",
				"The function to be maximized by the algorithm", PatternOptimizationFunction.class, () -> options);
	}

}

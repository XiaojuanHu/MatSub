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
package de.unibonn.realkd.algorithms.emm;

import static de.unibonn.realkd.patterns.emm.NormalizedMaxWithConstatntRef.NORMALIZED_MAX_CONSTANT_REF;
import static de.unibonn.realkd.patterns.emm.NormalizedMax.NORMALIZED_MAX;
import static de.unibonn.realkd.patterns.emm.NormalizedMin.NORMALIZED_MIN;
import static com.google.common.base.Preconditions.checkArgument;
import static de.unibonn.realkd.algorithms.common.MiningParameters.getAttributeSelectionParameter;
import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.parameter.Parameters.dependentRangeEnumerableParameter;
import static de.unibonn.realkd.common.parameter.Parameters.rangeEnumerableParameter;
import static de.unibonn.realkd.patterns.emm.KolmogorovSmirnovStatistic.KOLMOGOROV_SMIRNOV_STATISTIC;
import static de.unibonn.realkd.patterns.emm.ManhattenMeanDistance.MANHATTAN_MEAN_DISTANCE;
import static de.unibonn.realkd.patterns.emm.NormalizedAbsoluteMeanShift.NORMALIZED_ABSOLUTE_MEAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedAbsoluteMedianShift.NORMALIZED_ABSOLUTE_MEDIAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedNegativeMeanShift.NORMALIZED_NEGATIVE_MEAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedNegativeMedianShift.NORMALIZED_NEGATIVE_MEDIAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedPositiveMeanShift.NORMALIZED_POSITIVE_MEAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedPositiveMedianShift.NORMALIZED_POSITIVE_MEDIAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.PositiveProbabilityShift.POSITIVE_PROBABILITY_SHIFT;
import static de.unibonn.realkd.patterns.emm.ReliableConditionalEffect.RELIABLE_CONDITIONAL_EFFECT;
import static de.unibonn.realkd.patterns.subgroups.MedianDeviationReduction.AVERAGE_ABSOLUTE_MEDIAN_DEVIATION_REDUCTION;
import static de.unibonn.realkd.util.Lists.listOrEmpty;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.algorithms.common.MiningParameters;
import de.unibonn.realkd.algorithms.common.PatternOptimizationFunction;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.DefaultRangeEnumerableParameter.RangeComputer;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;
import de.unibonn.realkd.common.parameter.SubCollectionParameter;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;
import de.unibonn.realkd.patterns.Frequency;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.emm.AngularDistanceOfSlopes;
import de.unibonn.realkd.patterns.emm.CumulativeJensenShannonDivergence;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;
import de.unibonn.realkd.patterns.emm.HellingerDistance;
import de.unibonn.realkd.patterns.emm.KullbackLeiblerDivergence;
import de.unibonn.realkd.patterns.emm.ModelDeviationMeasure;
import de.unibonn.realkd.patterns.emm.TotalVariationDistance;
import de.unibonn.realkd.patterns.models.bernoulli.BernoulliDistributionFactory;
import de.unibonn.realkd.patterns.models.conditional.DiscretelyConditionedBernoulliFactory;
import de.unibonn.realkd.patterns.models.gaussian.GaussianModelFactory;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistributionFactory;
import de.unibonn.realkd.patterns.models.regression.LeastSquareRegressionModelFactory;
import de.unibonn.realkd.patterns.models.regression.TheilSenLinearRegressionModelFactory;
import de.unibonn.realkd.patterns.models.table.ContingencyTableModelFactory;
import de.unibonn.realkd.patterns.models.weibull.FixedShapeWeibullModelFactory;
import de.unibonn.realkd.patterns.subgroups.RepresentativenessMeasure;
import de.unibonn.realkd.common.workspace.Workspaces;
import de.unibonn.realkd.data.table.attribute.DefaultMetricAttribute;
import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;

/**
 * Utility class that provides factory methods for exceptional model mining
 * parameters.
 * 
 * @author Mario Boley
 *
 * @since 0.1.0
 * 
 * @version 0.7.0
 * 
 * 
 */
public class EMMParameters {

	/**
	 * Convenience method for providing an attribute selection parameter for
	 * choosing a non-empty collection of target attributes in exceptional model
	 * mining with default name and description.
	 * 
	 * @see MiningParameters#getAttributeSelectionParameter(String, String,
	 *      Parameter, Predicate)
	 * 
	 */
	public static SubCollectionParameter<Attribute<? extends Object>, List<Attribute<? extends Object>>> getEMMTargetAttributesParameter(
			Parameter<DataTable> dataTableParameter, Predicate<Attribute<?>> filterPredicate) {
		Predicate<List<Attribute<? extends Object>>> validator = attributeList -> !attributeList.isEmpty()
				&& dataTableParameter.current().attributes().containsAll(attributeList);
		return getAttributeSelectionParameter(id("targets"), "Target attributes",
				"List of attributes for which patterns should show special characteristics", dataTableParameter,
				filterPredicate, validator);
	}

	/**
	 * Convenience method for creating attribute selection parameter for EMM targets
	 * with no attribute filter predicate.
	 * 
	 * @see {@link #getEMMTargetAttributesParameter(Parameter, Predicate)
	 * 
	 */
	public static Parameter<List<Attribute<? extends Object>>> getEMMTargetAttributesParameter(
			Parameter<DataTable> dataTableParameter) {
		return getEMMTargetAttributesParameter(dataTableParameter, attribute -> true);
	}

	public static RangeEnumerableParameter<Optional<? extends Attribute<?>>> controlAttributeParameter(
			Parameter<DataTable> dataTableParameter,
			Parameter<List<Attribute<? extends Object>>> targetAttributeParameter) {
		RangeComputer<Optional<? extends Attribute<?>>> rangeSupplier = () -> {
			List<Optional<? extends Attribute<?>>> result = new ArrayList<>();
			result.add(Optional.empty());
			dataTableParameter.current().attributes().stream()
					.filter(a -> !targetAttributeParameter.current().contains(a)
							&& (a instanceof OrdinalAttribute || a instanceof CategoricAttribute))
					.map(a -> Optional.of(a)).forEach(result::add);
			return result;
		};
		return rangeEnumerableParameter(id("controls"), "Control attribute",
				"Attribute for which subgroup should show an identical distribution as in global population.",
				Optional.class, rangeSupplier, dataTableParameter, targetAttributeParameter);
	}

	/**
	 * <p>
	 * Creates a parameter for filtering out possible extension descriptor
	 * attributes from some data table. The valid range is given by all attributes
	 * that do not relate to any selected target attribute (i.e., all attributes
	 * that are neither an target attribute or a part of a joint macro-attribute
	 * with one), because propositions relating to those attributes are supposed to
	 * be filtered out anyway by EMM algorithms.
	 * </p>
	 * 
	 * @param dataTableParameter
	 *            parameter that holds selection of data table and, thus, the
	 *            underlying range of all available attributes
	 * 
	 * @param targetAttributesParameter
	 *            parameter that holds selection of target attributes, which induce
	 *            filter
	 * 
	 */
	public static SubCollectionParameter<Attribute<?>, Set<Attribute<?>>> getEMMDescriptorAttributesParameter(
			Parameter<DataTable> dataTableParameter, Parameter<List<Attribute<?>>> targetAttributesParameter) {

		Supplier<Set<Attribute<?>>> collectionComputer = new Supplier<Set<Attribute<?>>>() {

			@Override
			public Set<Attribute<?>> get() {
				DataTable dataTable = dataTableParameter.current();
				List<Attribute<?>> targetAttributes = targetAttributesParameter.current();
				Predicate<? super Attribute<?>> filterPredicate = attribute -> !targetAttributes.contains(attribute)
						&& !dataTable.containsDependencyBetweenAnyOf(attribute, targetAttributes);

				return dataTable.attributes().stream().filter(filterPredicate)
						.collect(Collectors.toCollection(LinkedHashSet::new));
			}

		};

		Supplier<Set<Attribute<?>>> initializer = () -> ImmutableSet.of();

		return Parameters.subSetParameter(id("attr_filter"), "Attribute filter",
				"Attributes not to be used in descriptors.", collectionComputer, initializer, dataTableParameter,
				targetAttributesParameter);
	}

	/**
	 * As {@link #getEMMDescriptorAttributesParameter(Parameter, Parameter)} but
	 * additionally filtering for one control attribute parameter.
	 * 
	 */
	public static SubCollectionParameter<Attribute<?>, Set<Attribute<?>>> getEMMDescriptorAttributesParameter(
			Parameter<DataTable> dataTableParameter, Parameter<List<Attribute<?>>> targetAttributesParameter,
			Parameter<Optional<? extends Attribute<?>>> controlAttributeParameter) {

		Supplier<Set<Attribute<?>>> rangeSupplier = new Supplier<Set<Attribute<?>>>() {

			@Override
			public Set<Attribute<?>> get() {
				DataTable dataTable = dataTableParameter.current();
				List<Attribute<?>> targetAttributes = targetAttributesParameter.current();
				Predicate<? super Attribute<?>> filterPredicate = attribute -> !targetAttributes.contains(attribute)
						&& !dataTable.containsDependencyBetweenAnyOf(attribute, targetAttributes);

				// TODO just this also filter dependent attributes as with
				// targets?
				if (controlAttributeParameter.current().isPresent()) {
					filterPredicate = filterPredicate.and(a -> a != controlAttributeParameter.current().get());
				}

				return dataTable.attributes().stream().filter(filterPredicate)
						.collect(Collectors.toCollection(LinkedHashSet::new));
			}

		};

		Supplier<Set<Attribute<?>>> initializer = () -> ImmutableSet.of();

		return Parameters.subSetParameter(id("attr_filter"), "Attribute filter",
				"Attributes not to be used in descriptors.", rangeSupplier, initializer, dataTableParameter,
				targetAttributesParameter, controlAttributeParameter);
	}

	/**
	 * @return the name of the parameters created by
	 *         {@link #distanceFunctionParameter(ModelClassParameter)}
	 */
	public static String distanceFunctionParameterName() {
		return "Deviation Measure";
	}

	/**
	 * 
	 * @param modelFactory
	 *            the model factory parameter that the resulting distance function
	 *            parameter depends on
	 * @return a parameter for choosing a model distance function that matches an
	 *         already selected model class
	 */
	public static RangeEnumerableParameter<ModelDeviationMeasure> distanceFunctionParameter(
			final ModelClassParameter modelFactory) {

		@SuppressWarnings("rawtypes")
		Class<MeasurementProcedure> type = MeasurementProcedure.class;
		String description = "The function for measuring the deviation of the local population from the global population.";

		List<? extends Function<? super ModelClassParameter, ? extends List<? extends ModelDeviationMeasure>>> providers = ImmutableList
				.of(factory -> listOrEmpty(factory.current().get() instanceof BernoulliDistributionFactory,
						POSITIVE_PROBABILITY_SHIFT),
						factory -> listOrEmpty(factory.current().get() instanceof DiscretelyConditionedBernoulliFactory, RELIABLE_CONDITIONAL_EFFECT),
						factory -> listOrEmpty(
								factory.current().get() instanceof MetricEmpiricalDistributionFactory
										&& factory.attributes().current().size() == 1,
								NORMALIZED_ABSOLUTE_MEAN_SHIFT, NORMALIZED_POSITIVE_MEAN_SHIFT,
								NORMALIZED_NEGATIVE_MEAN_SHIFT, NORMALIZED_ABSOLUTE_MEDIAN_SHIFT,
								NORMALIZED_POSITIVE_MEDIAN_SHIFT, NORMALIZED_NEGATIVE_MEDIAN_SHIFT,
								NORMALIZED_MAX_CONSTANT_REF, NORMALIZED_MAX, NORMALIZED_MIN,
								KOLMOGOROV_SMIRNOV_STATISTIC, CumulativeJensenShannonDivergence.CJS),
						factory -> listOrEmpty(factory.current().get() instanceof MetricEmpiricalDistributionFactory,
								       MANHATTAN_MEAN_DISTANCE, NORMALIZED_MAX_CONSTANT_REF, NORMALIZED_MAX,
								       NORMALIZED_MIN),
						factory -> listOrEmpty(factory.current().get() instanceof ContingencyTableModelFactory,
								TotalVariationDistance.TOTAL_VARIATION_DISTANCE, HellingerDistance.HELLINGER_DISTANCE),
						factory -> listOrEmpty(factory.current().get() instanceof GaussianModelFactory,
								TotalVariationDistance.TOTAL_VARIATION_DISTANCE, HellingerDistance.HELLINGER_DISTANCE,
								KullbackLeiblerDivergence.KL_DIVERGENCE),
						factory -> listOrEmpty(factory.current().get() instanceof FixedShapeWeibullModelFactory,
								HellingerDistance.HELLINGER_DISTANCE),
						factory -> listOrEmpty(
								factory.current().get() instanceof TheilSenLinearRegressionModelFactory
										|| factory.current().get() instanceof LeastSquareRegressionModelFactory,
								AngularDistanceOfSlopes.ANGULAR_DISTANCE_OF_SLOPES));

		String name = distanceFunctionParameterName();
		return Parameters.dependentRangeEnumerableParameter(id("dev_measure"), name, description, type, modelFactory,
				providers);
	}

	public static Parameter<PatternOptimizationFunction> emmTargetFunctionParameter(ModelClassParameter modelFactory) {
		List<Function<ModelClassParameter, List<PatternOptimizationFunction>>> providers = ImmutableList.of(
				m -> EMM_TARGET_FUNCTION_BASE_OPTIONS,
				m -> listOrEmpty(m.current() == m.empirical_distribution_option && m.attributes().current().size() == 1,
						DISPERSION_CORRECTED_TARGET_FUNCTION));

		return dependentRangeEnumerableParameter(EMM_OBJECTIVE_FUNCTION_PARAMETER_ID,
				EMM_TARGET_FUNCTION_PARAMETER_NAME, "The function which will be optimized by the algorithm",
				PatternOptimizationFunction.class, modelFactory, providers);
	}

	public static Parameter<PatternOptimizationFunction> emmTargetFunctionParameter(ModelClassParameter modelFactory,
			Parameter<Optional<? extends Attribute<?>>> controls) {
		List<BiFunction<ModelClassParameter, Parameter<Optional<? extends Attribute<?>>>, List<PatternOptimizationFunction>>> providers = ImmutableList
				.of((m, c) -> EMM_TARGET_FUNCTION_BASE_OPTIONS,
						(m, c) -> listOrEmpty(c.current().isPresent(), REPRESENTATIVENESS_CORRECTED_TARGET_FUNCTION),
						(m, c) -> listOrEmpty(
								m.current() == m.empirical_distribution_option && m.attributes().current().size() == 1,
								DISPERSION_CORRECTED_TARGET_FUNCTION));

		return Parameters.dependentRangeEnumerableParameter(EMM_OBJECTIVE_FUNCTION_PARAMETER_ID,
				EMM_TARGET_FUNCTION_PARAMETER_NAME, "The function which will be optimized by the algorithm",
				PatternOptimizationFunction.class, modelFactory, controls, providers);
	}

	public static final Identifier EMM_OBJECTIVE_FUNCTION_PARAMETER_ID = id("obj_func");

	public static final String EMM_TARGET_FUNCTION_PARAMETER_NAME = "Objective function";

	private static final PatternOptimizationFunction DISPERSION_CORRECTED_TARGET_FUNCTION = new PatternOptimizationFunction() {

		@Override
		public Double apply(Pattern<?> pattern) {
		    double aamdGain = pattern.value(AVERAGE_ABSOLUTE_MEDIAN_DEVIATION_REDUCTION);
		    		    return ((ExceptionalModelPattern) pattern).value(Frequency.FREQUENCY) * aamdGain
					* Math.max(pattern.value(((ExceptionalModelPattern) pattern).getDeviationMeasure()), 0);
		}

		@Override
		public String toString() {
			return "frequency times aamd-gain times deviation";
		}

	};

	private static final PatternOptimizationFunction REPRESENTATIVENESS_CORRECTED_TARGET_FUNCTION = new PatternOptimizationFunction() {

		@Override
		public Double apply(Pattern<?> pattern) {
			double representativeness = pattern.measurement(RepresentativenessMeasure.class).map(m -> m.value())
					.orElse(0.0);
			return ((ExceptionalModelPattern) pattern).value(Frequency.FREQUENCY) * representativeness
					* Math.max(pattern.value(((ExceptionalModelPattern) pattern).getDeviationMeasure()), 0);
		}

		@Override
		public String toString() {
			return "cov(Q)*dev(Q)*repr(Q)";
		}

	};

	static final List<PatternOptimizationFunction> EMM_TARGET_FUNCTION_BASE_OPTIONS = ImmutableList
			.of(new PatternOptimizationFunction() {

				@Override
				public Double apply(Pattern<?> pattern) {
					checkArgument(
							(pattern instanceof ExceptionalModelPattern) && pattern.hasMeasure(Frequency.FREQUENCY),
							"Target function only defined for exceptional model patterns with frequency.");

					//System.out.println("size "+((ExceptionalModelPattern) pattern).value(Frequency.FREQUENCY)+", devmeasure "
					//		   +pattern.value(((ExceptionalModelPattern) pattern).getDeviationMeasure())); 

					return Math.pow(((ExceptionalModelPattern) pattern).value(Frequency.FREQUENCY),1.0)
					    * Math.max(Math.pow(pattern.value(((ExceptionalModelPattern) pattern).getDeviationMeasure()),1.0), 0);
				}

				@Override
				public String toString() {
					return "frequency times deviation";
				}

			}, new PatternOptimizationFunction() {

				@Override
				public Double apply(Pattern<?> pattern) {
					checkArgument(
							(pattern instanceof ExceptionalModelPattern) && pattern.hasMeasure(Frequency.FREQUENCY),
							"Target function only defined for exceptional model patterns with frequency.");

					return Math.sqrt(pattern.value(Frequency.FREQUENCY))
							* Math.max(pattern.value(((ExceptionalModelPattern) pattern).getDeviationMeasure()), 0);
				}

				@Override
				public String toString() {
					return "sqrt(frequency) times deviation";
				}

			}, new PatternOptimizationFunction() {

				private double entropy(double p) {
					return (-1 * p * Math.log(p) - (1 - p) * Math.log(1 - p)) / Math.log(2);
				}

				@Override
				public Double apply(Pattern<?> pattern) {
					checkArgument(
							(pattern instanceof ExceptionalModelPattern) && pattern.hasMeasure(Frequency.FREQUENCY),
							"Target function only defined for exceptional model patterns with frequency.");

					double freq = pattern.value(Frequency.FREQUENCY);
					return entropy(freq)
							* Math.max(pattern.value(((ExceptionalModelPattern) pattern).getDeviationMeasure()), 0);
				}

				@Override
				public String toString() {
					return "H(frequency) times deviation";
				}

			    }, new PatternOptimizationFunction() {

				    private double entropy(double p) {
                                        return (-1 * p * Math.log(p) - (1 - p) * Math.log(1 - p)) / Math.log(2);
				    }

                                @Override
                                public Double apply(Pattern<?> pattern) {
                                        checkArgument(
                                                        (pattern instanceof ExceptionalModelPattern) && pattern.hasMeasure(Frequency.FREQUENCY),
                                                        "Target function only defined for exceptional model patterns with frequency.");
					double dev = pattern.value(((ExceptionalModelPattern) pattern).getDeviationMeasure());
					if (dev > 0.000001) {  
					    //    List<String> cutofftypes = ExceptionalSubgroupSampler.hardCutoffParameters();
					List<Double> params = ExceptionalSubgroupSampler.qualityFunctionParameters();
					double border = params.get(0);
					double Eadslow = params.get(1);
					double Eadshigh = params.get(2);
					//System.out.println("SVL params"+border+" "+Eadslow+" "+Eadshigh);
					IndexSet patset = ((ExceptionalModelPattern) pattern).descriptor().supportSet();
					double Nsubtotal = new Double(patset.size());
					double Nglobtotal = new Double(((ExceptionalModelPattern) pattern).descriptor().population().size());
					Attribute<?> firstAttribute = ((ExceptionalModelPattern) pattern).descriptor().targetAttributes().get(0);
					Attribute<?> secondAttribute = ((ExceptionalModelPattern) pattern).descriptor().targetAttributes().get(1);
					//double val1 = new Double(((Attribute<Double>) firstAttribute).value(0)).doubleValue();
					//double val2 = new Double(((Attribute<Double>) firstAttribute).value(1)).doubleValue();

					//global above threshold for first attribute (dCO)
					IndexSet globinside1 = ((DefaultMetricAttribute) firstAttribute).abovethresh(border);
					//global inside interval for the second attribute (Eads)
					IndexSet globinside2 = ((DefaultMetricAttribute) secondAttribute).withininterval(Eadslow, Eadshigh);
					IndexSet globinside = globinside2;//IndexSets.intersection(globinside1, globinside2);
					double Nglobinside = new Double(globinside.size());
					//subgroup above threshold for first attribute (dCO) 
					IndexSet subinside1 = ((DefaultMetricAttribute) firstAttribute).abovethreshOnRows(patset, border);
					//subgroup inside interval for the second attribute (Eads)
					IndexSet subinside2 = ((DefaultMetricAttribute) secondAttribute).withinintervalOnRows(patset, Eadslow, Eadshigh);
					IndexSet subinside = subinside2;//IndexSets.intersection(subinside1, subinside2);
					double Nsubinside = new Double(subinside.size());
					if (Nsubinside <= Nsubtotal-Nsubinside) return 0.0;
					double globentropy = entropy(Nglobinside/Nglobtotal);
					double subentropy = entropy(Nsubinside/Nsubtotal);
					double complsubentropy = entropy((Nglobinside-Nsubinside)/(Nglobtotal-Nsubtotal));
					double splitentropy = Nsubtotal/Nglobtotal*subentropy + (1-Nsubtotal/Nglobtotal)*complsubentropy;

					//This is for the split entropy
					//return Math.max((globentropy - splitentropy)*dev,0.0);

					//This is for the subgroup entropy
					return ((ExceptionalModelPattern) pattern).value(Frequency.FREQUENCY)*dev*(1-subentropy);
					}
					return 0.0;
                                }

                                @Override
                                public String toString() {
                                        return "multitask entropy gain";
                                }

                        });

}

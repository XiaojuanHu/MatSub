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
package de.unibonn.realkd.algorithms.emm;

import static de.unibonn.realkd.common.parameter.Parameters.doubleParameter;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.ParameterContainer;
import de.unibonn.realkd.common.parameter.ParameterListener;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.models.ModelFactory;
import de.unibonn.realkd.patterns.models.bernoulli.BernoulliDistributionFactory;
import de.unibonn.realkd.patterns.models.conditional.DiscretelyConditionedBernoulliFactory;
import de.unibonn.realkd.patterns.models.gaussian.GaussianModelFactory;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistributionFactory;
import de.unibonn.realkd.patterns.models.regression.LeastSquareRegressionModelFactory;
import de.unibonn.realkd.patterns.models.regression.TheilSenLinearRegressionModelFactory;
import de.unibonn.realkd.patterns.models.table.ContingencyTableModelFactory;
import de.unibonn.realkd.patterns.models.weibull.FixedShapeWeibullModelFactory;
import de.unibonn.realkd.util.Predicates;

/**
 * Parameter that provides a range of applicable model factories dependent on
 * another parameter that holds a selection of attributes to be modeled.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.5.0
 * 
 */
public final class ModelClassParameter implements RangeEnumerableParameter<Supplier<ModelFactory<?>>> {

	/**
	 * 
	 */
	public final ModelClassOption THEIL_SEN_REGRESSION_MODEL_OPTION = singleton(
			TheilSenLinearRegressionModelFactory.INSTANCE);

	public final ModelClassOption LASSO_MODEL_OPTION = singleton(LeastSquareRegressionModelFactory.INSTANCE);

	/**
	 * 
	 */
	public final ModelClassOption WEIBULL_OPTION = new FixedShapeWeibullWrapper();

	private final BernoulliOption bernoulliOption;

	private final DiscretelyConditionedBernoulliOption discretelyConditionedBernoulliOption;

	/**
	 * 
	 */
	public final ModelClassOption GAUSSIAN_OPTION = singleton(GaussianModelFactory.INSTANCE);
	/**
	 * 
	 */
	public final ModelClassOption empirical_distribution_option = singleton(
			MetricEmpiricalDistributionFactory.INSTANCE);
	/**
	 * 
	 */
	public final ModelClassOption contingency_table_option = singleton(ContingencyTableModelFactory.INSTANCE);

	private static ModelClassOption singleton(ModelFactory<?> instance) {
		return new SingletonModelFactoryWrapper(instance);
	}

	public interface ModelClassOption extends Supplier<ModelFactory<?>>, Identifiable {
		public boolean isApplicable(List<Attribute<?>> attributes);
	}

	private static class FixedShapeWeibullWrapper implements ModelClassOption, ParameterContainer {

		private final Parameter<Double> shapeParameter;

		private final List<Parameter<?>> parameters;

		public FixedShapeWeibullWrapper() {
			shapeParameter = doubleParameter(Identifier.id("k"), "k",
					"Positive shape parameter of the Weibull distribution to be fitted (k=1 for exponential distribution)",
					1.0, Predicates.largerThan(0.0), "Enter positive double value.");
			parameters = ImmutableList.of(shapeParameter);
		}

		@Override
		public ModelFactory<?> get() {
			return new FixedShapeWeibullModelFactory(shapeParameter.get().orElse(1.0));
		}

		@Override
		public List<Parameter<?>> getTopLevelParameters() {
			return parameters;
		}

		@Override
		public boolean isApplicable(List<Attribute<?>> attributes) {
			return !(attributes.size() != 1 || !(attributes.get(0) instanceof MetricAttribute));
		}

		@Override
		public String toString() {
			return "Weibull distribution";
		}

		@Override
		public Identifier identifier() {
			return Identifier.id("weibull");
		}

	}

	public static class BernoulliOption implements ModelClassOption, ParameterContainer {

		private final RangeEnumerableParameter<? extends Object> positiveCategory;
		private final List<Parameter<?>> parameters;

		private BernoulliOption(Parameter<List<Attribute<?>>> attributesParameter) {
			// TODO bad smell that the case distinction in next line is
			// necessary
			this.positiveCategory = Parameters.rangeEnumerableParameter(Identifier.identifier("pos_category"), "Target category",
					"The category corresponding to the event for which probability is modelled.", Object.class,
					() -> (attributesParameter.current()
							.get(attributesParameter.current().size() - 1) instanceof CategoricAttribute)
									? ((CategoricAttribute<?>) attributesParameter.current()
											.get(attributesParameter.current().size() - 1)).categories()
									: ImmutableList.of(),
					attributesParameter);
			this.parameters = ImmutableList.of(positiveCategory());
		}

		@Override
		public ModelFactory<?> get() {
			return new BernoulliDistributionFactory(positiveCategory().current());
		}

		@Override
		public boolean isApplicable(List<Attribute<?>> attributes) {
			return (attributes.size() == 1 && (attributes.get(0) instanceof CategoricAttribute));
		}

		@Override
		public String toString() {
			return "Bernoulli distribution";
		}

		@Override
		public List<Parameter<?>> getTopLevelParameters() {
			return parameters;
		}

		public RangeEnumerableParameter<? extends Object> positiveCategory() {
			return positiveCategory;
		}

		@Override
		public Identifier identifier() {
			return Identifier.identifier("bernoulli");
		}

	}

	private static class DiscretelyConditionedBernoulliOption implements ModelClassOption, ParameterContainer {

		private final RangeEnumerableParameter<? extends Object> positiveCategory;
		private final List<Parameter<?>> parameters;

		private DiscretelyConditionedBernoulliOption(Parameter<List<Attribute<?>>> attributesParameter) {
			this.positiveCategory = Parameters.rangeEnumerableParameter(Identifier.identifier("pos_category"), "Target category",
					"The category corresponding to the event for which probability is modelled.", Object.class,
					// TODO bad smell that the case distinction in next line is
					// necessary
					() -> (attributesParameter.current().size() >= 1 && attributesParameter.current()
							.get(attributesParameter.current().size() - 1) instanceof CategoricAttribute)
									? ((CategoricAttribute<?>) attributesParameter.current()
											.get(attributesParameter.current().size() - 1)).categories()
									: ImmutableList.of(),
					attributesParameter);
			this.parameters = ImmutableList.of(positiveCategory());
		}

		@Override
		public ModelFactory<?> get() {
			return new DiscretelyConditionedBernoulliFactory(positiveCategory().current());
		}

		@Override
		public boolean isApplicable(List<Attribute<?>> attributes) {
			return (attributes.size() >= 1);
			// && (attributes.get(0) instanceof CategoricAttribute)
			// && (attributes.get(attributes.size() - 1) instanceof CategoricAttribute));
		}

		@Override
		public String toString() {
			return "Discretely Conditioned Bernoulli";
		}

		@Override
		public List<Parameter<?>> getTopLevelParameters() {
			return parameters;
		}

		public RangeEnumerableParameter<? extends Object> positiveCategory() {
			return positiveCategory;
		}

		@Override
		public Identifier identifier() {
			return Identifier.identifier("discretely_conditioned_bernoulli");
		}

	}

	private static class SingletonModelFactoryWrapper implements ModelClassOption {

		private final ModelFactory<?> instance;

		public SingletonModelFactoryWrapper(ModelFactory<?> instance) {
			this.instance = instance;
		}

		@Override
		public ModelFactory<?> get() {
			return instance;
		}

		@Override
		public boolean isApplicable(List<Attribute<?>> attributes) {
			return instance.isApplicable(attributes);
		}

		@Override
		public String toString() {
			return instance.toString();
		}

		@Override
		public Identifier identifier() {
			return Identifier.identifier(instance.toString());
		}

	}

	private static final String DESCRIPTION = "The type of data summary, according to which population deviation is measured.";
	public static final String NAME = "Model class";
	public static final Identifier ID = Identifier.id("modeling");

	private final Parameter<List<Attribute<?>>> targetAttributesParameter;

	private final List<ModelClassOption> allOptions;

	private final RangeEnumerableParameter<Supplier<ModelFactory<?>>> wrapped;

	public ModelClassParameter(final Parameter<List<Attribute<?>>> targetAttributesParameter) {
		this.targetAttributesParameter = targetAttributesParameter;
		this.bernoulliOption = new BernoulliOption(targetAttributesParameter);
		this.discretelyConditionedBernoulliOption = new DiscretelyConditionedBernoulliOption(targetAttributesParameter);
		this.allOptions = ImmutableList.of(empirical_distribution_option, contingency_table_option, bernoulliOption(),
				discretelyConditionedBernoulliOption, GAUSSIAN_OPTION, WEIBULL_OPTION, LASSO_MODEL_OPTION,
				THEIL_SEN_REGRESSION_MODEL_OPTION);
		wrapped = Parameters.rangeEnumerableParameter(
				ID, NAME, DESCRIPTION, ModelFactory.class, () -> allOptions.stream()
						.filter(f -> f.isApplicable(targetAttributesParameter.current())).collect(toList()),
				targetAttributesParameter);
	}

	public List<? extends Supplier<ModelFactory<?>>> allOptions() {
		return allOptions;
	}

	public Parameter<List<Attribute<?>>> attributes() {
		return targetAttributesParameter;
	}

	public Identifier id() {
		return wrapped.id();
	}

	@Override
	public String getName() {
		return wrapped.getName();
	}

	@Override
	public String getDescription() {
		return wrapped.getDescription();
	}

	@Override
	public Class<?> getType() {
		return wrapped.getType();
	}

	@Override
	public void set(Supplier<ModelFactory<?>> value) {
		wrapped.set(value);
	}

	@Override
	public void setByString(String value) {
		wrapped.setByString(value);
	}

	@Override
	public Supplier<ModelFactory<?>> current() {
		return wrapped.current();
	}

	@Override
	public boolean isValid() {
		return wrapped.isValid();
	}

	@Override
	public String getValueCorrectionHint() {
		return wrapped.getValueCorrectionHint();
	}

	private Map<ParameterListener, ParameterListener> listenerReferrer = new HashMap<>();

	@Override
	public final void addListener(final ParameterListener listener) {
		// adding referrer that notifies listener with update for this parameter
		// (instead of wrapped)
		ParameterListener referrer = new ParameterListener() {
			@Override
			public void notifyValueChanged(Parameter<?> parameter) {
				listener.notifyValueChanged(ModelClassParameter.this);
			}
		};
		this.wrapped.addListener(referrer);

		// storing referrer for listener in order to support deregistration
		this.listenerReferrer.put(listener, referrer);
	}

	@Override
	public void removeListener(ParameterListener listener) {
		if (!listenerReferrer.containsKey(listener)) {
			return;
		}
		this.wrapped.removeListener(listenerReferrer.get(listener));
		// this.listenerReferrer.remove(listener);
	}

	@Override
	public boolean isContextValid() {
		return wrapped.isContextValid();
	}

	@Override
	public List<Parameter<?>> getDependsOnParameters() {
		return wrapped.getDependsOnParameters();
	}

	@Override
	public Collection<? extends Supplier<ModelFactory<?>>> getRange() {
		return wrapped.getRange();
	}

	@Override
	public boolean hidden() {
		return false;
	}

	public BernoulliOption bernoulliOption() {
		return bernoulliOption;
	}

}

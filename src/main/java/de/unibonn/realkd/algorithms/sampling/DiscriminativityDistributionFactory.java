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

package de.unibonn.realkd.algorithms.sampling;

import static de.unibonn.realkd.common.base.Identifier.id;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.emm.PosNegDecider.MultipleAttributesPosNegDecider;
import de.unibonn.realkd.algorithms.emm.PosNegDecider.PCAPosNegDecider;
import de.unibonn.realkd.algorithms.emm.PosNegDecider.SingleAttributePosNegDecider;
import de.unibonn.realkd.common.parameter.DefaultRangeEnumerableParameter.RangeComputer;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.ParameterContainer;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import edu.uab.consapt.sampling.TwoStepPatternSampler;
import edu.uab.consapt.sampling.TwoStepPatternSamplerFactory;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;

/**
 * Constructs discriminativity distribution with configurable powers for each
 * factor.
 * 
 * @author Mario Boley
 *
 * @since 0.1.0
 * 
 * @version 0.3.0
 * 
 */
public class DiscriminativityDistributionFactory implements DistributionFactory, ParameterContainer {

	public static enum GlobalFreqPower {

		ZERO(0), ONE(1), TWO(2);

		private int intValue;

		private GlobalFreqPower(int intValue) {
			this.intValue = intValue;
		}

		public int intValue() {
			return intValue;
		}

		public String toString() {
			return String.valueOf(intValue);
		}

	}

	public static enum PosFreqPower {

		ONE(1), TWO(2), THREE(3);

		private int intValue;

		private PosFreqPower(int intValue) {
			this.intValue = intValue;
		}

		public int intValue() {
			return intValue;
		}

		public String toString() {
			return String.valueOf(intValue);
		}

	}

	public static enum NegInvFreqPower {

		ONE(1), TWO(2), THREE(3);

		private int intValue;

		private NegInvFreqPower(int intValue) {
			this.intValue = intValue;
		}

		public int intValue() {
			return intValue;
		}

		public String toString() {
			return String.valueOf(intValue);
		}

	}

	private final Parameter<List<Attribute<?>>> targetAttributesParameter;
	private final Parameter<DataTable> targetTableParameter;

	private final Parameter<GlobalFreqPower> powerOfFrequency;
	private final Parameter<PosFreqPower> powerOfPosFrequency;
	private final Parameter<NegInvFreqPower> powerOfNegFrequency;

	private final Parameter<Supplier<IntPredicate>> dBCreatorParameter;
	private final List<Parameter<?>> parameters;

	private class SingleAttributeBasedSplitterSupplier implements Supplier<IntPredicate> {

		@Override
		public IntPredicate get() {
			return new SingleAttributePosNegDecider(targetAttributesParameter.current().get(0));
		}

		@Override
		public String toString() {
			return "Split on first attribute";
		}

		public boolean equals(Object other) {
			return (other instanceof SingleAttributeBasedSplitterSupplier);
		}

	}

	private class MultipleAttributeBasedSplitterSupplier implements Supplier<IntPredicate> {

		@Override
		public IntPredicate get() {
			return new MultipleAttributesPosNegDecider(targetAttributesParameter.current());
		}

		@Override
		public String toString() {
			return "Split on all attributes";
		}

		public boolean equals(Object other) {
			return (other instanceof MultipleAttributeBasedSplitterSupplier);
		}

	}

	private class PCABasedSplitterSupplier implements Supplier<IntPredicate> {

		@Override
		public IntPredicate get() {
			return new PCAPosNegDecider(targetTableParameter.current(), targetAttributesParameter.current());
		}

		@Override
		public String toString() {
			return "Split on 1st PC of all attributes";
		}

		public boolean equals(Object other) {
			return (other instanceof PCABasedSplitterSupplier);
		}

	}

	private final Supplier<IntPredicate> splitOnMultipleOption = new MultipleAttributeBasedSplitterSupplier();

	private final Supplier<IntPredicate> splitByPCAOption = new PCABasedSplitterSupplier();
	private final Predicate<Proposition> propositionFilter;

	public DiscriminativityDistributionFactory(final Parameter<DataTable> targetTableParameter,
			final Parameter<List<Attribute<?>>> targetAttributesParameter, Predicate<Proposition> propositionFilter) {

		this.targetTableParameter = targetTableParameter;
		this.targetAttributesParameter = targetAttributesParameter;

		this.powerOfFrequency = Parameters.rangeEnumerableParameter(id("a"), "a", "Power of global frequency factor.",
				GlobalFreqPower.class, () -> ImmutableList.copyOf(GlobalFreqPower.values()));
		this.powerOfPosFrequency = Parameters.rangeEnumerableParameter(id("b"), "b",
				"Power of factor for frequency in positive data portion.", PosFreqPower.class,
				() -> ImmutableList.copyOf(PosFreqPower.values()));
		this.powerOfNegFrequency = Parameters.rangeEnumerableParameter(id("c"), "c",
				"Power of factor for inverse frequency in negative data portion", NegInvFreqPower.class,
				() -> ImmutableList.copyOf(NegInvFreqPower.values()));

		this.propositionFilter = propositionFilter;

		this.dBCreatorParameter = Parameters.rangeEnumerableParameter(id("splitting"),"Splitting method",
				"The way data is split into a positive and a negative portion", IntPredicate.class,
				new RangeComputer<Supplier<IntPredicate>>() {

					@Override
					public List<Supplier<IntPredicate>> get() {
						List<Supplier<IntPredicate>> result = new ArrayList<>();
						if (targetAttributesParameter.current().size() > 0) {
							result.add(new SingleAttributeBasedSplitterSupplier());
						}
						if (targetAttributesParameter.current().size() > 1) {
							result.add(splitOnMultipleOption);
						}
						if (targetAttributesParameter.current().size() > 1
								&& allMetric(targetAttributesParameter.current())) {
							result.add(splitByPCAOption);
							// result.add(PosNegDatabaseCreator.PosNegDatabaseUsingPCA.INSTANCE);
						}

						return result;
					}

					private boolean allMetric(List<Attribute<?>> attributes) {
						for (Attribute<?> attribute : attributes) {
							if (!(attribute instanceof MetricAttribute)) {
								return false;
							}
						}
						return true;
					}
				}, targetAttributesParameter);
		this.parameters = ImmutableList.of(powerOfFrequency, powerOfPosFrequency, powerOfNegFrequency,
				dBCreatorParameter);
	}

	@Override
	public TwoStepPatternSampler getDistribution(PropositionalContext propositionalLogic) {
		PosNegDbInterface db = ConsaptUtils.createPosNegDb(propositionalLogic, dBCreatorParameter.current().get(),
				propositionFilter);

		try {
			return TwoStepPatternSamplerFactory.createDiscrTimesFreqDistribution(db,
					powerOfFrequency.current().intValue(), powerOfPosFrequency.current().intValue() - 1,
					powerOfNegFrequency.current().intValue() - 1);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "P(x)=freq(x)^a freq_p(x)^b (1-freq_n(x))^c";
		// return "P=" + ((powerOfPosFrequency > 0) ? "pfreq" : "")
		// + ((powerOfPosFrequency > 1) ? "^" + powerOfPosFrequency : "")
		// + ((powerOfNegFrequency > 0) ? "(1-nfreq)" : "")
		// + ((powerOfNegFrequency > 1) ? "^" + powerOfNegFrequency : "") +
		// ((powerOfFrequency > 0) ? "freq" : "")
		// + ((powerOfFrequency > 1) ? "^" + powerOfFrequency : "");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DiscriminativityDistributionFactory))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiscriminativityDistributionFactory other = (DiscriminativityDistributionFactory) obj;
		if (this.powerOfFrequency.current() == other.powerOfFrequency.current()
				&& this.powerOfNegFrequency.current() == other.powerOfNegFrequency.current()
				&& this.powerOfPosFrequency.current() == other.powerOfPosFrequency.current())
			// && this.propositionalLogic.equals(other.propositionalLogic))
			// && this.targetAttributes.equals(other.targetAttributes))
			return true;
		return false;
	}

	@Override
	public List<Parameter<?>> getTopLevelParameters() {
		return parameters;
	}

	public void powerOfGlobalFrequency(GlobalFreqPower power) {
		this.powerOfFrequency.set(power);
	}

	public void powerOfPositiveFrequency(PosFreqPower power) {
		this.powerOfPosFrequency.set(power);
	}

	public void powerOfNegativeInverseFrequency(NegInvFreqPower power) {
		this.powerOfNegFrequency.set(power);
	}

	public GlobalFreqPower powerOfGlobalFrequency() {
		return powerOfFrequency.current();
	}

	public PosFreqPower powerOfPositiveFrequency() {
		return powerOfPosFrequency.current();
	}

	public NegInvFreqPower powerOfNegativeInverseFrequency() {
		return powerOfNegFrequency.current();
	}

}

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
import static de.unibonn.realkd.common.parameter.Parameters.rangeEnumerableParameter;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.ParameterContainer;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import edu.uab.consapt.sampling.TwoStepPatternSampler;
import edu.uab.consapt.sampling.TwoStepPatternSamplerFactory;

/**
 * 
 * @author Sandy Moens
 * 
 * @since 0.1.0
 * 
 * @version 0.3.0
 *
 */
public final class FrequencyDistributionFactory implements DistributionFactory, ParameterContainer {

	private final Parameter<FreqPower> power;

	private final Predicate<Proposition> filter;

	private final List<Parameter<?>> parameters;

	public static enum FreqPower {

		ONE(1), TWO(2), THREE(3), FOUR(4);

		private int intValue;

		private FreqPower(int intValue) {
			this.intValue = intValue;
		}

		public int intValue() {
			return intValue;
		}

		public String toString() {
			return String.valueOf(intValue);
		}

	}

	public FrequencyDistributionFactory() {
		this(p -> true);
	}

	public FrequencyDistributionFactory(Predicate<Proposition> propositionFilter) {
		List<FreqPower> powerOptions = ImmutableList.copyOf(FreqPower.values());
		this.power = rangeEnumerableParameter(id("a"),"a", "The power of frequency used in seed distribution.", FreqPower.class, () -> powerOptions);
		this.filter = propositionFilter;
		this.parameters = ImmutableList.of(power);
	}

	@Override
	public TwoStepPatternSampler getDistribution(PropositionalContext propositionalLogic) {
		try {
			return TwoStepPatternSamplerFactory.createFreqTimesFreqDistribution(
					ConsaptUtils.createTransactionDbFromPropositionalLogic(propositionalLogic, filter),
					power.current().intValue - 1);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		// return "P=freq" + ((power > 1) ? "^" + power : "");
		return "P(x)=freq(x)^a";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FrequencyDistributionFactory other = (FrequencyDistributionFactory) obj;
		if (this.power.current() == other.power.current())
			return true;
		return false;
	}

	@Override
	public List<Parameter<?>> getTopLevelParameters() {
		return parameters;
	}

}
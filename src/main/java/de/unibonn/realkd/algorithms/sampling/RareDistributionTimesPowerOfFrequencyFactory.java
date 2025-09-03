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

import static de.unibonn.realkd.algorithms.sampling.RareDistributionTimesPowerOfFrequencyFactory.FreqPower.ONE;
import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.parameter.Parameters.rangeEnumerableParameter;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.ParameterContainer;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import edu.uab.consapt.sampling.TwoStepPatternSampler;
import edu.uab.consapt.sampling.TwoStepPatternSamplerFactory;

public class RareDistributionTimesPowerOfFrequencyFactory implements DistributionFactory, ParameterContainer {

	private final Parameter<FreqPower> power;

	private final List<Parameter<?>> parameters;

	public static enum FreqPower {

		ZERO(0), ONE(1), TWO(2), THREE(3), FOUR(4);

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

	public RareDistributionTimesPowerOfFrequencyFactory() {
		List<FreqPower> powerOptions = ImmutableList.copyOf(FreqPower.values());
		this.power = rangeEnumerableParameter(id("a"),"a", "The power of frequency used in seed distribution.", FreqPower.class, () -> powerOptions);
		this.power.set(ONE);
		this.parameters = ImmutableList.of(power);
	}

	@Override
	public TwoStepPatternSampler getDistribution(PropositionalContext propositionalLogic) {
		try {
			return TwoStepPatternSamplerFactory.createRareTimesFreqDistribution(
					ConsaptUtils.createTransactionDbFromPropositionalLogic(propositionalLogic),
					power.current().intValue());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void power(FreqPower option) {
		this.power.set(option);
	}

	@Override
	public String toString() {
		// return "P=rare" + ((power > 0) ? ("*freq" + ((power > 1) ? "^" +
		// power : "")) : "");
		return "P(x)=rarity(x)freq(x)^a";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RareDistributionTimesPowerOfFrequencyFactory other = (RareDistributionTimesPowerOfFrequencyFactory) obj;
		if (this.power == other.power)
			return true;
		return false;
	}

	@Override
	public List<Parameter<?>> getTopLevelParameters() {
		return parameters;
	}

}
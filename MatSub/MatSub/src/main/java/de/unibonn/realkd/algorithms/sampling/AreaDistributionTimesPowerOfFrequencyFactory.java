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

import de.unibonn.realkd.data.propositions.PropositionalContext;
import edu.uab.consapt.sampling.TwoStepPatternSampler;
import edu.uab.consapt.sampling.TwoStepPatternSamplerFactory;

public class AreaDistributionTimesPowerOfFrequencyFactory implements
		DistributionFactory {

	private int power;

	public AreaDistributionTimesPowerOfFrequencyFactory(int powerOfFrequency) {
		this.power = powerOfFrequency;
	}

	@Override
	public TwoStepPatternSampler getDistribution(PropositionalContext propositionalLogic) {
		try {
			return TwoStepPatternSamplerFactory
					.createAreaTimesFreqDistribution(ConsaptUtils
							.createTransactionDbFromPropositionalLogic(propositionalLogic), power);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "P=area"
				+ ((power > 0) ? ("*freq" + ((power > 1) ? "^" + power : ""))
						: "");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AreaDistributionTimesPowerOfFrequencyFactory other = (AreaDistributionTimesPowerOfFrequencyFactory) obj;
		if (this.power == other.power)
			return true;
		return false;
	}

}
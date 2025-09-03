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

import java.util.List;
import java.util.function.Predicate;

import de.unibonn.realkd.algorithms.emm.PosNegDecider.SingleAttributePosNegDecider;
import de.unibonn.realkd.algorithms.emm.RowWeightComputer;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import edu.uab.consapt.sampling.TwoStepPatternSampler;
import edu.uab.consapt.sampling.TwoStepPatternSamplerFactory;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;
import ua.ac.be.mime.plain.weighting.PosNegTransactionDb;

public class WeightedDiscriminativityDistributionFactory implements DistributionFactory {

	private final int powerOfFrequency;
	private final int powerOfPosFrequency;
	private final int powerOfNegFrequency;
	
	private final Predicate<Proposition> propositionFilter;

	private final RowWeightComputer rowWeightComputer;
	private final Parameter<PropositionalContext> propositionalLogicParameter;
	private final Parameter<List<Attribute<?>>> targetAttributesParameter;
	private final Parameter<DataTable> targetTableParameter;

	public WeightedDiscriminativityDistributionFactory(final Parameter<DataTable> targetTableParameter,
			final Parameter<List<Attribute<?>>> targetAttributesParameter,
			final Parameter<PropositionalContext> propositionalLogicParameter,
			final int powerOfFrequency, final int powerOfPosFrequency,
			final int powerOfNegFrequency, final RowWeightComputer rowWeightComputer, Predicate<Proposition> propositionFilter) {
		// if (propositionalLogic == null) {
		// throw new IllegalArgumentException(
		// "Propositional logic must not be null");
		// }
		// if (targets == null) {
		// throw new IllegalArgumentException(
		// "List of target attributes must not be null");
		// }
		// if (targets.isEmpty()) {
		// throw new IllegalArgumentException(
		// "List of target attributes must be non-empty");
		// }
		this.propositionalLogicParameter = propositionalLogicParameter;
		this.propositionFilter = propositionFilter;
		this.targetTableParameter = targetTableParameter;
		this.targetAttributesParameter = targetAttributesParameter;
		this.powerOfFrequency = powerOfFrequency;
		this.powerOfPosFrequency = powerOfPosFrequency;
		this.powerOfNegFrequency = powerOfNegFrequency;
		this.rowWeightComputer = rowWeightComputer;
//		this.posNegDatabaseByFirstAttribute = PosNegDatabaseCreator.PosNegDatabaseByFirstAttribute.INSTANCE;

	}

	@Override
	public TwoStepPatternSampler getDistribution(PropositionalContext propositionalLogic) {

		if (targetAttributesParameter.current().size() < 1) {
			throw new IllegalStateException("can only build sampling distribution if one or more targets selected");
		}
		
		PosNegDbInterface db = ConsaptUtils.createPosNegDb(propositionalLogic,
				new SingleAttributePosNegDecider(targetAttributesParameter.current().get(0)), propositionFilter);

		
		try {
			return TwoStepPatternSamplerFactory.createWeightedDiscrTimesFreqDistribution(db, powerOfFrequency,
					powerOfPosFrequency - 1, powerOfNegFrequency - 1,
					rowWeightComputer.getRowWeights(targetTableParameter.current(),
							targetAttributesParameter.current(), (PosNegTransactionDb) db));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "Weighted P=" + ((powerOfPosFrequency > 0) ? "pfreq" : "")
				+ ((powerOfPosFrequency > 1) ? "^" + powerOfPosFrequency : "")
				+ ((powerOfNegFrequency > 0) ? "(1-nfreq)" : "")
				+ ((powerOfNegFrequency > 1) ? "^" + powerOfNegFrequency : "") + ((powerOfFrequency > 0) ? "freq" : "")
				+ ((powerOfFrequency > 1) ? "^" + powerOfFrequency : "");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof WeightedDiscriminativityDistributionFactory))
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeightedDiscriminativityDistributionFactory other = (WeightedDiscriminativityDistributionFactory) obj;
		if (this.rowWeightComputer.getClass() != other.rowWeightComputer.getClass()) {
			return false;
		}
		if (this.powerOfFrequency == other.powerOfFrequency && this.powerOfNegFrequency == other.powerOfNegFrequency
				&& this.powerOfPosFrequency == other.powerOfPosFrequency
				&& this.propositionalLogicParameter == other.propositionalLogicParameter
				&& this.targetAttributesParameter == other.targetAttributesParameter)
			return true;
		return false;
	}
}
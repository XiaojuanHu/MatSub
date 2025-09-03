package de.unibonn.realkd.patterns.emm;

import static de.unibonn.realkd.common.base.Identifier.id;
import static java.lang.Math.max;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.models.bernoulli.BernoulliDistribution;
import de.unibonn.realkd.patterns.subgroups.Subgroup;

/**
 * 
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public enum PositiveProbabilityShift
		implements ModelDeviationMeasure, MeasurementProcedure<ModelDeviationMeasure, Object> {

	POSITIVE_PROBABILITY_SHIFT;

	public Identifier identifier() {
		return id("pos_prob_shift");
	}

	@Override
	public String caption() {
		return "Positive probability shift";
	}

	@Override
	public String description() {
		return "Difference between event probability in subgroup and refernce event probability or zero if difference negative.";
	}
	
	@Override
	public boolean isApplicable(Object descriptor) {
		return (descriptor instanceof Subgroup
				&& ((Subgroup<?>) descriptor).localModel() instanceof BernoulliDistribution);
	}

	@Override
	public ModelDeviationMeasure getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Object descriptor) {
		if (!(descriptor instanceof Subgroup)) {
			return Measures.measurement(getMeasure(), Double.NaN); 
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;
		BernoulliDistribution localModel = (BernoulliDistribution) subgroup.localModel();
		BernoulliDistribution referenceModel = (BernoulliDistribution) subgroup.referenceModel();
		return Measures.measurement(getMeasure(), max(localModel.probability() - referenceModel.probability(), 0));
	}

	@Override
	public String toString() {
		return caption();
	}

}

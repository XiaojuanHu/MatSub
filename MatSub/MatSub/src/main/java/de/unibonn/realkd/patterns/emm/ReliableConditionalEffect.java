package de.unibonn.realkd.patterns.emm;

import static de.unibonn.realkd.common.measures.Measures.measurement;
import static java.lang.Math.sqrt;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.patterns.models.Model;
import de.unibonn.realkd.patterns.models.conditional.DiscretelyConditionedBernoulli;
import de.unibonn.realkd.patterns.models.conditional.EmpiricalBernoulliDistribution;
import de.unibonn.realkd.patterns.models.table.Cell;
import de.unibonn.realkd.patterns.subgroups.Subgroup;

/**
 * @author Kailash Budhathoki
 *
 * @since 0.7.0
 * 
 * @version 0.7.0
 * 
 */
public enum ReliableConditionalEffect implements ModelDeviationMeasure {

	RELIABLE_CONDITIONAL_EFFECT;

	@Override
	public Identifier identifier() {
		return Identifier.identifier("reliableCondEffect");
	}

	@Override
	public String caption() {
		return "Reliable Conditional Effect";
	}

	@Override
	public String toString() {
		return caption();
	}

	@Override
	public String description() {
		return "Confidence difference of event probability between subgroup and complement conditioned on set of potential confounder variables.";
	}

	@Override
	public boolean isApplicable(Object descriptor) {
		if (!(descriptor instanceof Subgroup)) {
			return false;
		}
		Subgroup<?> sg = (Subgroup<?>) descriptor;
		if (sg.targetAttributes().size() < 1) {
			return false;
		}
		Model localModel = sg.localModel();
		Model referenceModel = sg.referenceModel();
		if (!(localModel instanceof DiscretelyConditionedBernoulli)
				|| !(referenceModel instanceof DiscretelyConditionedBernoulli)) {
			return false;
		}
		return true;
	}

	@Override
	public ModelDeviationMeasure getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Object descriptor) {
		Subgroup<?> sg = (Subgroup<?>) descriptor;

		DiscretelyConditionedBernoulli localModel = (DiscretelyConditionedBernoulli) sg.localModel();
		DiscretelyConditionedBernoulli referenceModel = (DiscretelyConditionedBernoulli) sg.referenceModel();

		Set<Cell> controlCells = new HashSet<Cell>(localModel.cells());
		controlCells.addAll(referenceModel.cells());

		int nTotal = 0;
		double race = 0;
		double ace = 0;
		for (Cell controlCell : controlCells) {
			EmpiricalBernoulliDistribution localConditionalTable = localModel.conditionalTable(controlCell);
			EmpiricalBernoulliDistribution referenceConditionalTable = referenceModel.conditionalTable(controlCell);

			int nLocal = localConditionalTable.totalCount();
			int nReference = referenceConditionalTable.totalCount();
			int nControl = nLocal + nReference;
			nTotal += nControl;

			int a = localConditionalTable.positiveCount();
			int c = referenceConditionalTable.positiveCount();

			double effect = (a + 1.0) / (nLocal + 2) - (c + 1.0) / (nReference + 2);
			ace += (nControl + 4) * effect;
			race += (nControl + 4) * (effect - 1.0 / sqrt(nLocal + 2) - 1.0 / sqrt(nReference + 2));
		}
		race /= (nTotal + 4 * controlCells.size());
		ace /= (nTotal + 4 * controlCells.size());

		return measurement(this, race, ImmutableList.of(measurement(AverageConditionalEffect.AVERAGE_CONDITIONAL_EFFECT, ace)));
	}

}

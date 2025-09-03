/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.emm;

import static de.unibonn.realkd.common.measures.Measures.measurement;

import java.util.HashSet;
import java.util.Set;

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
public enum AverageConditionalEffect implements ModelDeviationMeasure {
	
	AVERAGE_CONDITIONAL_EFFECT;
	
	@Override
	public Identifier identifier() {
		return Identifier.identifier("average_cond_effect");
	}

	@Override
	public String caption() {
		return "Average Conditional Effect";
	}

	@Override
	public String description() {
		return "Difference of event probability between subgroup and complement conditioned on set of potential confounder variables.";
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
		double res = 0;
		for (Cell controlCell : controlCells) {
			EmpiricalBernoulliDistribution localConditionalTable = localModel.conditionalTable(controlCell);
			EmpiricalBernoulliDistribution referenceConditionalTable = referenceModel.conditionalTable(controlCell);

			int nLocal = localConditionalTable.totalCount();
			int nReference = referenceConditionalTable.totalCount();
			int nControl = nLocal + nReference;
			nTotal += nControl;

			int a = localConditionalTable.positiveCount();
			int c = referenceConditionalTable.positiveCount();
			
			res += nControl * (a / nLocal - c / nReference);
		}
		res /= nTotal;
	
		return measurement(this, res);
	}

}

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
package de.unibonn.realkd.algorithms.branchbound;

import static de.unibonn.realkd.patterns.emm.ReliableConditionalEffect.RELIABLE_CONDITIONAL_EFFECT;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

import java.util.HashSet;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import de.unibonn.realkd.algorithms.branchbound.BranchAndBoundSearch.BranchAndBoundSearchNode;
import de.unibonn.realkd.common.JsonSerializable;
import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;
import de.unibonn.realkd.patterns.emm.ModelDeviationMeasure;
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
public final class OptimisticEstimators {
	
	@KdonTypeName("rceLoose")
	@KdonDoc("Loose optimistic estimator for the reliable conditional effect")
	public static class RceLooseOptimisticEstimator implements OptimisticEstimators.OptimisticEstimatorOption {
		
		@Override
		public boolean valid(ModelDeviationMeasure deviationMeasure) {
			return deviationMeasure.getMeasure().equals(RELIABLE_CONDITIONAL_EFFECT);
		}
		
		private double bound(ExceptionalModelPattern pattern) {
			Subgroup<?> sg = pattern.descriptor();
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

				res += (nControl + 4) * ((a + 1.0) / (a + 2) - (c + 1.0) / (nControl - a + 2) - 1.0 / sqrt(a + 2));
			}
			res /= (nTotal + 4 * controlCells.size());
			return res;
		}
		
		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			return n -> bound(n.content);
		}

	}
	
	@KdonTypeName("rceTight")
	@KdonDoc("Tight optimistic estimator for the reliable conditional effect")
	public static class RceTightOptimisticEstimator implements OptimisticEstimators.OptimisticEstimatorOption {
	
		@Override
		public boolean valid(ModelDeviationMeasure deviationMeasure) {
			return deviationMeasure.getMeasure().equals(RELIABLE_CONDITIONAL_EFFECT);
		}
	
		private double bound(ExceptionalModelPattern pattern) {
			Subgroup<?> sg = pattern.descriptor();
	
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
				int nOne = a + c;
	
				double opt = Double.NEGATIVE_INFINITY;
				for (int x = 0; x <= a; ++x) {
					opt = max(opt, (x + 1.0) / (x + 2) - (nOne - x + 1.0) / (nControl - x + 2) - 1.0 / sqrt(x + 2)
							- 1.0 / sqrt(nControl - x + 2));
				}
				res += (nControl + 4) * opt;
			}
			res /= (nTotal + 4 * controlCells.size());
			return res;
		}
	
		@Override
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get() {
			return n -> bound(n.content);
		}
	
		@Override
		public String toString() {
			return "tight";
		}

	}

	@KdonTypeName("optimisticEstimator")
	public static interface OptimisticEstimatorOption extends JsonSerializable {
	
		public boolean valid(ModelDeviationMeasure deviationMeasure);
	
		public ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> get();
		
	}	

}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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

import static de.unibonn.realkd.common.base.Identifier.id;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;
import de.unibonn.realkd.patterns.models.UnivariateOrdinalProbabilisticModel;
import de.unibonn.realkd.patterns.subgroups.Subgroup;

/**
 *
 * Measures the Kolmogorov-Smirnov statistic for subgroups with a single ordinal
 * target attribute by iterating over the empirical data points in ascending
 * order of their values.
 *
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public enum KolmogorovSmirnovStatistic implements ModelDeviationMeasure, Identifiable {

	KOLMOGOROV_SMIRNOV_STATISTIC;

	public Identifier identifier() {
		return id("kolmogorov_smirnov_statistic");
	}

	@Override
	public String caption() {
		return "Kolmogorov-Smirnov statistic";
	}

	@Override
	public String description() {
		return "Maximum difference between cumulative target distributions of global and subgroup data.";
	}

	@Override
	public boolean isApplicable(Object descriptor) {
		return (descriptor instanceof Subgroup)
				&& (((Subgroup<?>) descriptor).localModel() instanceof UnivariateOrdinalProbabilisticModel);
	}

	@Override
	public KolmogorovSmirnovStatistic getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Object descriptor) {
		if (!isApplicable(descriptor)) {
			return Measures.measurement(this, Double.NaN);
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;
		OrdinalAttribute<?> target = (OrdinalAttribute<?>) subgroup.targetAttributes().get(0);

		double maxDiff = 0.0;
		int subCumCount = 0;
		int globalNonMissingCount = target.sortedNonMissingRowIndices().size();
		int localNonMissingCount = (int) subgroup.supportSet().stream().filter(i -> !target.valueMissing(i)).count();
		if (localNonMissingCount == 0) {
			return Measures.measurement(getMeasure(), Double.NaN);
		}
		for (int i = 0; i < globalNonMissingCount; i++) {
			int obj = target.sortedNonMissingRowIndices().get(i);
			if (subgroup.supportSet().contains(obj)) {
				subCumCount++;
			}
			double diff = Math.abs((double) i / globalNonMissingCount - (double) subCumCount / localNonMissingCount);
			maxDiff = Math.max(maxDiff, diff);
		}
		return Measures.measurement(getMeasure(), maxDiff);
	}

	@Override
	public String toString() {
		return caption();
	}

}

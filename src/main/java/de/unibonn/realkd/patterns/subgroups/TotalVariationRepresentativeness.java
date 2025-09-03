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
package de.unibonn.realkd.patterns.subgroups;

import static de.unibonn.realkd.common.base.Identifier.id;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.models.table.Cell;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;

/**
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public enum TotalVariationRepresentativeness implements RepresentativenessMeasure<ContingencyTable> {

	TOTAL_VARIATION_REPRESENTATIVENESS;

	@Override
	public String caption() {
		return "TV representativeness";
	}

	@Override
	public String description() {
		return "One minus total variation distance between local and reference model of control variables.";
	}

	public Measurement measurement(ControlledSubgroup<?, ContingencyTable> subgroup) {
		double tvd = subgroup.localControlModel().totalVariationDistance(subgroup.referenceControlModel());
		return Measures.measurement(this, 1 - tvd / maxDeviation(subgroup.referenceControlModel()));
	}

	private double maxDeviation(ContingencyTable model) {
		//TODO must take into account existence of zero cells for correct norm factor
		double minProb = Double.POSITIVE_INFINITY;
		for (Cell v : model.nonZeroCells()) {
			minProb = Math.min(minProb, model.probability(v));
		}
		return 1 - minProb;
	}

	@Override
	public Identifier identifier() {
		return id("total_variation_representativeness");
	}

}

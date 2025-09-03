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
import static java.lang.Math.max;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;
import de.unibonn.realkd.patterns.models.table.TwoDimensionalContingencyTable;

/**
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public enum MutualInformationGain implements Measure, MeasurementProcedure<MutualInformationGain, Subgroup<?>>, Identifiable {

	MUTUAL_INFORMATION_GAIN;

	@Override
	public String caption() {
		return "Mutual inf. gain";
	}

	@Override
	public String description() {
		return "The relative gain of normalized mutual information between first and second dimension in subgroup (when compared to reference population).";
	}
	
	@Override
	public boolean isApplicable(Subgroup<?> subgroup) {
		return (subgroup.localModel() instanceof TwoDimensionalContingencyTable)
				&& (subgroup.referenceModel() instanceof TwoDimensionalContingencyTable);
	}

	@Override
	@JsonIgnore
	public MutualInformationGain getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Subgroup<?> subgroup) {
		double globalMI = ((TwoDimensionalContingencyTable) subgroup.referenceModel()).mutualInformation();
		double globalH = ((ContingencyTable) subgroup.referenceModel()).entropy();
		if (globalMI < globalH) {
			double localMI = ((TwoDimensionalContingencyTable) subgroup.localModel()).mutualInformation();
			double localH = ((ContingencyTable) subgroup.localModel()).entropy();
			return Measures.measurement(getMeasure(),
					max(0, localMI / localH - globalMI / globalH) / (1 - globalMI / globalH));
		} else {
			return Measures.measurement(getMeasure(), Double.NaN);
		}
	}

	@Override
	public String toString() {
		return caption();
	}

	@Override
	public Identifier identifier() {
		return id("mutual_information_gain");
	}


}

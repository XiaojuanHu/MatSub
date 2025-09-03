/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.logical;

import static de.unibonn.realkd.common.base.Identifier.id;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.patterns.Frequency;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternDescriptor;
import de.unibonn.realkd.patterns.QualityMeasureId;

/**
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.7.1
 *
 * @version 0.7.1
 *
 */
public enum Lift implements Measure, MeasurementProcedure<Measure, PatternDescriptor> {

	LIFT;
	
	private Lift() {
		;
	}
	
	@Override
	public Identifier identifier() {
		return id("lift");
	}

	@Override
	public String caption() {
		return "lift";
	}

	@Override
	public String description() {
		return "Normalized difference between actual frequency and expected frequency of pattern";
	}

	@JsonIgnore
	@Override
	public Measure getMeasure() {
		return this;
	}
	
	@Override
	public boolean isApplicable(PatternDescriptor descriptor) {
		return descriptor instanceof LogicalDescriptor;
	}

	private double computeProductOfIndividualFrequencies(LogicalDescriptor description) {
		double product = 1.0;
		int populationSize = description.population().size();

		for (Proposition literal : description.elements()) {
			product *= literal.supportCount() / (double) populationSize;
		}

		return product;
	}
	
	@Override
	public Measurement perform(PatternDescriptor descriptor) {

		LogicalDescriptor logicalDescriptor = (LogicalDescriptor) descriptor;

		double productOfFrequencies = computeProductOfIndividualFrequencies(logicalDescriptor);

		double denominator = Math.pow(2, logicalDescriptor.size() - 2.);

		Measurement frequencyMeasurement = Frequency.FREQUENCY.perform(descriptor);
		
		double deviationOfFrequency = frequencyMeasurement.value() - productOfFrequencies;

		double lift = deviationOfFrequency / denominator;

		List<Measurement> auxiliaryMeasurements = ImmutableList.of(
				frequencyMeasurement,
				Measures.measurement(QualityMeasureId.EXPECTED_FREQUENCY,
						productOfFrequencies),
				Measures.measurement(QualityMeasureId.FREQUENCY_DEVIATION,
						deviationOfFrequency));

		return Measures.measurement(this, lift, auxiliaryMeasurements);
	}

}

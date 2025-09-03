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
package de.unibonn.realkd.patterns;

import static de.unibonn.realkd.common.base.Identifier.id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;

/**
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.5.0
 * 
 * @version 0.7.1
 *
 */
public enum Frequency implements Measure, MeasurementProcedure<Frequency, Object>, Identifiable {

	FREQUENCY;

	@Override
	public String caption() {
		return "frequency";
	}

	@Override
	public String description() {
		return "Relative occurance frequency of the pattern in the complete data.";
	}

	@Override
	public boolean isApplicable(Object descriptor) {
		return (descriptor instanceof LocalPatternDescriptor);
	}

	@Override
	@JsonIgnore
	public Frequency getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Object descriptor) {
		if (!(descriptor instanceof LocalPatternDescriptor)) {
			return Measures.measurement(this, Double.NaN);
		}
		
		LocalPatternDescriptor localDescriptor = (LocalPatternDescriptor) descriptor;
		
		Measurement support = Support.SUPPORT.perform(descriptor);
		
		double value = support.value() / localDescriptor.population().size();
		
		ImmutableList<Measurement> auxiliaryMeasurements = ImmutableList.of(support);
		
		return Measures.measurement(getMeasure(), value, auxiliaryMeasurements);
	}

	@Override
	public Identifier identifier() {
		return id("frequency");
	}

}

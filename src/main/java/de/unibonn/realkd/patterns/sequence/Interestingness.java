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
package de.unibonn.realkd.patterns.sequence;

import static de.unibonn.realkd.common.base.Identifier.id;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternDescriptor;

/**
 * Procedure for computing the interestingness of a sequence pattern in a
 * sequence database. This is defined as the harmonic mean between the
 * frequency and the cohesion of a sequence pattern.
 * 
 * @author Sandy Moens
 * 
 * @since 0.3.0
 * 
 * @version 0.7.1
 *
 */
public enum Interestingness implements Measure, MeasurementProcedure<Measure,PatternDescriptor> {

	INTERESTINGNESS;

	private Interestingness() {
		;
	}

	@Override
	public Identifier identifier() {
		return id("sequence_interestingness");
	}

	@Override
	public String caption() {
		return "interestingness";
	}

	@Override
	public String description() {
		return "The harmonic mean between the frequency and the cohesion of a pattern.";
	}

	@Override
	public boolean isApplicable(PatternDescriptor descriptor) {
		return SequenceDescriptor.class.isAssignableFrom(descriptor.getClass());
	}

	@Override
	public Measure getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(PatternDescriptor descriptor) {

		Measurement support = SequenceSupport.SEQUENCE_SUPPORT.perform(descriptor);
		
		Measurement cohesion = Cohesion.COHESION.perform(descriptor);
		
		double interestingness = (support.value() / ((SequenceDescriptor)descriptor).sequentialPropositionalLogic().sequences().size()) * cohesion.value();
		
		List<Measurement> auxiliaryMeasurements = ImmutableList.of(support, cohesion);
		
		return Measures.measurement(this, interestingness, auxiliaryMeasurements);
	}

}

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
package de.unibonn.realkd.patterns;

import static de.unibonn.realkd.common.base.Identifier.id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;

/**
 * Procedure for computing the support of a pattern. This is defined as: the
 * absolute occurrence frequency of the pattern in the complete data.
 * 
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.1.2
 * 
 * @version 0.7.1
 *
 */
public enum Support implements Measure, MeasurementProcedure<Measure, Object>, Identifiable {

	SUPPORT;
	
	private Support() {
		;
	}

	@Override
	public Identifier identifier() {
		return id("support");
	}

	@Override
	public String caption() {
		return "support";
	}

	@Override
	public String description() {
		return "Absolute occurrence count of the pattern in the complete data.";
	}

	@JsonIgnore
	@Override
	public Measure getMeasure() {
		return this;
	}

	@Override
	public boolean isApplicable(Object descriptor) {
		return descriptor instanceof LocalPatternDescriptor;
	}


	@Override
	public Measurement perform(Object descriptor) {

		LocalPatternDescriptor localPatternDescriptor = (LocalPatternDescriptor) descriptor;

		double support = (double) localPatternDescriptor.supportSet().size();

		return Measures.measurement(getMeasure(), support);
	}

}

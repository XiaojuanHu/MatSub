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
package de.unibonn.realkd.patterns.rules;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptors;

/**
 * Procedure for computing the confidence of a rule pattern based on logical
 * descriptors. This is defined as: the conditional probability of the
 * consequent occurring given that the antecedent occurs.
 * 
 * @author Sandy Moens
 * 
 * @since 0.1.2
 *
 * @version 0.7.1
 *
 */
public enum Confidence implements Measure, MeasurementProcedure<Measure, PatternDescriptor> {

	CONFIDENCE;
	
	private Confidence() {
		;
	}
	
	@Override
	public Identifier identifier() {
		return Identifier.id("confidence");
	}
	
	@Override
	public String caption() {
		return "confidence";
	}
	
	@Override
	public String description() {
		return "The conditional probability of the consequent occurring given that the antecedent occurs";
	}

	@JsonIgnore
	@Override
	public Confidence getMeasure() {
		return this;
	}

	@Override
	public boolean isApplicable(PatternDescriptor descriptor) {
		return descriptor instanceof RuleDescriptor;
	}
	
	@Override
	public Measurement perform(PatternDescriptor descriptor) {

		RuleDescriptor ruleDescriptor = (RuleDescriptor) descriptor;

		LogicalDescriptor antecedent = ruleDescriptor.getAntecedent();
		LogicalDescriptor consequent = ruleDescriptor.getConsequent();

		List<Proposition> union = newArrayList(antecedent.elements());
		union.addAll(consequent.elements());
		LogicalDescriptor unionDescriptor = LogicalDescriptors
				.create(ruleDescriptor.population(), union);

		double frequencyUnion = (double) unionDescriptor.supportSet().size()
				/ ruleDescriptor.population().size();

		double frequencyConsequent = (double) antecedent.supportSet().size()
				/ ruleDescriptor.population().size();

		return Measures.measurement(this, frequencyUnion / frequencyConsequent);
	}

}

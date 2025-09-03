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

import static de.unibonn.realkd.common.base.Identifier.id;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

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
 * @author Sandy Moens
 * 
 * @since 0.4.0
 *
 * @version 0.7.1
 *
 */
public enum RuleLift implements Measure, MeasurementProcedure<Measure, PatternDescriptor> {

	RULE_LIFT;
	
	private RuleLift() {
		;
	}
	
	@Override
	public Identifier identifier() {
		return id("rule_lift");
	}
	
	@Override
	public String caption() {
		return "rule lift";
	}
	
	@Override
	public String description() {
		return "Probability of the consequent divided by the probability of the antecedent";
	}

	@JsonIgnore
	@Override
	public RuleLift getMeasure() {
		return this;
	}
	
	@Override
	public boolean isApplicable(PatternDescriptor descriptor) {
		return descriptor instanceof RuleDescriptor;
	}

	@Override
	public Measurement perform(PatternDescriptor descriptor) {

		RuleDescriptor ruleDescriptor = (RuleDescriptor) descriptor;

		int logicSize = ruleDescriptor.population().size();

		LogicalDescriptor antecedent = ruleDescriptor.getAntecedent();
		LogicalDescriptor consequent = ruleDescriptor.getConsequent();

		List<Proposition> union = Lists.newArrayList(antecedent.elements());
		union.addAll(consequent.elements());
		LogicalDescriptor unionDescriptor = LogicalDescriptors.create(ruleDescriptor.population(), union);

		double frequencyUnion = 1. * unionDescriptor.supportSet().size() / logicSize;

		double frequencyAntecedent = 1. * antecedent.supportSet().size() / logicSize;

		double frequencyConsequent = 1. * consequent.supportSet().size() / logicSize;

		return Measures.measurement(this, frequencyUnion / (frequencyAntecedent * frequencyConsequent));
	}

}

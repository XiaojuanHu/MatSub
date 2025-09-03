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
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.rules.Confidence;
import de.unibonn.realkd.patterns.rules.DefaultRuleDescriptor;
import de.unibonn.realkd.patterns.rules.RuleDescriptor;

/**
 * @author Sandy Moens
 * 
 * @since 0.7.1
 * 
 * @version 0.7.1
 * 
 */
public enum MinimalConfidenceLift implements Measure, MeasurementProcedure<Measure, PatternDescriptor> {

	MINIMAL_CONFIDENCE_LIFT;
	private MinimalConfidenceLift() {
		;
	}

	@Override
	public Identifier identifier() {
		return id("minimal_confidence_lift");
	}

	@Override
	public String caption() {
		return "minimal confidence lift";
	}

	@Override
	public String description() {
		return "Minimal confidence lift";
	}

	@JsonIgnore
	@Override
	public Measure getMeasure() {
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

		double maxSubSetConf = 0;

		for (Proposition p : antecedent) {
			LogicalDescriptor generalization = antecedent.generalization(p);

			RuleDescriptor generalizedRule = DefaultRuleDescriptor.create(ruleDescriptor.population(), generalization,
					consequent);

			double conf = Confidence.CONFIDENCE.perform(generalizedRule).value();

			maxSubSetConf = maxSubSetConf < conf ? conf : maxSubSetConf;
		}

		if (maxSubSetConf == 0) {
			return Measures.measurement(this, Double.NaN);
		}

		Measurement confidence = Confidence.CONFIDENCE.perform(descriptor);
		double rule_lift = confidence.value() / maxSubSetConf;

		List<Measurement> auxiliaryMeasurements = ImmutableList.of(confidence);

		return Measures.measurement(this, confidence.value() - (confidence.value() / rule_lift), auxiliaryMeasurements);
	}

}

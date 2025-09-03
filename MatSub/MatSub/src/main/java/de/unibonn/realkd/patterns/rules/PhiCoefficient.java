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

import static de.unibonn.realkd.common.IndexSets.intersection;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternDescriptor;
import de.unibonn.realkd.patterns.rules.RuleDescriptor;

/**
 * @author Sandy Moens
 * 
 * @since 0.7.1
 * 
 * @version 0.7.1
 * 
 */
public enum PhiCoefficient implements Measure, MeasurementProcedure<Measure, PatternDescriptor> {

	PHI_COEFFICIENT;

	@Override
	public String caption() {
		return "phi coefficient";
	}

	@Override
	public String description() {
		return "Phi coefficient";
	}

	@Override
	public boolean isApplicable(PatternDescriptor descriptor) {
		return RuleDescriptor.class.isAssignableFrom(descriptor.getClass());
	}

	@JsonIgnore
	@Override
	public Measure getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(PatternDescriptor descriptor) {

		RuleDescriptor ruleDescriptor = (RuleDescriptor) descriptor;

		IndexSet e = ruleDescriptor.getAntecedent().supportSet();
		IndexSet g = ruleDescriptor.getConsequent().supportSet();

		double n = ruleDescriptor.population().size();
		double n11 = intersection(e, g).size();
		double n1_ = e.size();
		double n_1 = g.size();

		double numerator = n * n11 - n1_ * n_1;
		double denominator = Math.sqrt(n1_ * n_1 * (n - n1_) * (n - n_1));

		return Measures.measurement(this, numerator / denominator);
	}

	@Override
	public String toString() {
		return "Phi coefficient";
	}

	@Override
	public Identifier identifier() {
		return Identifier.id("phi_coefficient");
	}

}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
 */

package de.unibonn.realkd.patterns;

import static de.unibonn.realkd.common.base.Identifier.id;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;

/**
 * Enumeration of measurement identifiers for which measurements can be bound to
 * patterns.
 * 
 * @see Pattern
 * 
 * @author Björn Jacobs
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.1.0
 * 
 * @version 0.7.1
 *
 */
public enum QualityMeasureId implements Measure, Identifiable {

	@Deprecated LIFT(id("positive_lift"),"lift", "Normalized difference between actual frequency and expected frequency of pattern."), 
	NEGATIVE_LIFT(id("negative_list"), "negative lift", "Positive lift or zero in case actual lift is negative."), 
	ABSOLUTE_LIFT(id("absolute_lift"), "absolute lift", "Absolute lift value."), 
	EXPECTED_FREQUENCY(id("expected_frequency"), "expected frequency", "Expected frequency when assuming independenc of single propositions."), 
	FREQUENCY_DEVIATION(id("frequency_deviation"), "deviation of frequency", "Difference between actual frequency and expected frequency of pattern."), 
	OUTLIER_SCORE(id("outlier_score"),"outlier score", ""),
	REFERENCE_STD(id("reference_standard_deviation"), "global std. dev.", "Squareroot of 1-norm of attribute sample covariance matrix in global population."),
	LOCAL_STD(id("local_standard_deviation"),"local std. dev.", "Squareroot of 1-norm of attribute sample covariance matrix in subgroup."),
	@Deprecated AREA(id("area"),"area", "The true size of the pattern in the data as a combination of the support and the size of the pattern."), 
	@Deprecated SUPPORT(id("support"), "support", "Absolute occurance frequency of the pattern in the complete data."), 
	@Deprecated CONFIDENCE(id("rule_confidence"), "confidence", "The conditional probability of the consequent occurring given that the antecedent occurs."), 
	WEIBULL_SCALE(id("weibull_scale"), "scale","The scale parameter of the fitted distribution."),
	REFERENCE_WEIBULL_SCALE(id("reference_weibull_scale"), "global scale","The scale parameter of the Weibull distribution fitted to the global population"),
	LOCAL_WEIBULL_SCALE(id("local_weibull_scale"), "local scale","The scale parameter of the Weibull distribution fitted to the local population"), 
	LOCAL_RMSE(id("local_root_mean_squared_error"), "local rmse","The root mean squared error of the local model"),
	REFERENCE_RMSE(id("reference_root_mean_squared_error"), "ref. rmse","The root mean squared error of the reference model."),
	REF_ENTROPY(id("reference_entropy"), "ref. entropy","Shannon entropy of reference model."),
	LOCAL_ENTROPY(id("local_entropy"), "local entropy","Shannon entropy of local model."),
	LOCAL_MODE_PROBABILITY(id("local_mode_probability"),"local mode probability", "Maximum probability cell of local model."),
	REFERENCE_PEARSON(id("reference_pearson_correlation"),"ref. Pearson correlation",""),
	LOCAL_PEARSON(id("local_pearson_correlation"), "local Pearson correlation",""),
	@Deprecated SEQUENCE_COHESION(id("sequence_cohesion"), "cohesion","Indicates the cohesion or the tightness of a sequence pattern."),
	@Deprecated SEQUENCE_INTERESTINGNESS(id("sequence_interestingness"), "interestingness","The harmonic mean between the frequency and the cohesion of a pattern."),
	@Deprecated ASSOCIATION_LEVERAGE(id("association_leverage"), "itemset leverage", "Tests if an itemset has a higher support than would be expected under any assumption of independence between subsets.")
	;

	private final Identifier id;
	
	private final String name;
	
	private final String description;

	private QualityMeasureId(Identifier id, String name, String description) {
		this.id=id;
		this.name = name;
		this.description = description;
	}

	@Override
	public String caption() {
		return name;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public Identifier identifier() {
		return id;
	}

}

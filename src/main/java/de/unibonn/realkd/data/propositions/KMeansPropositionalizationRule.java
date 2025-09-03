/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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
package de.unibonn.realkd.data.propositions;

import static de.unibonn.realkd.common.base.IntegerValues.evenPosIntValue;
import static de.unibonn.realkd.common.base.IntegerValues.posIntValue;
import static de.unibonn.realkd.data.constraints.Constraints.greaterOrEquals;
import static de.unibonn.realkd.data.constraints.Constraints.greaterThan;
import static de.unibonn.realkd.data.constraints.Constraints.lessOrEquals;
import static de.unibonn.realkd.data.constraints.Constraints.lessThan;
import static de.unibonn.realkd.data.propositions.Propositions.proposition;
import static de.unibonn.realkd.util.Lists.kMeansCutPoints;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.IntegerValues.EvenPositiveIntegerValue;
import de.unibonn.realkd.common.base.IntegerValues.PositiveIntegerValue;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;

/**
 * Maps metric attribute to a number of propositions corresponding to kmeans
 * cutoff points and their negations.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.2
 * 
 * @version 0.7.0
 *
 */
@KdonTypeName("kmeansPropRule")
@KdonDoc("Propositionalization rule for metric attributes that creates a set of propositions corresponding to a binning that is neither equal count nor equal width but instead approximately minimizes sum of squared error.")
public class KMeansPropositionalizationRule implements PropositionalizationRule {

	private static final EvenPositiveIntegerValue DEFAULT_NUMBER_OF_CUT_OFF_POINTS = evenPosIntValue(4);

	private static final PositiveIntegerValue DEFAULT_KMEANS_MAX_ITERATIONS = posIntValue(25);

	public static KMeansPropositionalizationRule kMeansPropRule() {
		return new KMeansPropositionalizationRule(DEFAULT_NUMBER_OF_CUT_OFF_POINTS, DEFAULT_KMEANS_MAX_ITERATIONS);
	}

	public static KMeansPropositionalizationRule kMeansPropRule(EvenPositiveIntegerValue numberOfCutOffs) {
		return new KMeansPropositionalizationRule(numberOfCutOffs, DEFAULT_KMEANS_MAX_ITERATIONS);
	}

	public static KMeansPropositionalizationRule kMeansPropRule(EvenPositiveIntegerValue numberOfCutOffs,
			PositiveIntegerValue maxIterations) {
		return new KMeansPropositionalizationRule(numberOfCutOffs, maxIterations);
	}

	private final EvenPositiveIntegerValue numberOfCutoffs;

	private final PositiveIntegerValue maxNumberOfIterations;

	@JsonCreator
	private KMeansPropositionalizationRule(@JsonProperty("numberOfCutoffs") EvenPositiveIntegerValue numberOfCutOffs,
			@JsonProperty("maxNumberOfIterations") PositiveIntegerValue maxNumberOfIterations) {
		this.numberOfCutoffs = numberOfCutOffs;
		this.maxNumberOfIterations = (maxNumberOfIterations == null) ? DEFAULT_KMEANS_MAX_ITERATIONS
				: maxNumberOfIterations;
	}

	@JsonProperty("numberOfCutoffs")
	@JsonInclude(Include.NON_EMPTY)
	@KdonDoc("Even number greater or equal to 2.")
	private EvenPositiveIntegerValue numberOfCutoffs() {
		return (numberOfCutoffs == DEFAULT_NUMBER_OF_CUT_OFF_POINTS) ? null : numberOfCutoffs;
	}

	@JsonProperty("maxNumberOfIterations")
	@JsonInclude(Include.NON_EMPTY)
	@KdonDoc("Maximal number of kmeans iterations to be performed (default 25)")
	private PositiveIntegerValue kMeansMaxIterations() {
		return (maxNumberOfIterations == DEFAULT_KMEANS_MAX_ITERATIONS) ? null : maxNumberOfIterations;
	}

	@Override
	public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
		List<AttributeBasedProposition<?>> result = new ArrayList<>();
		if (attribute instanceof MetricAttribute) {
			MetricAttribute metricAttribute = (MetricAttribute) attribute;
			List<Double> cutPoints = kMeansCutPoints(metricAttribute.nonMissingValuesInOrder(),
					numberOfCutoffs.asInt() + 1, maxNumberOfIterations.asInt());
			for (int i = 0; i < numberOfCutoffs.asInt() / 2; i++) {
				result.add(proposition(table, metricAttribute, lessThan(cutPoints.get(i))));
				result.add(proposition(table, metricAttribute, greaterOrEquals(cutPoints.get(i))));
			}
			for (int i = numberOfCutoffs.asInt() / 2; i < cutPoints.size(); i++) {
				result.add(proposition(table, metricAttribute, greaterThan(cutPoints.get(i))));
				result.add(proposition(table, metricAttribute, lessOrEquals(cutPoints.get(i))));
			}
		}
		return result;
	}

}
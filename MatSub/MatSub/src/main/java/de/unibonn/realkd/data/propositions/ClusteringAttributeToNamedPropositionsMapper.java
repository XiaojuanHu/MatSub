/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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

import static de.unibonn.realkd.data.constraints.Constraints.greaterOrEquals;
import static de.unibonn.realkd.data.constraints.Constraints.lessThan;
import static de.unibonn.realkd.data.propositions.Propositions.proposition;
import static de.unibonn.realkd.util.Lists.kMeansCutPoints;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.constraints.Constraints;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;

/**
 * <p>
 * Mappers that create propositions with intuitive names from metric attributes
 * corresponding to an <i>even</i> number of cut-off points on the empirical
 * range of that attribute. The cut-off points can be irregular (non-equal
 * width). In the current implementation (version 0.3.0) cut-off points are
 * found via k-means clustering with a limited number of iterations.
 * </p>
 * <p>
 * All of these mappers are elements of
 * {@link PropositionalContextFromTableBuilder#ALL_MAPPERS} and are thus valid
 * options for propositional logic construction via that builder.
 * <p>
 * 
 * @author Mario Boley
 * 
 * @author Sandy Moens
 * 
 * @since 0.3.0
 * 
 * @version 0.3.1
 *
 */
public enum ClusteringAttributeToNamedPropositionsMapper implements PropositionalizationRule {

	IRREGULAR_2_CUTOFFS_CLUSTERING(ImmutableList.of("low", "high")),

	IRREGULAR_4_CUTOFFS_CLUSTERING(ImmutableList.of("very low", "low", "high", "very high")),

	IRREGULAR_6_CUTOFFS_CLUSTERING(
			ImmutableList.of("extremely low", "very low", "low", "high", "very high", "extremely high")),

	IRREGULAR_8_CUTOFFS_CLUSTERING(ImmutableList.of("extremely low", "very low", "low", "reduced", "increased", "high",
			"very high", "extremely high")),

	IRREGULAR_10_CUTOFFS_CLUSTERING(ImmutableList.of("extremely low", "very low", "low", "reduced", "slightly reduced",
			"slightly increased", "increased", "high", "very high", "extremely high")),

	IRREGULAR_12_CUTOFFS_CLUSTERING(
			ImmutableList.of("extremely low", "exceptionally low", "very low", "low", "reduced", "slightly reduced",
					"slightly increased", "increased", "high", "very high", "exceptionally high", "extremely high")),

	IRREGULAR_14_CUTOFFS_CLUSTERING(ImmutableList.of("extremely low", "exceptionally low", "very low", "low",
			"notably reduced", "somewhat reduced", "slightly reduced", "slightly increased", "somewhat increased",
			"notably increased", "high", "very high", "exceptionally high", "extremely high")),

	IRREGULAR_16_CUTOFFS_CLUSTERING(ImmutableList.of("extremely low", "exceptionally low", "very low", "lower", "low",
			"notably reduced", "somewhat reduced", "slightly reduced", "slightly increased", "somewhat increased",
			"notably increased", "high", "higher", "very high", "exceptionally high", "extremely high")),

	IRREGULAR_18_CUTOFFS_CLUSTERING(ImmutableList.of("extremely low", "exceptionally low", "outstandingly low",
			"very low", "lower", "low", "notably reduced", "somewhat reduced", "slightly reduced", "slightly increased",
			"somewhat increased", "notably increased", "high", "higher", "very high", "outstandingly high",
			"exceptionally high", "extremely high"));

	private static final int KMEANS_ITERATIONS = 25;

	private final List<String> names;

	private ClusteringAttributeToNamedPropositionsMapper(List<String> names) {
		this.names = names;
	}

	@Override
	public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
		List<AttributeBasedProposition<?>> result=new ArrayList<>();
		if (attribute instanceof MetricAttribute) {
			MetricAttribute metricAttribute = (MetricAttribute) attribute;
			List<Double> cutPoints = kMeansCutPoints(metricAttribute.nonMissingValuesInOrder(), names.size() + 1,
					KMEANS_ITERATIONS);
			for (int i = 0; i < names.size() / 2; i++) {
				result.add(lessThanConstraint(table, metricAttribute, cutPoints.get(i), names.get(i)));
				result.add(notLessThanConstraint(table, metricAttribute, cutPoints.get(i), "not " + names.get(i)));
			}
			for (int i = names.size() / 2; i < cutPoints.size(); i++) {
				result.add(greaterThanConstraint(table, metricAttribute, cutPoints.get(i), names.get(i)));
				result.add(notGreaterThanConstraint(table, metricAttribute, cutPoints.get(i), "not " + names.get(i)));
			}
		}
		return result;
	}

	private AttributeBasedProposition<Double> lessThanConstraint(DataTable table, MetricAttribute metricAttribute,
			Double value, String name) {
		return proposition(table, metricAttribute, lessThan(value, name));
	}

	private AttributeBasedProposition<Double> notLessThanConstraint(DataTable table, MetricAttribute metricAttribute,
			Double value, String name) {
		return proposition(table, metricAttribute, greaterOrEquals(value, name));
	}

	private AttributeBasedProposition<Double> greaterThanConstraint(DataTable table, MetricAttribute metricAttribute,
			Double value, String name) {
		return proposition(table, metricAttribute, Constraints.greaterThan(value, name));
	}

	private AttributeBasedProposition<Double> notGreaterThanConstraint(DataTable table, MetricAttribute metricAttribute,
			Double value, String name) {
		return proposition(table, metricAttribute, Constraints.lessOrEquals(value,name));
	}

}

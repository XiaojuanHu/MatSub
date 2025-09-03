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

import static de.unibonn.realkd.common.base.IntegerValues.evenPosIntValue;
import static de.unibonn.realkd.common.base.IntegerValues.posIntValue;
import static de.unibonn.realkd.data.propositions.KMeansPropositionalizationRule.kMeansPropRule;

import java.util.List;

import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 * <p>
 * Mappers that create propositions from metric attributes corresponding to an
 * <i>even</i> number of cut-off points on the empirical range of that
 * attribute. The cut-off points can be irregular (non-equal width). In the
 * current implementation (version 0.7.0) cut-off points are found via k-means
 * clustering with a limited number of iterations.
 * </p>
 * <p>
 * All of these mappers are elements of
 * {@link PropositionalContextFromTableBuilder#ALL_MAPPERS} and are thus valid
 * options for propositional logic construction via that builder.
 * <p>
 * 
 * @author Mario Boley
 * 
 * @since 0.3.2
 * 
 * @version 0.7.0
 * 
 * @see ClusteringAttributeToNamedPropositionsMapper
 * @see KMeansPropositionalizationRule
 *
 */
public enum ClusteringAttributeToPropositionsMapper implements PropositionalizationRule {

	CLUSTERING_2_CUTOFFS(2),

	CLUSTERING_4_CUTOFFS(4),

	CLUSTERING_6_CUTOFFS(6),

	CLUSTERING_8_CUTOFFS(8),

	CLUSTERING_10_CUTOFFS(10),

	CLUSTERING_12_CUTOFFS(12),

	CLUSTERING_14_CUTOFFS(14),

	CLUSTERING_16_CUTOFFS(16),

	CLUSTERING_18_CUTOFFS(18),

	CLUSTERING_20_CUTOFFS(20);

	private static final int KMEANS_ITERATIONS = 25;

	private final KMeansPropositionalizationRule rule;

	private ClusteringAttributeToPropositionsMapper(int numberOfCutOffs) {
		this.rule = kMeansPropRule(evenPosIntValue(numberOfCutOffs), posIntValue(KMEANS_ITERATIONS));
	}

	@Override
	public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
		return rule.apply(table, attribute);
	}

}

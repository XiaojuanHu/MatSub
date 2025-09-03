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
import static de.unibonn.realkd.data.constraints.Constraints.lessOrEquals;
import static de.unibonn.realkd.data.propositions.AsymetricRegularQuantileBasedAttributeToPropositionMapper.APX_EQUAL_FREQUENCY_LOWER_BOUNDS_8_CUTOFFS;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;

/**
 * Mapper creates all inequality constraints (both directions) for all possible
 * thresholds values for ordinal attributes that are also categoric. For
 * non-categoric ordinal attributes it falls back to quantile based
 * inequalities. Metric attributes are skipped altogether.
 * 
 * @author Mario Boley
 * 
 * @since 0.4.0
 * 
 * @version 0.6.0
 *
 */
public enum SmartDiscreteOrdinalAttributeToPropositionMapper implements PropositionalizationRule {

	SMART_DISCRETE_ORDINAL;

	@Override
	public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
		if (attribute instanceof MetricAttribute || !(attribute instanceof OrdinalAttribute<?>)) {
			return ImmutableList.of();
		}
		if (attribute instanceof CategoricAttribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			OrdinalAttribute<T> ordinal = (OrdinalAttribute<T>) attribute;
			CategoricAttribute<T> discrete = (CategoricAttribute<T>) attribute;
			discrete.categories().forEach(c -> {
				result.add(Propositions.proposition(table, attribute, greaterOrEquals(c, ordinal.valueComparator())));
				result.add(Propositions.proposition(table, attribute, lessOrEquals(c, ordinal.valueComparator())));
			});
			return result;
		} else {
			return APX_EQUAL_FREQUENCY_LOWER_BOUNDS_8_CUTOFFS.apply(table, attribute);
		}
	}

}

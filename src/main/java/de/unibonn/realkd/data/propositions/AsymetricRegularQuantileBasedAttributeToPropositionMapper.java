/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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

import static de.unibonn.realkd.data.constraints.Constraints.lowerQuantileBound;
import static de.unibonn.realkd.data.constraints.Constraints.lowerQuantileBoundNegation;
import static de.unibonn.realkd.data.propositions.Propositions.proposition;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;

/**
 * @author Mario Boley
 * 
 * @since 0.5.2
 * 
 * @version 0.5.2
 *
 */
public enum AsymetricRegularQuantileBasedAttributeToPropositionMapper implements PropositionalizationRule {

	APX_EQUAL_FREQUENCY_LOWER_BOUNDS_8_CUTOFFS(8);

	private final int cuts;

	AsymetricRegularQuantileBasedAttributeToPropositionMapper(int cuts) {
		this.cuts = cuts;
	}

	@Override
	public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
		if (!(attribute instanceof OrdinalAttribute)) {
			return ImmutableList.of();
		}
		List<AttributeBasedProposition<?>> result = new ArrayList<>();
		OrdinalAttribute<T> ordinal = (OrdinalAttribute<T>) attribute;
		int k = cuts + 1;
		double q_prev = 0;
		for (int i = 1; i < k; i++) {
			double q = q_prev + (1.0 - q_prev) / (k - i + 1);
			T threshold = ordinal.quantile(q);
			final int orderNumber = ordinal.orderNumber(threshold);
			final int numberOfNonMissingValues = ordinal.numberOfNonMissingValues();
			q_prev = (double) orderNumber / numberOfNonMissingValues;
			if (q_prev >= 1) {
				break;
			}
			threshold = ordinal.nonMissingValuesInOrder().get(orderNumber);
			result.add(proposition(table, ordinal, lowerQuantileBound(ordinal, threshold)));
			result.add(proposition(table, ordinal, lowerQuantileBoundNegation(ordinal, threshold)));
		}
		return result;
	}

}

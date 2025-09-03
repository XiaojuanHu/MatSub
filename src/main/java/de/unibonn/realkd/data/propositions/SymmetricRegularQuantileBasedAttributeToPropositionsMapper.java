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

import static de.unibonn.realkd.data.propositions.Propositions.addLowerQuantileBoundBasedPropositions;
import static de.unibonn.realkd.data.propositions.Propositions.addUpperQuantileBoundBasedPropositions;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;

/**
 * Collection of attribute to proposition mappers that create an even number of
 * cut-off points corresponding to a symmetric grid of quantiles that
 * distributes the cut-off quantiles evenly in the upper and the lower half of
 * the quantile range. That is: for two cut-off values we have 0.25 and 0.75,
 * for four we have 0.2, 0.4, 0.6, and 0.8 and so on.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.4.0
 *
 */
public enum SymmetricRegularQuantileBasedAttributeToPropositionsMapper implements PropositionalizationRule {

	COUNTING_BASED_2_CUTOFFS(2), COUNTING_BASED_4_CUTOFFS(4), COUNTING_BASED_6_CUTOFFS(
			6), SYMMETRIC_EQUAL_FREQUENCY_8_CUTOFFS(8);

	private final int cuts;

	SymmetricRegularQuantileBasedAttributeToPropositionsMapper(int cuts) {
		this.cuts = cuts;
	}

	@Override
	public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
		if (!(attribute instanceof OrdinalAttribute)) {
			return ImmutableList.of();
		}
		List<AttributeBasedProposition<?>> result = new ArrayList<>();
		OrdinalAttribute<T> ordinal = (OrdinalAttribute<T>) attribute;
		int binsPerHalf = (cuts / 2) + 1;
		range(1, binsPerHalf).mapToDouble(i -> 0.5 + i * 0.5 / binsPerHalf)
				.forEach(c -> addLowerQuantileBoundBasedPropositions(table, result, ordinal, c));
		range(1, binsPerHalf).mapToDouble(i -> 0.5 - i * 0.5 / binsPerHalf)
				.forEach(c -> addUpperQuantileBoundBasedPropositions(table, result, ordinal, c));
		return result;
	}

}

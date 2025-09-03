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
package de.unibonn.realkd.patterns.models.bernoulli;

import static java.util.Objects.hash;
import static java.util.stream.IntStream.range;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.patterns.models.ModelFactory;

/**
 * Creates a model of the probability of a single discrete event over one data
 * attribute.
 * 
 * @author Mario Boley
 * 
 * @author Michael Hedderich
 * 
 * @since 0.4.0
 * 
 * @version 0.5.0
 *
 */
@KdonTypeName("asBernoulli")
@KdonDoc("Models categoric attribute as Bernoulli distribution.")
public class BernoulliDistributionFactory implements ModelFactory<BernoulliDistribution> {

	@JsonCreator
	public BernoulliDistributionFactory(@JsonProperty("positiveCategory") Object positiveCategory) {
		this.positiveCategory = positiveCategory;
	}

	@JsonProperty("positiveCategory")
	@KdonDoc("Attribute value that is considered positive event.")
	private final Object positiveCategory;

	@Override
	public Class<? extends BernoulliDistribution> modelClass() {
		return (Class<? extends BernoulliDistribution>) BernoulliDistribution.class;
	}

	private Optional<Integer> indexOfPositiveCategory(CategoricAttribute<?> attribute) {
		return range(0, attribute.categories().size())
				.filter(i -> attribute.categories().get(i).equals(positiveCategory)).mapToObj(i -> Integer.valueOf(i))
				.findFirst();
	}

	@Override
	public BernoulliDistribution getModel(DataTable dataTable, List<? extends Attribute<?>> attributes, IndexSet rows) {
		CategoricAttribute<?> attribute = (CategoricAttribute<?>) attributes.get(0);
		Double prob = indexOfPositiveCategory(attribute).map(i -> attribute.getCategoryFrequenciesOnRows(rows).get(i))
				.orElse(0.0);
		return new BernoulliDistribution(prob);
	}

	@Override
	public boolean isApplicable(List<? extends Attribute<?>> attributes) {
		return (attributes.size() == 1 && (attributes.get(0) instanceof CategoricAttribute));
	}

	@Override
	public String symbol() {
		return "event[" + positiveCategory + "]";
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BernoulliDistributionFactory)) {
			return false;
		}
		return this.positiveCategory.equals(((BernoulliDistributionFactory) other).positiveCategory);
	}

	@Override
	public int hashCode() {
		return hash(positiveCategory);
	}

}

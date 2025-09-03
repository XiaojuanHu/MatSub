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

package de.unibonn.realkd.patterns.outlier;

import static java.util.Objects.hash;

import java.util.Set;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.DefaultPattern;
import de.unibonn.realkd.patterns.ExplicitLocalPatternDescriptor;
import de.unibonn.realkd.patterns.Frequency;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.QualityMeasureId;

/**
 * Pattern indicating that a specific set of rows in the dataTable shows an
 * anormal behavior with respect to a specific set of attributes.
 * 
 * @author mboley
 * 
 */
public class Outlier extends DefaultPattern<ExplicitLocalPatternDescriptor> implements Pattern<ExplicitLocalPatternDescriptor> {

	private final IndexSet rows;
	private final Set<Attribute<?>> attributes;
	private final DataTable dataTable;

	public Outlier(DataTable dataTable, IndexSet rows,
			Set<Attribute<?>> attributes, double score, double frequency) {
		super(dataTable.population(), ExplicitLocalPatternDescriptor
				.createExplicitLocalPatternDescriptor(dataTable, attributes,
						rows), ImmutableList.of(
				Measures.measurement(QualityMeasureId.OUTLIER_SCORE, score),
				Measures.measurement(Frequency.FREQUENCY, frequency)));
		this.dataTable = dataTable;
		this.rows = rows;
		this.attributes = attributes;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Outlier) {
			Outlier otherOutlier = (Outlier) other;
			//TODO use equals method for attributes
			return (this.attributes.containsAll(otherOutlier.attributes)
					&& this.rows.equals(otherOutlier.rows)
					&& otherOutlier.attributes.containsAll(this.attributes) && otherOutlier.rows
						.equals(this.rows));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hash(this.attributes, this.rows);
	}

	@Override
	public ExplicitLocalPatternDescriptor descriptor() {
		return (ExplicitLocalPatternDescriptor) super.descriptor();
	}

	public DataTable getDatatable() {
		return this.dataTable;
	}

}

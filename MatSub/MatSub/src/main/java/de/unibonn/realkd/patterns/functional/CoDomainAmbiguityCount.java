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
package de.unibonn.realkd.patterns.functional;

import static de.unibonn.realkd.common.base.Identifier.id;
import static java.util.stream.IntStream.range;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;

/**
 * @author Mario Boley
 * 
 * @since 0.5.1
 * 
 * @version 0.5.1
 *
 */
public enum CoDomainAmbiguityCount implements Measure, MeasurementProcedure<CoDomainAmbiguityCount, Object>, Identifiable {

	CODOMAIN_AMBIGUITY_COUNT;

	@Override
	public String caption() {
		return "Ambiguity count";
	}

	@Override
	public String description() {
		return "The number of data record pairs with distinct co-domain value that have identical domain value";
	}

	@Override
	public boolean isApplicable(Object object) {
		return object instanceof BinaryAttributeSetRelation;
	}

	@Override
	public Measurement perform(Object object) {
		if (!isApplicable(object)) {
			return Measures.measurement(this, Double.NaN);
		}
		BinaryAttributeSetRelation descriptor = (BinaryAttributeSetRelation) object;
		int[] xIndices = range(0, descriptor.domain().size()).toArray();
		ContingencyTable jointDistribution = descriptor.contingencyTable();
		int uncoveredEdgeCount = jointDistribution.ambiguityCount(xIndices);
		return Measures.measurement(this, uncoveredEdgeCount);
	}

	@Override
	public CoDomainAmbiguityCount getMeasure() {
		return this;
	}

	@Override
	public Identifier identifier() {
		return id("codomain_ambiguity_count");
	}

}

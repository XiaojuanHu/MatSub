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
import static de.unibonn.realkd.patterns.functional.CoDomainEntropy.CODOMAIN_ENTROPY;
import static java.lang.Double.NaN;

import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;
import de.unibonn.realkd.patterns.models.table.ContingencyTables;

/**
 * @author Mario Boley
 * 
 * @author Panagiotis Mandros
 * 
 * @since 0.5.0
 *
 */
public enum FractionOfInformation
		implements FunctionalDependencyMeasure, MeasurementProcedure<FractionOfInformation, Object> {

	FRACTION_OF_INFORMATION;

	@Override
	public String caption() {
		return "fraction of information";
	}

	@Override
	public String symbol() {
		return "F(X;Y)";
	}

	@Override
	public String description() {
		return "The relative entropy reduction of the co-domain given the domain.";
	}

	@Override
	public boolean isApplicable(Object object) {
		return object instanceof BinaryAttributeSetRelation;
	}

	@Override
	public FractionOfInformation getMeasure() {
		return FractionOfInformation.FRACTION_OF_INFORMATION;
	}

	@Override
	public Measurement perform(Object object) {
		if (!isApplicable(object)) {
			return Measures.measurement(this, NaN);
		}
		BinaryAttributeSetRelation descriptor = (BinaryAttributeSetRelation) object;
		if (descriptor.domain().isEmpty()) {
			double entropyOfY = descriptor.contingencyTable().entropy();
			return Measures.measurement(getMeasure(), 1,
					ImmutableList.of(Measures.measurement(CODOMAIN_ENTROPY, entropyOfY)));
		}
		int[] domainAttributeIndexes = IntStream.range(0, descriptor.domain().size()).toArray();
		ContingencyTable firstCTable = descriptor.contingencyTable().marginal(domainAttributeIndexes);
		ContingencyTable secondCTable = descriptor.contingencyTable().marginal(domainAttributeIndexes.length);
		double mutualInformation = ContingencyTables.mutualInformation(descriptor.contingencyTable(), firstCTable,
				secondCTable);
		double entropyOfY = secondCTable.entropy();
		double result = mutualInformation / entropyOfY;

		return Measures.measurement(getMeasure(), result,
				ImmutableList.of(Measures.measurement(CODOMAIN_ENTROPY, entropyOfY)));
	}

	@Override
	public Identifier identifier() {
		return id("fraction_of_information");
	}

}

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
package de.unibonn.realkd.patterns.models.table;

import static de.unibonn.realkd.common.base.Lazy.lazy;
import static de.unibonn.realkd.patterns.models.table.MutualInformation.MUTUAL_INFORMATION;
import static de.unibonn.realkd.patterns.models.table.ShannonEntropy.ENTROPY;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Lazy;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.util.InformationTheory;

/**
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public class TwoDimensionalContingencyTableImplementation extends ContingencyTableImplementation
		implements TwoDimensionalContingencyTable {

	private final Lazy<Double> mutualInformation;

	private final Lazy<? extends List<Measurement>> measurements;

	public TwoDimensionalContingencyTableImplementation(Dimension d1, Dimension d2, CountTable table,
			List<ContingencyTableParameter> parameters) {
		super(ImmutableList.of(d1, d2), table, parameters);
		this.mutualInformation = lazy(() -> computeMutualInformation());
		this.measurements = lazy(() -> ImmutableList.of(Measures.measurement(ENTROPY, entropy()),
				Measures.measurement(MUTUAL_INFORMATION, mutualInformation())));
	}

	public List<Measurement> measurements() {
		return measurements.get();
	}

	private double computeMutualInformation() {
		return InformationTheory.mutualInformation(marginal(0).entropy(), marginal(1).entropy(),entropy());
	}

	@Override
	public double mutualInformation() {
		return mutualInformation.get();
	}
	
	@Override
	public double expectedMutualInformationUnderPermutationModel() {
		return ContingencyTables.expectedMutualInformationUnderPermutationModel(marginal(0), marginal(1));
	}



}

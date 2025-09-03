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

package de.unibonn.realkd.patterns.models.gaussian;

import java.util.List;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.models.ModelFactory;

public enum GaussianModelFactory implements ModelFactory<UnivariateGaussian> {

	INSTANCE;

	private static final String STRING_NAME = "Gaussian distribution";

	private GaussianModelFactory() {
		;
	}

	@Override
	public Class<? extends UnivariateGaussian> modelClass() {
		return UnivariateGaussian.class;
	}

	@Override
	public UnivariateGaussian getModel(DataTable dataTable, List<? extends Attribute<?>> attributes) {
		return Gaussians.gaussian(dataTable, (MetricAttribute) attributes.get(0));
	}

	@Override
	public UnivariateGaussian getModel(DataTable dataTable, List<? extends Attribute<?>> attributes, IndexSet rows) {
		return Gaussians.gaussian(dataTable, attributes.get(0), rows);
	}

	@Override
	public boolean isApplicable(List<? extends Attribute<?>> attributes) {
		return !(attributes.size() != 1 || !(attributes.get(0) instanceof MetricAttribute));
	}

	@Override
	public String toString() {
		return STRING_NAME;
	}

	@Override
	public String symbol() {
		return "Gaussian";
	}

}

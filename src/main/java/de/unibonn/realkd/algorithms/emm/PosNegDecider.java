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

package de.unibonn.realkd.algorithms.emm;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.util.List;
import java.util.function.IntPredicate;

import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.DefaultCategoricAttribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;

public interface PosNegDecider extends IntPredicate {

	public static class SingleAttributePosNegDecider implements PosNegDecider {

		private Attribute<?> attribute;
		private double thresholdValue;
		private String positiveValue;

		public SingleAttributePosNegDecider(Attribute<?> attribute) {
			this.attribute = attribute;
			if (attribute instanceof MetricAttribute) {
				thresholdValue = ((MetricAttribute) attribute).mean();
			} else {
				positiveValue = ((DefaultCategoricAttribute) attribute)
						.categories().get(0);
			}
		}

		public boolean test(int rowIx) {
			if (attribute.valueMissing(rowIx)) {
				return false;
			}
			if (attribute instanceof MetricAttribute) {
				return ((MetricAttribute) attribute).value(rowIx) >= thresholdValue;
//				return ((MetricAttribute) attribute).getValue(rowIx) < thresholdValue;
			}
			return attribute.value(rowIx).equals(positiveValue);
		}
	}

	public static class MultipleAttributesPosNegDecider implements PosNegDecider {

		List<PosNegDecider> deciders;

		public MultipleAttributesPosNegDecider(
				List<Attribute<?>> attributes) {
			deciders = newArrayListWithCapacity(attributes.size());
			for (Attribute<?> attribute : attributes) {
				deciders.add(new SingleAttributePosNegDecider(attribute));
			}
		}

		public boolean test(int rowIx) {
			for (PosNegDecider decider : deciders) {
				if (!decider.test(rowIx)) {
					return false;
				}
			}
			return true;
		}
	}
	
	public static class PCAPosNegDecider implements PosNegDecider {

		PCAEvaluator pcaEvaluator;

		public PCAPosNegDecider(DataTable dataTable, List<Attribute<?>> attributes) {
			pcaEvaluator = PCAEvaluators.newPCAEvaluator(dataTable, attributes);
		}

		@Override
		public boolean test(int rowIx) {
			return pcaEvaluator.getDevFirstDimension(rowIx) >= 0;
		}
	}

	public boolean test(int rowIx);
}
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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.IntStream;

import com.mkobos.pca_transform.PCA;
import com.mkobos.pca_transform.PCA.TransformationType;

import Jama.Matrix;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;

public class PCAEvaluators {
	
	public static PCAEvaluator newPCAEvaluator(DataTable dataTable, List<? extends Attribute<?>> targetAttributes) {
		return new DefaultPCAEvaluator(dataTable, targetAttributes);
	}
	
	private static class DefaultPCAEvaluator implements PCAEvaluator {
		
		private Matrix transformation;

		public DefaultPCAEvaluator(DataTable dataTable, List<? extends Attribute<?>> targetAttributes) {
			List<Attribute<?>> metricAttributes = targetAttributes.stream().filter(a -> a instanceof MetricAttribute).collect(toList());
			
			Matrix matrix = new Matrix(dataTable.population().size(), metricAttributes.size());
			
			IntStream.range(0, metricAttributes.size()).forEach(i ->  {
				MetricAttribute metricAttribute = (MetricAttribute) metricAttributes.get(i);
				
				IntStream.range(0, dataTable.population().size()).forEach(j-> {
					if(metricAttribute.valueMissing(j)) {
						matrix.set(j, i, metricAttribute.median());
					} else {
						matrix.set(j, i, metricAttribute.value(j));
					}
				});
			});
			
			
			PCA pca = new PCA(matrix, true);
			transformation = pca.transform(matrix, TransformationType.ROTATION);
		}

		@Override
		public double getDevForDimension(int index, int dimension) {
			if (dimension >= transformation.getColumnDimension()) {
				return 0;
			}
			return transformation.get(index, dimension);
		}
		
	}
	
}

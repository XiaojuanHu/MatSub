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

package de.unibonn.realkd.patterns.models;

import java.util.List;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.JsonSerializable;
import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 * Implementing classes create local models describing some data attributes on
 * some or all data rows.
 * 
 * @author Mario Boley
 * 
 * @since 0.0.1
 * 
 * @version 0.0.1
 * 
 */
@KdonTypeName("modellingMethod")
@KdonDoc("Method of fitting a model to a set of attributes.")
public interface ModelFactory<T extends Model> extends JsonSerializable {

	public Class<? extends T> modelClass();

	public default T getModel(DataTable dataTable, List<? extends Attribute<?>> attributes) {
		return getModel(dataTable, attributes, dataTable.population().objectIds());
	}

	public T getModel(DataTable dataTable, List<? extends Attribute<?>> attributes, IndexSet rows);

	/**
	 * Returns whether the model can be inferred for a selection of attributes
	 */
	public boolean isApplicable(List<? extends Attribute<?>> attributes);

	/**
	 * @return human-readable abbreviation of model factory to be used whenever
	 *         space is scarce. Examples: ctable, Gaussian, LSF, Theil-Sen etc.
	 */
	public String symbol();

}

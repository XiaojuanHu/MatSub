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

package de.unibonn.realkd.patterns.models.table;

import static de.unibonn.realkd.patterns.models.table.ContingencyTables.contingencyTable;

import java.util.List;
import java.util.logging.Logger;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;
import de.unibonn.realkd.patterns.models.ModelFactory;

public enum ContingencyTableModelFactory implements ModelFactory<ContingencyTable> {
	
	INSTANCE;
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER=Logger.getLogger(ContingencyTableModelFactory.class.getName());

	private ContingencyTableModelFactory() {
		;
	}

	@Override
	public Class<? extends ContingencyTable> modelClass() {
		return ContingencyTable.class;
	}

	@Override
	public ContingencyTable getModel(DataTable dataTable, List<? extends Attribute<?>> attributes, IndexSet rows) {
		return contingencyTable(dataTable,attributes,rows);
	}

	@Override
	public boolean isApplicable(List<? extends Attribute<?>> targets) {
		for (Attribute<?> target : targets) {
			if (!((target instanceof OrdinalAttribute) || (target instanceof CategoricAttribute))) {
				return false;
			}
		}

		return targets.size() > 0;
	}

	@Override
	public String toString() {
		return "Contingency table";
	}

	@Override
	public String symbol() {
		return "ctable";
	}

}

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
package de.unibonn.realkd.models;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.testing.AbstractJsonSerializationTest;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.models.ModelFactory;
import de.unibonn.realkd.patterns.models.regression.LinearRegressionModel;

/**
 * @author Mario Boley
 * 
 * @since 0.4.0
 * 
 * @version 0.6.0
 *
 */
public class RegressionModelSerializationTest extends AbstractJsonSerializationTest<LinearRegressionModel> {

	private static final ImmutableList<Class<? super LinearRegressionModel>> CLASSES = ImmutableList
			.of(LinearRegressionModel.class, SerialForm.class);

	public static enum DummyRegressionModelFactory implements ModelFactory<LinearRegressionModel> {

		INSTANCE;

		@Override
		public Class<? extends LinearRegressionModel> modelClass() {
			return LinearRegressionModel.class;
		}

		@Override
		public LinearRegressionModel getModel(DataTable dataTable, List<? extends Attribute<?>> attributes,
				IndexSet rows) {
			return new LinearRegressionModel(15.0, 2.0);
		}

		@Override
		public boolean isApplicable(List<? extends Attribute<?>> attributes) {
			return true;
		}

		@Override
		public String symbol() {
			return "dummy";
		}

	}

	public RegressionModelSerializationTest() {
		super(new LinearRegressionModel(15.0, 2.0), CLASSES);
	}

}

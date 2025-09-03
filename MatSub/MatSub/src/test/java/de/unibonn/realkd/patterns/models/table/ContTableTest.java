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

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.data.Populations.population;
import static de.unibonn.realkd.data.table.DataTables.table;
import static de.unibonn.realkd.data.table.attribute.Attributes.categoricalAttribute;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.Attributes;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;

/**
 * @author Panagiotis Mandros
 * 
 * @version 0.5
 *
 */
public class ContTableTest {
	private static final double DOUBLE_COMPARISON_PRECISION2 = 0.005;

	private Population population1 = population(id("test_population"), 4);

	private CategoricAttribute<String> a = categoricalAttribute("A", "",
			ImmutableList.of("a", "a", "b", "b"));

	private CategoricAttribute<String> b = categoricalAttribute("B", "",
			ImmutableList.of("a", "b", "a", "b"));

	private CategoricAttribute<String> c = categoricalAttribute("C", "",
			ImmutableList.of("a", "b", "a", "b"));

	private CategoricAttribute<String> d = categoricalAttribute("D", "",
			ImmutableList.of("a", "b", "c", "d"));

	private CategoricAttribute<String> e = categoricalAttribute("E", "",
			ImmutableList.of("a", "a", "b", "a"));

	private CategoricAttribute<String> f = categoricalAttribute("F", "",
			ImmutableList.of("a", "a", "c", "c"));

	private List<Attribute<?>> attrList1 = ImmutableList.of(a, b, c, d, e, f);

	private DataTable table1 = table(id("table"), "table", "", population1, attrList1);
	
	private TwoDimensionalContingencyTable ctable1 = ContingencyTables.contingencyTable(table1, a, b);
	
	private TwoDimensionalContingencyTable ctable2 = ContingencyTables.contingencyTable(table1, a, e);


	@Test
	public void expMIFirstTest() {

		double result = ctable1.expectedMutualInformationUnderPermutationModel();
		double trueResult = 0.333;

		assertEquals("Should be 0.3333.", trueResult, result, DOUBLE_COMPARISON_PRECISION2);
	}
	
	@Test
	public void expMISecondTest() {

		double result = ctable2.expectedMutualInformationUnderPermutationModel();
		double trueResult = 0.31127;

		assertEquals("Should be 0.311.", trueResult, result, DOUBLE_COMPARISON_PRECISION2);
	}
	
	

}

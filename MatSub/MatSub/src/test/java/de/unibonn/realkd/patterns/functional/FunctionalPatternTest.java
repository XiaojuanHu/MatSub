/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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
import static de.unibonn.realkd.data.Populations.population;
import static de.unibonn.realkd.data.table.DataTables.table;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attributes;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;

/**
 * 
 * 
 * @author Mario Boley
 * @author Panagiotis Mandros
 * 
 * @since 0.3.1
 * @version 0.3.1
 *
 */
public class FunctionalPatternTest {

	private static final double DOUBLE_COMPARISON_PRECISION = 0.00001;

	private Population population = population(id("test_population"), 4);

	private CategoricAttribute<String> a = Attributes.categoricalAttribute("A", "",
			ImmutableList.of("a", "a", "b", "b"));

	private CategoricAttribute<String> b = Attributes.categoricalAttribute("B", "",
			ImmutableList.of("a", "b", "a", "b"));

	private CategoricAttribute<String> c = Attributes.categoricalAttribute("C", "",
			ImmutableList.of("a", "b", "a", "b"));

	private CategoricAttribute<String> d = Attributes.categoricalAttribute("D", "",
			ImmutableList.of("a", "b", "c", "d"));

	private CategoricAttribute<String> e = Attributes.categoricalAttribute("E", "",
			ImmutableList.of("a", "a", "b", "a"));

	private CategoricAttribute<String> f = Attributes.categoricalAttribute("F", "",
			ImmutableList.of("a", "a", "c", "c"));

	private DataTable table = table(id("table"), "table", "", population, ImmutableList.of(a, b, c, d, e, f));

	private Population anotherPopulation = population(id("anotherTest_population"), 6);

	private CategoricAttribute<String> A = Attributes.categoricalAttribute("A", "",
			ImmutableList.of("1", "1", "1", "1", "2", "2"));

	private CategoricAttribute<String> B = Attributes.categoricalAttribute("B", "",
			ImmutableList.of("1", "1", "2", "2", "2", "2"));

	private CategoricAttribute<String> C = Attributes.categoricalAttribute("C", "",
			ImmutableList.of("1", "2", "1", "2", "1", "2"));

	private CategoricAttribute<String> D = Attributes.categoricalAttribute("D", "",
			ImmutableList.of("2", "1", "2", "1", "2", "1"));

	private CategoricAttribute<String> E = Attributes.categoricalAttribute("E", "",
			ImmutableList.of("1", "1", "2", "2", "3", "3"));

	private DataTable anotherTable = table(id("table"), "table", "", anotherPopulation, ImmutableList.of(A, B, C, D, E));

	@Test
	public void constructionOfAttributeRelationTest() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(table, ImmutableSet.of(a),
				ImmutableSet.of(b));
		assertNotNull(relation);
		assertEquals("Domain must be {a}.", relation.domain(), ImmutableSet.of(a));
		assertEquals("Co-domain must be {b}.", relation.coDomain(), ImmutableSet.of(b));
	}

	@Test
	public void constructionOfFunctionalPatternConstructionTest() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(table, ImmutableSet.of(a),
				ImmutableSet.of(b));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation);
		assertNotNull(pattern);
		assertEquals(pattern.descriptor(), relation);
		assertTrue(pattern.hasMeasure(FractionOfInformation.FRACTION_OF_INFORMATION));
	}

	@Test
	public void fractionOfInformationIndependentCaseTest() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(table, ImmutableSet.of(a),
				ImmutableSet.of(b));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation,
				FractionOfInformation.FRACTION_OF_INFORMATION);
		assertEquals("Independent variables must have 0 fraction of information of each other.", 0.0,
				pattern.value(FractionOfInformation.FRACTION_OF_INFORMATION), DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void fractionOfInformationFullyDependentCaseTest() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(table, ImmutableSet.of(b),
				ImmutableSet.of(c));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation,
				FractionOfInformation.FRACTION_OF_INFORMATION);
		assertEquals("Dependent variables must have 1 fraction of information of each other.", 1.0,
				pattern.value(FractionOfInformation.FRACTION_OF_INFORMATION), DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void fractionOfInformationZeroPointThreeEightCaseTest() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(table, ImmutableSet.of(f),
				ImmutableSet.of(e));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation,
				FractionOfInformation.FRACTION_OF_INFORMATION);
		assertEquals("Independent variables must have 0.38 fraction of information of each other.", 0.3836885465,
				pattern.value(FractionOfInformation.FRACTION_OF_INFORMATION), DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void fractionOfInformationFullyDependentCaseMultidimensionalTest() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(table,
				ImmutableSet.of(b, d), ImmutableSet.of(c));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation,
				FractionOfInformation.FRACTION_OF_INFORMATION);
		assertEquals("Dependent variables must have 1 fraction of information of each other.", 1.0,
				pattern.value(FractionOfInformation.FRACTION_OF_INFORMATION), DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void fractionOfInformationIndependentCaseMultidimensionalTest() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(table,
				ImmutableSet.of(b, d), ImmutableSet.of(c));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation,
				FractionOfInformation.FRACTION_OF_INFORMATION);
		assertEquals("Dependent variables must have 1 fraction of information of each other.", 1.0,
				pattern.value(FractionOfInformation.FRACTION_OF_INFORMATION), DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void fractionOfInformationFullyDependentCaseMultidimensionalTest2() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(anotherTable,
				ImmutableSet.of(A, B), ImmutableSet.of(E));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation,
				FractionOfInformation.FRACTION_OF_INFORMATION);
		assertEquals("Dependent variables must have 1 fraction of information of each other.", 1.0,
				pattern.value(FractionOfInformation.FRACTION_OF_INFORMATION), DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void fractionOfInformationIndependentCaseMultidimensionalTest2() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(anotherTable,
				ImmutableSet.of(C, D), ImmutableSet.of(E));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation,
				FractionOfInformation.FRACTION_OF_INFORMATION);
		assertEquals("Dependent variables must have 0.0 fraction of information of each other.", 0.0,
				pattern.value(FractionOfInformation.FRACTION_OF_INFORMATION), DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void fractionOfInformationForDim2() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(anotherTable,
				ImmutableSet.of(C), ImmutableSet.of(E));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation,
				FractionOfInformation.FRACTION_OF_INFORMATION);
		assertEquals("Dependent variables must have 0 fraction of information of each other.", 0,
				pattern.value(FractionOfInformation.FRACTION_OF_INFORMATION), DOUBLE_COMPARISON_PRECISION);
	}

	//////////////////

	@Test
	public void reliableFractionOfInformationIndependentCaseTest() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(table, ImmutableSet.of(a),
				ImmutableSet.of(b));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation,
				ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION);
		assertEquals("Independent variables must have 0 fraction of information of each other.", 0.0,
				pattern.value(ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION),
				DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void reliableFractionOfInformationTest1() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(table, ImmutableSet.of(b),
				ImmutableSet.of(c));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation,
				ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION);
		assertEquals("Dependent variables must have 0.66 reliable fraction of information of each other.", 0.6666666,
				pattern.value(ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION),
				DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void reliableFractionOfInformationTest2() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(table, ImmutableSet.of(f),
				ImmutableSet.of(e));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation,
				ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION);
		assertEquals("Independent variables must have 0 reliable fraction of information of each other.", 0.0,
				pattern.value(ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION),
				DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void reliableFractionOfInformationMultidimensionalTest() {
		BinaryAttributeSetRelation relation = FunctionalPatterns.binaryAttributeSetRelation(table,
				ImmutableSet.of(b, d), ImmutableSet.of(c));
		FunctionalPattern pattern = FunctionalPatterns.functionalPattern(relation,
				ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION);
		assertEquals("Dependent variables must have 0 reliable fraction of information of each other.", 0,
				pattern.value(ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION),
				DOUBLE_COMPARISON_PRECISION);
	}
	//
	// @Test
	// public void
	// reliableFractionOfInformationIndependentCaseMultidimensionalTest() {
	// CorrelationDescriptor relation =
	// FunctionalPatterns.correlationDescriptor(table, ImmutableSet.of(b, d),
	// ImmutableSet.of(c));
	// FunctionalPattern pattern =
	// FunctionalPatterns.functionalPattern(relation,
	// ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION);
	// assertEquals("Dependent variables must have 1 fraction of information of
	// each other.", 1.0,
	// pattern.value(ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION),
	// DOUBLE_COMPARISON_PRECISION);
	// }
	//
	// @Test
	// public void
	// reliableFractionOfInformationFullyDependentCaseMultidimensionalTest2() {
	// CorrelationDescriptor relation =
	// FunctionalPatterns.correlationDescriptor(anotherTable, ImmutableSet.of(A,
	// B),
	// ImmutableSet.of(E));
	// FunctionalPattern pattern =
	// FunctionalPatterns.functionalPattern(relation,
	// ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION);
	// assertEquals("Dependent variables must have 1 fraction of information of
	// each other.", 1.0,
	// pattern.value(ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION),
	// DOUBLE_COMPARISON_PRECISION);
	// }
	//
	// @Test
	// public void
	// reliableFractionOfInformationIndependentCaseMultidimensionalTest2() {
	// CorrelationDescriptor relation =
	// FunctionalPatterns.correlationDescriptor(anotherTable, ImmutableSet.of(C,
	// D),
	// ImmutableSet.of(E));
	// FunctionalPattern pattern =
	// FunctionalPatterns.functionalPattern(relation,
	// ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION);
	// assertEquals("Dependent variables must have 0.0 fraction of information
	// of each other.", 0.0,
	// pattern.value(ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION),
	// DOUBLE_COMPARISON_PRECISION);
	// }
	//
	// @Test
	// public void reliableFractionOfInformationForDim2() {
	// CorrelationDescriptor relation =
	// FunctionalPatterns.correlationDescriptor(anotherTable,
	// ImmutableSet.of(C),
	// ImmutableSet.of(E));
	// FunctionalPattern pattern =
	// FunctionalPatterns.functionalPattern(relation,
	// ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION);
	// assertEquals("Dependent variables must have 0 fraction of information of
	// each other.", 0,
	// pattern.value(ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION),
	// DOUBLE_COMPARISON_PRECISION);
	// }

}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.correlated;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.data.Populations.population;
import static de.unibonn.realkd.data.table.DataTables.table;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attributes;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;

/**
 * @author Panagiotis Mandros
 *
 */
public class NormalizedTotalCorrelationTest {

	private static final double DOUBLE_COMPARISON_PRECISION = 0.001;

	private Population population = population(id("test_population"), 4);

	private CategoricAttribute<String> a = Attributes.categoricalAttribute("A", "",
			ImmutableList.of("a", "a", "b", "b"));

	private CategoricAttribute<String> b = Attributes.categoricalAttribute("B", "",
			ImmutableList.of("a", "b", "a", "b"));

	private CategoricAttribute<String> c = Attributes.categoricalAttribute("C", "",
			ImmutableList.of("a", "a", "b", "b"));

	private DataTable table = table(id("table"), "table", "", population, ImmutableList.of(a, b, c));

	@Test
	public void fullDependenceTest() {
		AttributeSetRelation relation = CorrelationPatterns.attributeSetRelation(table, ImmutableSet.of(a, c));
		CorrelationPattern pattern = CorrelationPatterns.correlationPattern(relation,
				NormalizedTotalCorrelation.NORMALIZED_TOTAL_CORRELATION);
		assertEquals("Copy variables must have 1 normalized total correlation.", 1,
				pattern.value(NormalizedTotalCorrelation.NORMALIZED_TOTAL_CORRELATION), DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void independenceTest() {
		AttributeSetRelation relation = CorrelationPatterns.attributeSetRelation(table, ImmutableSet.of(a, b));
		CorrelationPattern pattern = CorrelationPatterns.correlationPattern(relation,
				NormalizedTotalCorrelation.NORMALIZED_TOTAL_CORRELATION);
		assertEquals("Two completely independent variables must have 0 normalized total correlation.", 0,
				pattern.value(NormalizedTotalCorrelation.NORMALIZED_TOTAL_CORRELATION), DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void oneVariableTest() {
		AttributeSetRelation relation = CorrelationPatterns.attributeSetRelation(table, ImmutableSet.of(a));
		CorrelationPattern pattern = CorrelationPatterns.correlationPattern(relation,
				NormalizedTotalCorrelation.NORMALIZED_TOTAL_CORRELATION);
		assertEquals("A single variable should have 0 normalized total correlation.", 0,
				pattern.value(NormalizedTotalCorrelation.NORMALIZED_TOTAL_CORRELATION), DOUBLE_COMPARISON_PRECISION);
		assertEquals("A single variable should have its entropy as sum of entorpies.", 1,
				pattern.value(SumOfEntropies.SUM_OF_ENTROPIES), DOUBLE_COMPARISON_PRECISION);
		assertEquals("A single variable should have 0 as sum of mutual informations.", 0,
				pattern.value(SumOfMutualInformations.SUM_OF_MUTUAL_INFORMATIONS), DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void threeVariablesTest() {
		AttributeSetRelation relation = CorrelationPatterns.attributeSetRelation(table, ImmutableSet.of(a, b, c));
		CorrelationPattern pattern = CorrelationPatterns.correlationPattern(relation,
				NormalizedTotalCorrelation.NORMALIZED_TOTAL_CORRELATION);
		assertEquals("The three variables should have 0.5 normalized total correlation.", 0.5,
				pattern.value(NormalizedTotalCorrelation.NORMALIZED_TOTAL_CORRELATION), DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void sumOfEntropiesTest() {
		AttributeSetRelation relation = CorrelationPatterns.attributeSetRelation(table, ImmutableSet.of(a, b, c));
		CorrelationPattern pattern = CorrelationPatterns.correlationPattern(relation);
		assertEquals("The sum of entropies should be 3.", 3, pattern.value(SumOfEntropies.SUM_OF_ENTROPIES),
				DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void fullDependenceCorrectedTest() {
		AttributeSetRelation relation = CorrelationPatterns.attributeSetRelation(table, ImmutableSet.of(a, c));
		CorrelationPattern pattern = CorrelationPatterns.correlationPattern(relation);
		assertEquals(
				"Fully dependent variables with 2 categories and 4 samples should have -0.4150 corrected normalized total correlation.",
				-0.4150, pattern.value(ReliableNormalizedTotalCorrelation.RELIABLE_NORMALIZED_TOTAL_CORRELATION),
				DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void independenceCorrectedTest() {
		AttributeSetRelation relation = CorrelationPatterns.attributeSetRelation(table, ImmutableSet.of(a, b));
		CorrelationPattern pattern = CorrelationPatterns.correlationPattern(relation);
		assertEquals("Two completely independent variables must have -1.4150 corrected normalized total correlation.",
				-1.4150, pattern.value(ReliableNormalizedTotalCorrelation.RELIABLE_NORMALIZED_TOTAL_CORRELATION),
				DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void threeVariablesCorrectedTest() {
		AttributeSetRelation relation = CorrelationPatterns.attributeSetRelation(table, ImmutableSet.of(a, b, c));
		CorrelationPattern pattern = CorrelationPatterns.correlationPattern(relation);
		assertEquals("The three variables should have -1.2075 normalized total correlation.", -1.2075,
				pattern.value(ReliableNormalizedTotalCorrelation.RELIABLE_NORMALIZED_TOTAL_CORRELATION),
				DOUBLE_COMPARISON_PRECISION);
	}

	@Test
	public void oneVariableCCorrectedTest() {
		AttributeSetRelation relation = CorrelationPatterns.attributeSetRelation(table, ImmutableSet.of(a));
		CorrelationPattern pattern = CorrelationPatterns.correlationPattern(relation);
		assertEquals("A single variable should have 0 normalized total correlation.", 0,
				pattern.value(NormalizedTotalCorrelation.NORMALIZED_TOTAL_CORRELATION), DOUBLE_COMPARISON_PRECISION);
		assertEquals("A single variable should have its entropy as sum of entorpies.", 1,
				pattern.value(SumOfEntropies.SUM_OF_ENTROPIES), DOUBLE_COMPARISON_PRECISION);
		assertEquals("A single variable should have 0 as sum of mutual informations.", 0,
				pattern.value(SumOfMutualInformations.SUM_OF_MUTUAL_INFORMATIONS), DOUBLE_COMPARISON_PRECISION);
		assertEquals("A single variable should have 0 corrected normalized total correlation.", 0,
				pattern.value(pattern.correlationMeasure()), DOUBLE_COMPARISON_PRECISION);
	}

}

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
package de.unibonn.realkd.patterns.association;

import static de.unibonn.realkd.patterns.logical.ItemsetLeverage.ITEMSET_LEVERAGE;

import org.junit.Test;

/**
 * @author Sandy Moens
 *
 * @since 0.3.0
 * 
 * @version 0.7.1
 *
 */
public class DefaultItemsetLeverageMeasurementProcedureTest extends AssociationMeasurementProcedureTest {
	
	public DefaultItemsetLeverageMeasurementProcedureTest() {
		super(ITEMSET_LEVERAGE);
	}
	
	@Test
	public void testSingleProposition() {
		test(0.0, getList("1"));
		test(0.0, getList("2"));
		test(0.0, getList("3"));
		test(0.0, getList("4"));
		test(0.0, getList("5"));
		test(0.0, getList("6"));
		test(0.0, getList("7"));
	}
	
	@Test
	public void testDoublePropositions() {
		test(4.0/7.0 - (5.0/7.0 * 5.0/7.0), getList("1", "2"));
		test(3.0/7.0 - (5.0/7.0 * 4.0/7.0), getList("2", "3"));
		test(3.0/7.0 - (4.0/7.0 * 4.0/7.0), getList("3", "4"));
		test(3.0/7.0 - (4.0/7.0 * 6.0/7.0), getList("4", "5"));
		test(3.0/7.0 - (6.0/7.0 * 3.0/7.0), getList("5", "6"));
		test(2.0/7.0 - (3.0/7.0 * 2.0/7.0), getList("6", "7"));
	}
	
	@Test
	public void testTriplePropositions() {
		test(2.0/7.0 - (4.0/7.0 * 4.0/7.0), getList("1", "2", "3"));
		test(3.0/7.0 - (4.0/7.0 * 4.0/7.0), getList("2", "3", "4"));
		test(2.0/7.0 - (3.0/7.0 * 6.0/7.0), getList("3", "4", "5"));
		test(0.0/7.0 - (3.0/7.0 * 4.0/7.0), getList("4", "5", "6"));
		test(2.0/7.0 - (2.0/7.0 * 6.0/7.0), getList("5", "6", "7"));
	}
	
	@Test
	public void testQuadruplePropositions() {
		test(2.0/7.0 - (3.0/7.0 * 5.0/7.0), getList("1", "2", "3", "4"));
		test(2.0/7.0 - (6.0/7.0 * 3.0/7.0), getList("2", "3", "4", "5"));
		test(0.0/7.0 - (3.0/7.0 * 3.0/7.0), getList("3", "4", "5", "6"));
		test(0.0/7.0 - (4.0/7.0 * 2.0/7.0), getList("4", "5", "6", "7"));
	}
	
}

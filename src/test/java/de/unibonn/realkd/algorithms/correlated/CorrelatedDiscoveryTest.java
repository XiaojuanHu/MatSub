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
package de.unibonn.realkd.algorithms.correlated;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;

import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.common.workspace.Workspaces;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.xarf.XarfImport;
import de.unibonn.realkd.patterns.correlated.CorrelationPattern;

/**
 * @author Panagiotis Mandros
 *
 */
public class CorrelatedDiscoveryTest {

	public static final String CSV_FILE = "src/test/resources/data/tic_tac_toe/tic_tac_toe.xarf";
	public static final double DOUBLE_PRECISION = 0.01;

	static XarfImport builder = XarfImport.xarfImport(CSV_FILE);
	static DataTable dataTable = builder.get();

	@Test
	public void testBestPatternBNB() throws ValidationException {
		Workspace workspace = Workspaces.workspace();
		workspace.add(dataTable);
		BNBCorrelatedPatternSearch correlatedPatternSearch = new BNBCorrelatedPatternSearch(workspace);
		Collection<CorrelationPattern> resultPatterns = correlatedPatternSearch.call();
		CorrelationPattern[] resultsPatternsToArray = resultPatterns.toArray(new CorrelationPattern[0]);
		assertEquals(0.08692586045981629,
				resultsPatternsToArray[0].value(resultsPatternsToArray[0].correlationMeasure()), DOUBLE_PRECISION);
	}

	@Test
	public void testBestPatternBeam() throws ValidationException {
		Workspace workspace = Workspaces.workspace();
		workspace.add(dataTable);
		BeamCorrelatedPatternSearch correlatedPatternSearch = new BeamCorrelatedPatternSearch(workspace);
		Collection<CorrelationPattern> resultPatterns = correlatedPatternSearch.call();
		CorrelationPattern[] resultsPatternsToArray = resultPatterns.toArray(new CorrelationPattern[0]);
		assertEquals(0.08692586045981629,
				resultsPatternsToArray[0].value(resultsPatternsToArray[0].correlationMeasure()), DOUBLE_PRECISION);
	}

	@Test
	public void testDiscoveryCardinalityBestSolution() throws ValidationException {
		Workspace workspace = Workspaces.workspace();
		workspace.add(dataTable);

		BNBCorrelatedPatternSearch BNBfunctionalPatternSearch = new BNBCorrelatedPatternSearch(workspace);
		BNBfunctionalPatternSearch.call();

		BeamCorrelatedPatternSearch BeamfunctionalPatternSearch = new BeamCorrelatedPatternSearch(workspace);
		BeamfunctionalPatternSearch.call();

		assertEquals("Best pattern should have a size of 4", BNBfunctionalPatternSearch.bestDepth(), 4);
		assertEquals("Best pattern should have a size of 2", BeamfunctionalPatternSearch.bestDepth(), 2);

	}

}

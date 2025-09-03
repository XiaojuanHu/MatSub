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
package de.unibonn.realkd.algorithms.functional;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;

import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.common.workspace.Workspaces;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.xarf.XarfImport;
import de.unibonn.realkd.patterns.functional.FunctionalPattern;

/**
 * @author Panagiotis Mandros
 *
 */
public class FunctionalDiscoveryTest {

	public static final String CSV_FILE = "src/test/resources/data/tic_tac_toe/tic_tac_toe.xarf";
	public static final double DOUBLE_PRECISION = 0.01;

	@Test
	public void testBestPatternOPUS() throws ValidationException {
		XarfImport builder = XarfImport.xarfImport(CSV_FILE);
		DataTable dataTable = builder.get();
		Workspace workspace = Workspaces.workspace();
		workspace.add(dataTable);
		OPUSFunctionalPatternSearch functionalPatternSearch = new OPUSFunctionalPatternSearch(workspace);
		functionalPatternSearch.target(dataTable.attributes().get(dataTable.attributes().size() - 1));
		Collection<FunctionalPattern> resultPatterns = functionalPatternSearch.call();
		FunctionalPattern[] resultsPatternsToArray = resultPatterns.toArray(new FunctionalPattern[0]);
		assertEquals(0.45, resultsPatternsToArray[0].value(resultsPatternsToArray[0].functionalityMeasure()),
				DOUBLE_PRECISION);
	}

	@Test
	public void testBestPatternBeam() throws ValidationException {
		XarfImport builder = XarfImport.xarfImport(CSV_FILE);
		DataTable dataTable = builder.get();
		Workspace workspace = Workspaces.workspace();
		workspace.add(dataTable);
		BeamFunctionalPatternSearch functionalPatternSearch = new BeamFunctionalPatternSearch(workspace);
		functionalPatternSearch.target(dataTable.attributes().get(dataTable.attributes().size() - 1));
		Collection<FunctionalPattern> resultPatterns = functionalPatternSearch.call();
		FunctionalPattern[] resultsPatternsToArray = resultPatterns.toArray(new FunctionalPattern[0]);
		System.out.println(resultsPatternsToArray[0]);
		assertEquals(0.444, resultsPatternsToArray[0].value(resultsPatternsToArray[0].functionalityMeasure()),
				DOUBLE_PRECISION);
	}

	@Test
	public void testDiscoveryCardinalityBestSolution() throws ValidationException {
		XarfImport builder = XarfImport.xarfImport(CSV_FILE);
		DataTable dataTable = builder.get();
		Workspace workspace = Workspaces.workspace();
		workspace.add(dataTable);

		OPUSFunctionalPatternSearch OPUSfunctionalPatternSearch = new OPUSFunctionalPatternSearch(workspace);
		OPUSfunctionalPatternSearch.target(dataTable.attributes().get(dataTable.attributes().size() - 1));
		OPUSfunctionalPatternSearch.call();

		BeamFunctionalPatternSearch BeamfunctionalPatternSearch = new BeamFunctionalPatternSearch(workspace);
		BeamfunctionalPatternSearch.target(dataTable.attributes().get(dataTable.attributes().size() - 1));
		BeamfunctionalPatternSearch.call();

		assertEquals("Best pattern should have a size of 5", OPUSfunctionalPatternSearch.bestDepth(), 5);
		assertEquals("Best pattern should have a size of 5", BeamfunctionalPatternSearch.bestDepth(), 5);
	}

}

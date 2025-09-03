/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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
package de.unibonn.realkd.algorithms.outlier.LOF;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.DataTableFromCSVFileBuilder;

/**
 * <p>
 * Junit test case that test the static Local Oulier run from the test case
 * there is 9 German cities and one Egyptian city and when run the test, the
 * algorithm could detect Egyptian city as the Outlier
 * </p>
 * 
 * @author Amr Koura
 *
 */
public class LOFAlgorithmUnitTest {

	private static final Logger LOGGER = Logger
			.getLogger(LOFAlgorithmUnitTest.class.getName());

	private static final String ATTRIBUTES_FILENAME = "src/main/resources/data/cities/attributes.txt";
	private static final String DATA_FILENAME = "src/main/resources/data/cities/data.txt";
	private static final String ATTRIBUTE_GROUPS_FILENAME = "src/main/resources/data/cities/groups.txt";

	@Test
	public void test() {

		DataTable table = null;

		int KValue = 4;
		try {
			table = new DataTableFromCSVFileBuilder().setDelimiter(';')
					.setMissingSymbol("?")
					.setAttributeMetadataCSVFilename(ATTRIBUTES_FILENAME)
					.setDataCSVFilename(DATA_FILENAME)
					.setAttributeGroupCSVFilename(ATTRIBUTE_GROUPS_FILENAME)
					.build();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("error in filling the data table");
		}

		LOFOutlier algorithm = new LOFOutlier();
		algorithm.numExamples = 10;
		List<Integer> indecies = new ArrayList<Integer>();
		indecies.add(0);
		indecies.add(1);
		algorithm.computeLofValues(table, indecies, KValue);

		double[] lofValues = algorithm.getLof();

		for (int i = 0; i < lofValues.length; i++) {

			LOGGER.fine((i + 1) + "  " + lofValues[i]);

		}

		for (int i = 0; i < 9; i++) {
			assertTrue(lofValues[i] <= 1.5);
		}
		assertTrue(lofValues[9] > 1.5); // the last one

	}

}

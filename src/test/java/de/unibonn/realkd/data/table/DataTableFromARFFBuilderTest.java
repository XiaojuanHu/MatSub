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
package de.unibonn.realkd.data.table;

import static de.unibonn.realkd.data.xarf.XarfImport.xarfImport;
import static org.junit.Assert.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import de.unibonn.realkd.data.xarf.XarfImport;

/**
 * @author Panagiotis Mandros
 * 
 * @since 0.4.0
 * 
 * @version 0.5.0
 *
 */
public class DataTableFromARFFBuilderTest {
	/**
	 * 
	 */
	private static final Pattern LINE_END = Pattern.compile("(\r\n)|(\r)|(\n)");
	public static final String breast = "src/main/resources/data/breast-cancer/breast-cancer.arff";
	public static final String iono = "src/main/resources/data/ionosphere/ionosphere.arff";
	public static final String iono_missing = "src/main/resources/data/ionosphere/ionosphere_missing.arff";
	public static final String vote = "src/main/resources/data/vote/vote.arff";
	public static final String wizmir = "src/main/resources/data/wizmir/wizmir.arff";

	@Test
	public void descriptionParsed() throws Exception {
		XarfImport builder = xarfImport(breast);
		DataTable dataTable = builder.get();
		assertEquals("Age of patient", dataTable.attribute(0).description());
	}

	@Test
	public void nameParsed() throws Exception {
		XarfImport builder = xarfImport(breast);
		DataTable dataTable = builder.get();
		assertEquals("Patient age", dataTable.attribute(0).caption());
	}

	@Test
	public void descriptionNumberOfLinesTestBreast() throws Exception {
		XarfImport builder = xarfImport(breast);
		DataTable dataTable = builder.get();
		int count = countLines(dataTable.description());
		assertEquals(94, count);
	}

	@Test
	public void descriptionNumberOfLinesTestIono() throws Exception {
		XarfImport builder = xarfImport(iono);
		DataTable dataTable = builder.get();
		int count = countLines(dataTable.description());
		assertEquals(64, count);
	}
	
	@Test
	public void descriptionNumberOfLinesTestIonoMissing() throws Exception {
		XarfImport builder = xarfImport(iono_missing);
		DataTable dataTable = builder.get();
		int count = countLines(dataTable.description());
		assertEquals(64, count);
	}

	@Test
	public void descriptionNumberOfLinesTestVote() throws Exception {
		XarfImport builder = xarfImport(vote);
		DataTable dataTable = builder.get();
		int count = countLines(dataTable.description());
		assertEquals(195, count);
	}

	@Test
	public void descriptionNumberOfLinesTestWizmir() throws Exception {
		XarfImport builder = xarfImport(wizmir);
		DataTable dataTable = builder.get();
		int count = countLines(dataTable.description());
		assertEquals(1, count);
	}

	@Test
	public void numberOfSamplesTestIono() throws Exception {
		XarfImport builder = xarfImport(iono);
		DataTable dataTable = builder.get();
		assertEquals(dataTable.population().size(), 351);
	}
	
	@Test
	public void numberOfSamplesTestIonoMissing() throws Exception {
		XarfImport builder = xarfImport(iono_missing);
		DataTable dataTable = builder.get();
		assertEquals(dataTable.population().size(), 351);
	}

	@Test
	public void numberOfSamplesTestVote() throws Exception {
		XarfImport builder = xarfImport(vote);
		DataTable dataTable = builder.get();
		assertEquals(dataTable.population().size(), 435);
	}

	@Test
	public void numberOfSamplesTestBreast() throws Exception {
		XarfImport builder = xarfImport(breast);
		DataTable dataTable = builder.get();
		assertEquals(dataTable.population().size(), 286);
	}

	@Test
	public void numberOfSamplesTestWizmir() throws Exception {
		XarfImport builder = xarfImport(wizmir);
		DataTable dataTable = builder.get();
		assertEquals(dataTable.population().size(), 1461);
	}

	@Test
	public void numberOfAttributesTestIono() throws Exception {
		XarfImport builder = xarfImport(iono);
		DataTable dataTable = builder.get();
		assertEquals(dataTable.attributes().size(), 35);
	}
	
	@Test
	public void numberOfAttributesTestIonoMissing() throws Exception {
		XarfImport builder = xarfImport(iono_missing);
		DataTable dataTable = builder.get();
		assertEquals(dataTable.attributes().size(), 35);
	}

	@Test
	public void numberOfAttributesTestWizmir() throws Exception {
		XarfImport builder = xarfImport(wizmir);
		DataTable dataTable = builder.get();
		assertEquals(dataTable.attributes().size(), 10);
	}

	@Test
	public void numberOfAttributesTestVote() throws Exception {
		XarfImport builder = xarfImport(vote);
		DataTable dataTable = builder.get();

		assertEquals(dataTable.attributes().size(), 17);
	}

	@Test
	public void numberOfAttributesTestBreast() throws Exception {
		XarfImport builder = xarfImport(breast);
		DataTable dataTable = builder.get();
		assertEquals(dataTable.attributes().size(), 10);
	}

	@Test
	public void missingValueTestIonoMissing() throws Exception {
		XarfImport builder = xarfImport(iono_missing);
		DataTable dataTable = builder.get();
		
		assertEquals(true, dataTable.attribute(0).valueMissing(0));
		assertEquals(true, dataTable.attribute(1).valueMissing(1));
		assertEquals(true, dataTable.attribute(2).valueMissing(2));
		assertEquals(true, dataTable.attribute(3).valueMissing(3));
		assertEquals(true, dataTable.attribute(4).valueMissing(4));
	}
	
	@Test
	public void missingValueBreastTest() throws Exception {
		XarfImport builder = xarfImport(breast);
		DataTable dataTable = builder.get();
		assertEquals(true, dataTable.attribute(4).valueMissing(20));
	}

	@Test
	public void missingValuesVoteTest() throws Exception {
		XarfImport builder = xarfImport(vote);
		DataTable dataTable = builder.get();
		assertEquals(12, dataTable.attribute(0).missingPositions().size());
	}

	private static int countLines(String str) {
		Matcher m = LINE_END.matcher(str);
		int lines = 1;
		while (m.find()) {
			lines++;
		}
		return lines;
		// String[] lines = str.split("\r\n|\r|\n");
		// return lines.length;
	}

}

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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.xarf.XarfImport;

/**
 * @author Panagiotis Mandros
 *
 */
public class DiscretizationTest {
	public static final String glass = "src/main/resources/data/glass/glass.arff";
	public static final String autos = "src/main/resources/data/autos/autos.arff";
	public static final String test = "src/main/resources/data/arff/test.arff";

	@Test
	public void NumberOfBinsOfAllAttributesGlassTest() throws Exception {
		XarfImport builder = XarfImport.xarfImport().dataFilename(glass);
		DataTable dataTable = builder.get();
		DiscreteDataTable discreteDataTable=DataTables.discretization(dataTable, DataTables.equalFrequencyDiscretization(5));
		int[] numberOfBinsForAttributes=new int[]{5,5,5,5,5,5,5,2,3,6};    		
		boolean checkIfCorrectNumberOfBins=true;
		int i=0;
		for(CategoricAttribute<?> attr: discreteDataTable.attributes() )
		{

			if(attr.categories().size()!=numberOfBinsForAttributes[i])
			{
				checkIfCorrectNumberOfBins=false;
			}
			i++;
		}
		assertEquals(true,checkIfCorrectNumberOfBins);
	}

	

}

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
package de.unibonn.realkd.data.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.common.base.Identifier;

/**
 * @author janis
 *
 */
public class DataTableFromDataBuilderTest {
	
	List<Double> dataMetric;
	List<Double> dataMetricNA;
	double[] dataMetricArray;
	boolean[] dataMetricIsNA;
	int[] dataMetricIdxNA;
	List<Integer> dataInteger;
	List<Integer> dataIntegerNA;
	int[] dataIntegerArray;
	boolean[] dataIntegerIsNA;
	int[] dataIntegerIdxNA;
	List<String> dataCategorical;
	List<String> dataCategoricalNA;
	String[] dataCategoricalArray;
	boolean[] dataCategoricalIsNA;
	int[] dataCategoricalIdxNA;
	
	static int[] indexMissing(List<?> value) {
		return IntStream.range(0,value.size())
				.filter(i -> value.get(i)==null).toArray();
	}
	static boolean[] isMissing(List<?> value) {
		int[] idxMissing = indexMissing(value);
		boolean[] isMissing = new boolean[value.size()];
		for(int idx : idxMissing) {
			isMissing[idx] = true;
		}
		return isMissing;
	}
	
	@Before
	public void setup() {
		dataMetric = Arrays.asList(new Double[] {1.,4.,0.,6.,7.3,0.,3.5});
		dataMetricNA = Arrays.asList(new Double[] {1.,null,0.,6.,7.3,null,3.5});
		dataMetricArray = dataMetric.stream().mapToDouble(Double::doubleValue).toArray();
		dataMetricIdxNA = indexMissing(dataMetricNA);
		dataMetricIsNA = isMissing(dataMetricNA);
		
		dataInteger = Arrays.asList(new Integer[] {1,4,0,9,7,7,3});
		dataIntegerNA = Arrays.asList(new Integer[] {1,4,null,9,null,7,3});
		dataIntegerArray = dataInteger.stream().mapToInt(Integer::intValue).toArray();
		dataIntegerIdxNA = indexMissing(dataIntegerNA);
		dataIntegerIsNA = isMissing(dataIntegerNA);
		
		dataCategoricalArray = new String[] {"one","two","three","four","five","six","seven"};
		dataCategorical = Arrays.asList(dataCategoricalArray);
		dataCategoricalNA = Arrays.asList(new String[] {"one","two","three","four",null,"six",null});
		dataCategoricalIdxNA = indexMissing(dataCategoricalNA);
		dataCategoricalIsNA = isMissing(dataCategoricalNA);
	}
	
	/**
	 * Test method for {@link de.unibonn.realkd.data.table.DataTableFromDataBuilder#build()}.
	 */
	@Test
	public void testBuild() throws Exception {
		DataTableFromDataBuilder builder = new DataTableFromDataBuilder();
		builder.addMetricAttribute("TestMetric", "Test Metric", "A test metric", dataMetric);
		DataTable dataTable = builder.build();
		assertNotNull(dataTable);
	}
	/**
	 * Test method for {@link de.unibonn.realkd.data.table.DataTableFromDataBuilder#build()}.
	 */
	@Test @SuppressWarnings("unchecked")
	public void testConversion() {
		
		{
			List<Double> clone = new ArrayList<Double>(dataMetric);
			DataTableFromDataBuilder.setMissingIndex(clone, dataMetricIdxNA);
			assertTrue(clone.equals(dataMetricNA));
			clone = new ArrayList<Double>(dataMetric);
			DataTableFromDataBuilder.setMissingLogical(clone, dataMetricIsNA);
			assertTrue(clone.equals(dataMetricNA));
			clone = DataTableFromDataBuilder.valuesFromArray(dataMetricArray);
			assertTrue(clone.equals(dataMetric));
			clone = DataTableFromDataBuilder.valuesFromArray(dataMetricArray,dataMetricIdxNA);
			assertTrue(clone.equals(dataMetricNA));
			clone = DataTableFromDataBuilder.valuesFromArray(dataMetricArray,dataMetricIsNA);
			assertTrue(clone.equals(dataMetricNA));
		}
		{
			List<Integer> clone = new ArrayList<Integer>(dataInteger);
			DataTableFromDataBuilder.setMissingIndex(clone, dataIntegerIdxNA);
			assertTrue(clone.equals(dataIntegerNA));
			clone = new ArrayList<Integer>(dataInteger);
			DataTableFromDataBuilder.setMissingLogical(clone, dataIntegerIsNA);
			assertTrue(clone.equals(dataIntegerNA));
			clone = DataTableFromDataBuilder.valuesFromArray(dataIntegerArray);
			assertTrue(clone.equals(dataInteger));
			clone = DataTableFromDataBuilder.valuesFromArray(dataIntegerArray,dataIntegerIdxNA);
			assertTrue(clone.equals(dataIntegerNA));
			clone = DataTableFromDataBuilder.valuesFromArray(dataIntegerArray,dataIntegerIsNA);
			assertTrue(clone.equals(dataIntegerNA));
		}
		{
			List<String> clone = new ArrayList<String>(dataCategorical);
			DataTableFromDataBuilder.setMissingIndex(clone, dataCategoricalIdxNA);
			assertTrue(clone.equals(dataCategoricalNA));
			clone = new ArrayList<String>(dataCategorical);
			DataTableFromDataBuilder.setMissingLogical(clone, dataCategoricalIsNA);
			assertTrue(clone.equals(dataCategoricalNA));
			clone = DataTableFromDataBuilder.valuesFromArray(dataCategoricalArray);
			assertTrue(clone.equals(dataCategorical));
			clone = DataTableFromDataBuilder.valuesFromArray(dataCategoricalArray,dataCategoricalIdxNA);
			assertTrue(clone.equals(dataCategoricalNA));
			clone = DataTableFromDataBuilder.valuesFromArray(dataCategoricalArray,dataCategoricalIsNA);
			assertTrue(clone.equals(dataCategoricalNA));
		}
	}
	
	public static <T> List<Optional<T>> optionalise(List<T> values) {
		return values.stream()
				.map(Optional::ofNullable)
				.collect(Collectors.toList());
	}
	@Test
	public void testStaticBuilder() {
		String[] attributeIds = new String[] {"metric_1","integer_cat_2","integer_org_3","categorical_4"};
		String[] attributeCaptions = new String[] {"metric 1","integer.cat 2","integer ord 3","categorical 4"};
		String[] attributeDescriptions = new String[] {"metric attribute 1","integer categorical 2","integer ordinal 3","categorical attribute 4"};
		String[] attributeTypes = Stream.of(DataTableFromDataBuilder.AttributeType.values())
				.map(DataTableFromDataBuilder.AttributeType::toString).toArray(String[]::new);
		Object[] attributeValueArrays = new Object[] {dataMetricArray, dataIntegerArray, dataIntegerArray, dataCategoricalArray};
		Object[] attributeMissing = new Object[] {dataMetricIdxNA, dataIntegerIdxNA, dataIntegerIsNA, dataCategoricalIsNA};
		
		DataTable dataTable = DataTableFromDataBuilder.build(Identifier.id("data_table"), "Relation", "Testing data table",
				attributeIds, attributeCaptions, attributeDescriptions,
				attributeTypes, attributeValueArrays, attributeMissing);
		assertNotNull(dataTable);
		for(int ai=0;ai<attributeIds.length;++ai) {
			assertEquals(dataTable.attribute(ai).identifier().toString(), attributeIds[ai]);
			assertEquals(dataTable.attribute(ai).description(), attributeDescriptions[ai]);
			assertEquals(dataTable.attribute(ai).caption(), attributeCaptions[ai]);
		}
		assertTrue(dataTable.attribute(0).missingPositions().containsAll(IndexSets.of(dataMetricIdxNA)));
		assertTrue(dataTable.attribute(1).missingPositions().containsAll(IndexSets.of(dataIntegerIdxNA)));
		assertTrue(dataTable.attribute(2).missingPositions().containsAll(IndexSets.of(dataIntegerIdxNA)));
		assertTrue(dataTable.attribute(3).missingPositions().containsAll(IndexSets.of(dataCategoricalIdxNA)));
		assertEquals(dataTable.attribute(0).getValues(),optionalise(dataMetricNA));
		assertEquals(dataTable.attribute(1).getValues(),optionalise(dataIntegerNA));
		assertEquals(dataTable.attribute(2).getValues(),optionalise(dataIntegerNA));
		assertEquals(dataTable.attribute(3).getValues(),optionalise(dataCategoricalNA));
	}
}

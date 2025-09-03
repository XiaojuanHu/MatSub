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
package de.unibonn.realkd.patterns.sequence;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.junit.Test;

import de.unibonn.realkd.data.propositions.Proposition;

/**
 * @author Sandy Moens
 *
 * @since 0.3.0
 * 
 * @version 0.6.0
 *
 */
public class DefaultCohesionMeasurementProcedureTest extends SequenceMeasureProcedureTest {
	
	public DefaultCohesionMeasurementProcedureTest() {
		super(Cohesion.COHESION);
	}
	
	@Test
	public void singleListSingleItemEqualsOne() {
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1"));
			test(1.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value2"));
			test(1.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature2=value1"));
			test(1.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature2=value2"));
			test(1.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature2=value30"));
			test(1.0, orderedSetBuilders);
		}
	}
	
	@Test
	public void singleEventMultipleEvents() {
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1", "feature1=value2"));
			test(0.5, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value2", "feature1=value1"));
			test(0.5, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1", "feature2=value1"));
			test(0.5, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value2", "feature2=value2"));
			test(1.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value2", "feature2=value1"));
			test(1.0/(4.0/3.0), orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value2", "feature2=value3"));
			test(0.5, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value3", "feature2=value20", "feature1=value3"));
			test(1, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value3", "feature2=value20", "feature2=value30"));
			test(0.5, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value3", "feature2=value20", "feature1=value3", "feature2=value30"));
			test(0.5, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1", "feature2=value10", "feature1=value2", "feature2=value1"));
			test(0.25, orderedSetBuilders);
		}
	}
	
	@Test
	public void multipleEventsMultipleEvents() {
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1"));
			orderedSetBuilders.add(getList("feature1=value2"));
			test(1.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value2"));
			orderedSetBuilders.add(getList("feature1=value1"));
			test(1.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1"));
			orderedSetBuilders.add(getList("feature2=value1"));
			test(2.0/3.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value2"));
			orderedSetBuilders.add(getList("feature2=value2"));
			test(1.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value2"));
			orderedSetBuilders.add(getList("feature2=value1"));
			test(2.0/(5.0/2.0), orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value2"));
			orderedSetBuilders.add(getList("feature2=value3"));
			test(Double.NaN, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value3"));
			orderedSetBuilders.add(getList("feature2=value20", "feature1=value3"));
			test(Double.NaN, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value3", "feature2=value20"));
			orderedSetBuilders.add(getList("feature1=value3"));
			test(1.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value3"));
			orderedSetBuilders.add(getList("feature2=value20"));
			orderedSetBuilders.add(getList("feature1=value3"));
			test(Double.NaN, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value3"));
			orderedSetBuilders.add(getList("feature2=value20", "feature2=value30"));
			test(Double.NaN, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value3", "feature2=value20"));
			orderedSetBuilders.add(getList("feature2=value30"));
			test(1.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value3"));
			orderedSetBuilders.add(getList("feature2=value20"));
			orderedSetBuilders.add(getList("feature2=value30"));
			test(Double.NaN, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value3"));
			orderedSetBuilders.add(getList("feature2=value20", "feature1=value3", "feature2=value30"));
			test(Double.NaN, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value3", "feature2=value20"));
			orderedSetBuilders.add(getList("feature1=value3", "feature2=value30"));
			test(1.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1"));
			orderedSetBuilders.add(getList("feature2=value10", "feature1=value2", "feature2=value1"));
			test(0.5, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1"));
			orderedSetBuilders.add(getList("feature2=value10"));
			orderedSetBuilders.add(getList("feature1=value2", "feature2=value1"));
			test(0.75, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1", "feature2=value10"));
			orderedSetBuilders.add(getList("feature1=value2", "feature2=value1"));
			test(0.5, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1", "feature2=value10", "feature1=value2"));
			orderedSetBuilders.add(getList("feature2=value1"));
			test(0.5, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1", "feature1=value2", "feature2=value10"));
			orderedSetBuilders.add(getList("feature2=value1"));
			test(0.5, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1"));
			orderedSetBuilders.add(getList("feature2=value10"));
			orderedSetBuilders.add(getList("feature1=value2"));
			orderedSetBuilders.add(getList("feature2=value1"));
			test(1.0, orderedSetBuilders);
		}
		{
			List<List<Proposition>> orderedSetBuilders = newArrayList();
			orderedSetBuilders.add(getList("feature1=value1"));
			orderedSetBuilders.add(getList("feature1=value2"));
			orderedSetBuilders.add(getList("feature2=value10"));
			orderedSetBuilders.add(getList("feature2=value1"));
			test(Double.NaN, orderedSetBuilders);
		}
	}

	
}

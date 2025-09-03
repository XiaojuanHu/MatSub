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
package de.unibonn.realkd.patterns.sequence;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;

import com.google.common.collect.Maps;

import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.sequences.SequentialPropositionalContext;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternDescriptor;

/**
 * @author Sandy Moens
 *
 * @since 0.6.0
 * 
 * @version 0.6.0
 * 
 */
public abstract class SequenceMeasureProcedureTest {

	private static final double PRECISION = 0.00000000001;
	
	private Workspace dataWorkspace;
	private SequentialPropositionalContext context;
	private Map<String, Proposition> propositionMap;
	
	private MeasurementProcedure<Measure,PatternDescriptor> measurementProcedure; 
	
	public SequenceMeasureProcedureTest(MeasurementProcedure<Measure,PatternDescriptor> measurementProcedure) {
		this.measurementProcedure = measurementProcedure;
	}

	@Before
	public void setUp() {
		this.dataWorkspace = TestConstants.getSequenceWorkspaceWithDateDistance();
		this.context = (SequentialPropositionalContext) dataWorkspace.propositionalContexts().get(0);

		this.propositionMap = Maps.newHashMap();
		
		this.context.propositions().forEach(p -> propositionMap.put(p.name(), p));
	}
	
	protected List<Proposition> getList(String ...names) {
		List<Proposition> b = newArrayList();
		for(String name: names) {
			b.add(this.propositionMap.get(name));
		}
		return b;
	}
	
	protected void test(double expected, List<List<Proposition>> orderedSets) {
		SequenceDescriptor sequenceDescriptor = DefaultSequenceDescriptor.create(this.context, orderedSets);
		Assert.assertEquals(expected, this.measurementProcedure.perform(sequenceDescriptor).value(), PRECISION);
	}
	
}

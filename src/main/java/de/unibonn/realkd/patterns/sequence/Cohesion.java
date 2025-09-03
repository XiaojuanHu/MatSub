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
package de.unibonn.realkd.patterns.sequence;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static de.unibonn.realkd.common.base.Identifier.id;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.sequences.SequenceEvent;
import de.unibonn.realkd.data.sequences.SequenceTransaction;
import de.unibonn.realkd.data.sequences.SequentialPropositionalContext;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternDescriptor;

/**
 * Procedure for computing the cohesion of a sequence pattern in a sequence database.
 * 
 * @author Sandy Moens
 * 
 * @since 0.3.0
 * 
 * @version 0.7.0
 *
 */
public enum Cohesion implements Measure, MeasurementProcedure<Measure,PatternDescriptor> {

	COHESION;

	private Cohesion() {
		;
	}

	@Override
	public Identifier identifier() {
		return id("sequence_cohesion");
	}

	@Override
	public String caption() {
		return "cohesion";
	}

	@Override
	public String description() {
		return "Indicates the cohesion or the tightness of a sequence pattern.";
	}

	@Override
	public boolean isApplicable(PatternDescriptor descriptor) {
		return SequenceDescriptor.class.isAssignableFrom(descriptor.getClass());
	}

	@Override
	public Measure getMeasure() {
		return this;
	}

	private int getWindow(SequenceTransaction sequence, List<SequenceEvent<?>> events, List<List<Proposition>> orderedSets, int startIx) {
		int window = -1;
		int ix = startIx;
		Set<Proposition> propositionsToFind = newHashSet();
		
		Object timestamp = null;
		
		for(List<Proposition> orderedSet: orderedSets) {
			propositionsToFind = newHashSet(orderedSet);
			while(!propositionsToFind.isEmpty() && ix < events.size()) {
				boolean changed = propositionsToFind.remove(events.get(ix).proposition());
				if(window == -1 && changed) {
					window = 1;
					timestamp = events.get(ix).value();
					
				} else if (window != -1) {
					if(timestamp != events.get(ix).value()) { 
						window++;
					}
					timestamp = events.get(ix).value();
				}
				ix++;
			}
			
			Comparable<?> value = events.get(ix - 1).value();
			
			while(ix < events.size()) {
				if(value != events.get(ix).value()) {
					break;
				}
				ix++;
			}
		}			
		if(propositionsToFind.isEmpty() && ix <= events.size()) {
			return window;
		}
		return -1;
	}
	
	private List<List<Proposition>> getOrderedSetsNoEmpty(SequenceDescriptor descriptor) {
		return descriptor.orderedSets().stream().filter(o -> !o.isEmpty()).collect(Collectors.toList());
	}
	
	@Override
	public Measurement perform(PatternDescriptor descriptor) {

		SequenceDescriptor sequenceDescriptor = (SequenceDescriptor) descriptor;

		SequentialPropositionalContext context = sequenceDescriptor.sequentialPropositionalLogic();

		double totWindow = 0;
		double support = 0;
		List<List<Proposition>> orderedSetsNoEmpty = getOrderedSetsNoEmpty(sequenceDescriptor);
		
		IndexSet indexSet = IndexSets.full(context.population().size() - 1);
		
		for(List<Proposition> orderedSet: sequenceDescriptor.orderedSets()) {
			for(Proposition proposition: orderedSet) {
				indexSet = IndexSets.intersection(indexSet, proposition.supportSet());
			}
		}
		
		for(int ix: indexSet) {
			SequenceTransaction sequence = context.sequences().get(ix);
			long minWindow = -1;
			for(int i = 0; i <  sequence.events().size(); i++) {
				int window = getWindow(sequence, newArrayList(sequence.events()), orderedSetsNoEmpty, i);
				if(window != -1) {
					if(minWindow == -1) {
						minWindow = window;
					} else{
						minWindow = Math.min(minWindow, window);
					}
				}
				if(minWindow == orderedSetsNoEmpty.size()) {
					//Can not get smaller!
					break;
				}
			}
			if(minWindow != -1) {
				support++;
				totWindow += minWindow;
			}
		}

		double avgWindow = totWindow / support;
		double cohesion = 1. * orderedSetsNoEmpty.size() / avgWindow;

		return Measures.measurement(this, cohesion);
	}

}

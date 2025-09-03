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

import static com.google.common.collect.Lists.newLinkedList;
import static de.unibonn.realkd.common.base.Identifier.id;
import static java.util.stream.Collectors.toList;

import java.util.List;

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
 * Procedure for computing the support of a pattern. This is defined as: the
 * absolute occurrence frequency of the pattern in the complete data.
 * 
 * @author Sandy Moens
 * 
 * @since 0.1.2
 * 
 * @version 0.7.0
 *
 */
public enum SequenceSupport implements Measure, MeasurementProcedure<Measure,PatternDescriptor> {

	SEQUENCE_SUPPORT;

	private SequenceSupport() {
		;
	}

	@Override
	public Identifier identifier() {
		return id("sequence_support");
	}

	@Override
	public String caption() {
		return "support";
	}

	@Override
	public String description() {
		return "Absolute occurance frequency of the pattern in the complete data.";
	}

	@Override
	public boolean isApplicable(PatternDescriptor descriptor) {
		return SequenceDescriptor.class.isAssignableFrom(descriptor.getClass());
	}

	@Override
	public Measure getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(PatternDescriptor descriptor) {

		SequenceDescriptor sequenceDescriptor = (SequenceDescriptor) descriptor;

		SequentialPropositionalContext context = sequenceDescriptor.sequentialPropositionalLogic();
		
		double support = 0;
		
		int descriptorSizeNoEmpties = sequenceDescriptor.orderedSets().stream().filter(o -> !o.isEmpty()).collect(toList()).size();
		
		IndexSet indexSet = IndexSets.full(context.population().size() - 1);
		
		for(List<Proposition> orderedSet: sequenceDescriptor.orderedSets()) {
			for(Proposition proposition: orderedSet) {
				indexSet = IndexSets.intersection(indexSet, proposition.supportSet());
			}
		}
		
		for(int i: indexSet) {
			SequenceTransaction sequence = context.sequences().get(i);
			
			List<SequenceEvent<?>> events = sequence.events();
			
			if(events.size() < descriptorSizeNoEmpties) {
				continue;
			}
			
			int ix = 0;
			int ixx = 0;
			List<Proposition> propositionsToFind = newLinkedList();
			for(List<Proposition> orderedSet: sequenceDescriptor.orderedSets()) {
				propositionsToFind.addAll(orderedSet);

				while(!propositionsToFind.isEmpty() && ix < events.size()) {
					propositionsToFind.remove(events.get(ix).proposition());
					ix++;
				}
				
				if(ix > 0 && ix != events.size()) {
					Comparable<?> value = events.get(ix - 1).value();
					
					while(ix < events.size()) {
						if(value != events.get(ix).value()) {
							break;
						}
						ix++;
					}
				}
				
				ixx++;
				if(ix == events.size()) {
					break;
				}
			}
			if(propositionsToFind.isEmpty()  && ixx >= descriptorSizeNoEmpties) {
				support++;
			}
		}

		return Measures.measurement(this, support);
	}

}

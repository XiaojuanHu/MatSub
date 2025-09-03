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

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.sequences.SequentialPropositionalContext;

/**
 * @author Sandy Moens
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public class SequenceTestInputs {
	
	private static Sequence createSequence0(SequentialPropositionalContext context) {
		List<List<Proposition>> orderedSetsDescriptors = ImmutableList.of();
		
		return Sequences.create(DefaultSequenceDescriptor.create(context, orderedSetsDescriptors), ImmutableList.of());
	}
	
	private static Sequence createSequence1(SequentialPropositionalContext context) {
		List<Proposition> orderedSetDescriptor1 = ImmutableList.of(context.proposition(1));
		
		List<List<Proposition>> orderedSetsDescriptors = ImmutableList.of(orderedSetDescriptor1);
		
		return Sequences.create(DefaultSequenceDescriptor.create(context, orderedSetsDescriptors), ImmutableList.of());
	}
	
	private static Sequence createSequence2(SequentialPropositionalContext context) {
		List<Proposition> orderedSetDescriptor1 = ImmutableList.of(context.proposition(1), context.proposition(3));
		List<Proposition> orderedSetDescriptor2 = ImmutableList.of(context.proposition(2));
		
		List<List<Proposition>> orderedSetsDescriptors = ImmutableList.of(orderedSetDescriptor1, orderedSetDescriptor2);
		
		return Sequences.create(DefaultSequenceDescriptor.create(context, orderedSetsDescriptors), ImmutableList.of());
	}

	private static Sequence createSequence3(SequentialPropositionalContext context) {
		List<Proposition> orderedSetDescriptor1 = ImmutableList.of(context.proposition(1), context.proposition(3));
		List<Proposition> orderedSetDescriptor2 = ImmutableList.of(context.proposition(2));
		List<Proposition> orderedSetDescriptor3 = ImmutableList.of(context.proposition(4));
		
		List<List<Proposition>> orderedSetsDescriptors = ImmutableList.of(orderedSetDescriptor1, orderedSetDescriptor2, orderedSetDescriptor3);
		
		return Sequences.create(DefaultSequenceDescriptor.create(context, orderedSetsDescriptors), ImmutableList.of());
	}
	
	public static Iterable<Object[]> getOrdinalSequencePatternTestInput() {
		Workspace workspace = TestConstants.getSequenceWorkspaceWithOrdinalDistance();
		SequentialPropositionalContext context = (SequentialPropositionalContext) workspace.propositionalContexts().get(0);
		
		return Arrays.asList(new Object[][] {
				{ workspace, createSequence0(context) },
				{ workspace, createSequence1(context) },
				{ workspace, createSequence2(context) },
				{ workspace, createSequence3(context) }
		});
	}
	
	public static Iterable<Object[]> getDateSequencePatternTestInput() {
		Workspace workspace = TestConstants.getSequenceWorkspaceWithOrdinalDistance();
		SequentialPropositionalContext context = (SequentialPropositionalContext) workspace.propositionalContexts().get(0);
		
		return Arrays.asList(new Object[][] {
			{ workspace, createSequence0(context) },
			{ workspace, createSequence1(context) },
			{ workspace, createSequence2(context) },
			{ workspace, createSequence3(context) }
		});
	}
}

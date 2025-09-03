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
package de.unibonn.realkd.data.propositions;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.workspace.Entity;
import de.unibonn.realkd.data.Population;

/**
 * Collection of binary statements (propositions) about some fixed population of
 * data objects.
 * 
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.1.2
 * 
 * @version 0.6.0
 *
 */
public interface PropositionalContext extends Entity {

	public abstract Population population();

	/**
	 * Provides all base propositions available in this propositional logic.
	 * 
	 */
	public abstract List<? extends Proposition> propositions();
	
	public abstract Optional<Integer> index(Proposition p);

	/**
	 * <p>
	 * Provides the set (of indices) of data objects for which some specific
	 * proposition evaluates to 'true'.
	 * </p>
	 * 
	 * <p>
	 * The returned set is immutable and guaranteed to provide fixed iteration
	 * order in ascending order of object indices.
	 * </p>
	 * 
	 * @param propositionIndex
	 *            the index of the proposition (corresponding to
	 *            {@link #propositions()}) for which the support set is
	 *            requested
	 * @return the set of data objects (indices) supporting the proposition
	 * 
	 */
	public abstract IndexSet supportSet(int basePropositionIndex);

	public default boolean holdsFor(int basePropositionIndex, IndexSet subPopulationIndices) {
		Proposition proposition = propositions().get(basePropositionIndex);
		return proposition.supportSet().containsAll(subPopulationIndices);
//		for (int i : subPopulationIndices) {
//			if (!proposition.holdsFor(i)) {
//				return false;
//			}
//		}
//		return true;
	};

	/**
	 * <p>
	 * Returns the complete "truth" about some data object, i.e., the set of all
	 * propositions that evaluate to 'true' for the specified data object.
	 * </p>
	 * 
	 * <p>
	 * The returned set is immutable and guaranteed to provide fixed iteration
	 * order in ascending order of proposition indices.
	 * </p>
	 * 
	 * @param objectId
	 *            the index of the data object (corresponding to
	 *            {@link #objectIds()}) for which truth set is requested
	 * @return the set of base proposition (indices) supported by the data
	 *         object
	 */
	public abstract Set<Integer> truthSet(int objectId);

	/**
	 * Convenience method to access a specific base proposition (identified by
	 * index in proposition list).
	 * 
	 * @param i
	 *            the index of the requested proposition
	 * @return proposition with index i
	 */
	public default Proposition proposition(int i) {
		return propositions().get(i);
	};
	
	@Override
	public default List<Entity> dependencies() {
		return ImmutableList.of(population());
	}

	// public default boolean empiricallyImplies(int i, int j) {
	// Set<Integer> x=getSupportSet(i);
	// Set<Integer> y=getSupportSet(j);
	// //...
	// };

}
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
 */

package de.unibonn.realkd.patterns.logical;

import java.util.Collection;
import java.util.List;

import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.LocalPatternDescriptor;

/**
 * <p>
 * Conjunction of an aggregated set of propositions.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.6.0
 * 
 */
public interface LogicalDescriptor extends LocalPatternDescriptor, Iterable<Proposition> {

	/**
	 * 
	 * @return number of contained propositions
	 */
	public int size();

	public boolean isEmpty();

	public List<String> getElementsAsStringList();

	/**
	 * <p>
	 * Computes the maximal support-preserving specialization of this
	 * conjunction that can be created through given list of propositions.
	 * </p>
	 * 
	 * @param augmentations
	 *            collection of potential augmentations
	 * 
	 * @return maximal specialization of conjunction that can be build from
	 *         provided augmentation elements
	 * 
	 */
	public LogicalDescriptor supportPreservingSpecialization(List<Proposition> augmentations);

	public LogicalDescriptor specialization(Proposition augmentation);

	public LogicalDescriptor generalization(Proposition reductionElement);

	public Collection<Proposition> elements();

	public default boolean semanticallyImplies(Proposition prop) {
		for (Proposition element : elements()) {
			if (element.implies(prop)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return whether descriptor is minimal in describing its extension
	 */
	public boolean minimal();

	public LogicalDescriptor lexicographicallyLastMinimalGenerator();

	public boolean empiricallyImplies(Proposition prop);
	// {
	// return semanticallyImplies(prop) ||
	// prop.getSupportSet().containsAll(indices());
	// }

	/**
	 * Checks whether at least one member proposition is defined based on a
	 * specific table attribute.
	 * 
	 * @param attribute
	 *            the attribute for which to check reference
	 * @return whether there is at least one proposition referring to attribute
	 */
	public boolean refersToAttribute(Attribute<?> attribute);

	public SerialForm<LogicalDescriptor> serialForm();

	// /**
	// * Computes the closure of this descriptor, i.e., the unique maximal
	// logical
	// * descriptor that is extension-equivalent (same support set) to this.
	// Here,
	// * maximal refers to the subsumption relation with respect to all (base)
	// * propositions in the underlying propositional logic.
	// *
	// * @return unique maximal support-set-equivalent extension
	// *
	// */
	// public LogicalDescriptor closure();

}

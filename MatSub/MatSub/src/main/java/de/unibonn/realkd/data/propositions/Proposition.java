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

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.workspace.HasSerialForm;

/**
 * <p>
 * Binary statement that can be checked for each data object of some entailing
 * {@link DefaultTableBasedPropositionalLogic}.
 * </p>
 * <p>
 * This general interface was added in realKD 0.1.2 while the more specific
 * concept of propositions that depend on some underlying attribute has been
 * part of realKD from the beginning.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.1.2
 *
 */
public interface Proposition extends HasSerialForm<Proposition> {

	/**
	 * Checks the proposition against a specific entry in the underlying
	 * DataTable.
	 * 
	 * @param i
	 *            the objectId in the database against which the proposition is
	 *            checked
	 * @return false if any value required for the check is missing; otherwise
	 *         true if and only if proposition holds.
	 */
	public abstract boolean holdsFor(int i);

	public abstract String name();

	/**
	 * <p>
	 * Checks whether it is known that this proposition implies another
	 * proposition. Does not guarantee to be true for all cases of implication
	 * (therefore default implementation returns false for all arguments).
	 * </p>
	 *
	 * <p>
	 * For market basket analysis this can for instance happen with the
	 * propositions "milk" (this) and "diary product" (other). For
	 * attribute-based propositions this can happen when there is a functional
	 * dependencies between the underlying attributes.
	 * </p>
	 * 
	 * <p>
	 * Algorithms can use this information to avoid to generate redundant
	 * information, e.g., 'milk' in a transaction implies 'diary product' in a
	 * transaction.
	 * </p>
	 * 
	 * <p>
	 * Based on the theory of logic this test cannot guaranteed to be complete.
	 * Also note that this is supposed to be a syntactic check based on
	 * domain-logic that independent of concrete data. That is, 'milk' implies
	 * 'dairy product' for all datasets whereas 'gender=male' can imply
	 * 'height&gt;=1.70m' in some datasets while it does not in others.
	 * </p>
	 * 
	 * @param anotherProposition
	 *            proposition which is tested for whether it is implied by this
	 * 
	 * @return whether this is known to imply other proposition
	 * 
	 */
	public default boolean implies(Proposition anotherProposition) {
		return false;
	}

	/**
	 * Method that returns a sorted set of all object indices for which the
	 * proposition holds. It is the choice of the implementation if this result
	 * is buffered. Typically algorithm access the support set of a proposition
	 * often.
	 * 
	 * @return the support set of the proposition
	 * 
	 */
	public abstract IndexSet supportSet();

	/**
	 * Convenience method.
	 * 
	 * @return getSupportSet().size()
	 */
	public abstract int supportCount();
	

}
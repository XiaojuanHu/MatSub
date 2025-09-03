/**
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
 *
 */
package de.unibonn.realkd.algorithms.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.unibonn.realkd.patterns.Pattern;

/**
 * Interface for algorithm building blocks that take a whole collection of
 * patterns and apply some operations to them---e.g., as post-processing step.
 * Return type of the operation is general Collection. A processes can also be
 * defined to order a pattern collection in which case the class of the result
 * would be an ordered collection (which should be documented clearly for such
 * processors).
 * 
 * When used within StoppableMiningAlgorithms processors should only apply fast
 * operations.
 * 
 * @author mboley
 * 
 */
public interface PatternCollectionProcessor {

	public Collection<Pattern<?>> process(Collection<Pattern<?>> patterns);

	/**
	 * Operations that provides input as output (new object) without any content
	 * changes.
	 */
	public static PatternCollectionProcessor NO_PROCESSOR = new PatternCollectionProcessor() {

		@Override
		public Collection<Pattern<?>> process(Collection<Pattern<?>> patterns) {
			return new ArrayList<Pattern<?>>(patterns);
		}
	};

	/**
	 * Pattern collection processor that filters out all patterns that do not
	 * satisfy all members of some set of constraints and otherwise keeps the
	 * order of the input collection.<br>
	 * 
	 * <br>
	 * Constraints are checked in order specified by constructor argument.
	 * 
	 * @author mboley
	 * 
	 */
	public static class ConstraintBasedFilter implements
			PatternCollectionProcessor {

		private List<PatternConstraint> constraints;

		public ConstraintBasedFilter(List<PatternConstraint> constraints) {
			this.constraints = constraints;
		}

		@Override
		public List<Pattern<?>> process(Collection<Pattern<?>> patterns) {
			List<Pattern<?>> result = new ArrayList<>();
			for (Pattern<?> pattern : patterns) {
				if (satisfiesAllConstraints(pattern)) {
					result.add(pattern);
				}
			}
			return result;
		}

		private boolean satisfiesAllConstraints(Pattern<?> pattern) {
			for (PatternConstraint constraint : constraints) {
				if (!constraint.satisfies(pattern)) {
					return false;
				}
			}
			return true;
		}

	}

}

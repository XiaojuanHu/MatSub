/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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
package de.unibonn.realkd.computations.dag;

import de.unibonn.realkd.computations.core.Computation;

/**
 * <p>
 * Computations that perform search in a directed acyclic structure (most
 * commonly trees) for one or more solutions starting from a single (or
 * potentially multiple) root node, refining active nodes with a certain
 * strategy, and either adding refinements to the boundary of active nodes or
 * discarding them.
 * </p>
 * <p>
 * In this context, information about the number of explored nodes, maximum
 * attained depth, etc. is available.
 * </p>
 * 
 * @author Panagiotis Mandros
 * 
 * @author Mario Boley
 * 
 * @since 0.6.2
 * 
 * @version 0.6.2
 *
 */
public interface DagSearch<T> extends Computation<T> {

	public int nodesCreated();

	public int nodesDiscarded();

	public int boundarySize();

	public int maxAttainedBoundarySize();

	public int maxAttainedDepth();

	/**
	 * @return depth of (current) best solution
	 */
	public int bestDepth();

}

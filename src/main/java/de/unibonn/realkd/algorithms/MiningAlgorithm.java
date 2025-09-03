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

package de.unibonn.realkd.algorithms;

import java.util.Collection;

import com.google.common.reflect.Parameter;

import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.ParameterContainer;
import de.unibonn.realkd.computations.core.Computation;
import de.unibonn.realkd.patterns.Pattern;

/**
 * Mining algorithms are Java Callables that return a collection of patterns and
 * provide metadata in the form of name, description, and category. In addition
 * they offer means to transparently interact with and provide meta-information
 * about their parameters.
 * 
 * @author Pavel Tokmakov
 * @author Mario Boley
 * @author Björn Jacobs
 * 
 * @since 0.1.0
 * 
 * @version 0.6.2
 * 
 */
public interface MiningAlgorithm extends Computation<Collection<? extends Pattern<?>>>, ParameterContainer {

	/**
	 * Runs the algorithm with specified parameter values.
	 * 
	 * @return Collection of result patterns that is not necessarily ordered
	 *         (depends on concrete algorithm).
	 * 
	 * @throws IllegalStateException
	 *             when algorithm already running (in another thread) or one of its
	 *             parameters is either not set (null) or set to an invalid value
	 *             ({@link Parameter}).
	 */
	public Collection<? extends Pattern<?>> call() throws ValidationException;

	/**
	 * Flag indicating whether the algorithm is currently being executed. Algorithm
	 * may not be called a second time and no parameters can be set while algorithm
	 * running.
	 */
	public boolean running();

	/**
	 * @return the name of the algorithm in a user-readable form.
	 */
	public String caption();

	/**
	 * 
	 * @return the description of the algorithm in a user-readable form.
	 */
	public String description();

	/**
	 * 
	 * @return the category of the algorithm that can be used by UIs for menu
	 *         structures.
	 */
	public AlgorithmCategory getCategory();

}

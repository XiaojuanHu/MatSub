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

package de.unibonn.realkd.common.parameter;

import java.util.Collection;
import java.util.stream.Collectors;

import de.unibonn.realkd.common.base.Identifiable;

/**
 * Interface for mining parameters that have a finite set of valid choices,
 * which can, e.g., be displayed by an UI or iterated over by a parameter
 * evaluation procedure.
 * 
 * @author Björn Jacobs
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.6.0
 * 
 */
public interface RangeEnumerableParameter<T> extends Parameter<T> {

	/**
	 * Computes the range of currently valid values for parameter. If parameter
	 * depends on other parameters and context is not valid then must return an
	 * empty collection.
	 * 
	 * @return range of currently valid values for parameter
	 * 
	 */
	public Collection<? extends T> getRange();

	/**
	 * 
	 * @return user-readable string with currently valid options
	 * 
	 */
	public default String getRangeOptionString() {
		return getRange().stream()
				.map(o -> "'" + ((o instanceof Identifiable) ? ((Identifiable) o).identifier().toString() : o.toString()) + "'")
				.collect(Collectors.joining(",", "(valid range in current context: ", ")"));
	}

}

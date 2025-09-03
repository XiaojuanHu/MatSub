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
package de.unibonn.realkd.patterns;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.Population;

/**
 * Descriptor of event/pattern that occurs on some subset of a given population.
 * This subset is referred to as <b>support set</b>.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.4.1
 *
 */
public interface LocalPatternDescriptor extends PatternDescriptor {

	/**
	 * 
	 * @return the global population to which this descriptor describes a subset
	 */
	public Population population();

	/**
	 * 
	 * @return the ordered index set of the population members that are part of
	 *         the described sub-population
	 */
	public IndexSet supportSet();

}

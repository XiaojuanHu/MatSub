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

/**
 * <p>
 * Possible categories of algorithms.
 * </p>
 * <p>
 * WARNING: this concept will probably leave realKD in the future to Creedo
 * (because it is a solely tagged on means for user guidance in the Creeo UI)
 * </p>
 * 
 * @author Björn Jacobs
 * 
 * @since 0.1.0
 * 
 * @version 0.3.2
 * 
 */
public enum AlgorithmCategory {
	ASSOCIATION_MINING("Association pattern mining"),
	ASSOCIATION_RULE_MINING("Association rule mining"),
	PURE_SUBGROUP_DISCOVERY("Pure subgroup discovery"),
	EXCEPTIONAL_SUBGROUP_DISCOVERY("Exceptional subgroup discovery"),
	FUNCTIONAL_PATTERN_DISCOVERY("Functional pattern discovery"),
	OUTLIER_DETECTION("Outlier Detection"), 
	SEQUENCE_MINING("Sequence mining"), 
	EPISODE_MINING("Episode mining"),
	OTHER("Other algorithms"),
	GENERIC("Generic");

	private final String fullName;

	public String getName() {
		return fullName;
	}

	private AlgorithmCategory(String fullName) {
		this.fullName = fullName;
	}
}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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
package de.unibonn.realkd.util;

import java.util.Collection;

/**
 * @author Mario Boley
 * 
 * @since 0.5.1
 * 
 * @version 0.5.1
 *
 */
public class Combinatorics {

	private Combinatorics() {
		;
	}

	/**
	 * <p>
	 * Number of edges in a complete k-partite graph with given number of nodes
	 * per partition element.
	 * </p>
	 * <p>
	 * Implementation uses linear time recursion formula
	 * f(c1,c2,c3,...,ck)=c1*c2+f(c1+c2,c3,...,ck).
	 * </p>
	 * 
	 * @param partitionSizes
	 *            the number of nodes in each element of the partition
	 * @return number of edges in k-partite graph with given partition sizes
	 */
	public static int kPartiteEdgeCount(Collection<Integer> partitionSizes) {
		if (partitionSizes.size() < 2) {
			return 0;
		}
		int[] sizes = partitionSizes.stream().mapToInt(i -> i).toArray();
		int result = 0;
		for (int i = 0; i < sizes.length - 1; i++) {
			result += sizes[i] * sizes[i + 1];
			sizes[i + 1] = sizes[i] + sizes[i + 1];
		}
		return result;
	}

}

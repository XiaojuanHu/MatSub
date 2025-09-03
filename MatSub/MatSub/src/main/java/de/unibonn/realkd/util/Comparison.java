/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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

import java.util.Comparator;

/**
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class Comparison {

	private Comparison() {
		;
	}

	public static <T> T min(Comparator<? super T> order, T a, T b) {
		return order.compare(a, b) <= 0 ? a : b;
	}

	public static boolean equalsIncludingNaN(double a, double b) {
		return ((Double.isNaN(a) && Double.isNaN(b)) || a == b);
	}
	
	public static boolean greaterThanOrSecondNaN(double a, double b) {
		return Double.isNaN(b) || a > b;
	}
	
	/**
	 * Double comparison with NaN values being smallest (in contrast to standard
	 * {@link Double#compare(double,double)} where {@code NaN} is the largest
	 * possible value).
	 * 
	 * @param a
	 *            the first double to compare
	 * @param b
	 *            the second double to compare
	 * 
	 * @return {@literal 0} if {@code a==b} OR {@code a==b==NaN}, {@literal -1} if
	 *         {@code a<b} OR a==NaN, {@literal 1} if {@code a>b} OR {@code b==NaN}
	 * 
	 */
	public static int compareNanSmallest(double a, double b) {
		if (Double.isNaN(a) && Double.isNaN(b)) {
			return 0;
		} else if (Double.isNaN(a)) {
			return -1;
		} else if (Double.isNaN(b)) {
			return 1;
		}
		return Double.compare(a, b);
	}
	
	/**
	 * Flipped comparison with NaN values being largest (in contrast to flipping
	 * standard {@link Double#compare(double,double)} which would result in NaN
	 * being smallest).
	 * 
	 * @param a
	 *            the first double to compare
	 * @param b
	 *            the second double to compare
	 * 
	 * @return {@literal 0} if {@code a==b} OR {@code a==b==NaN}, {@literal -1} if {@code a>b} OR
	 *         {@code b==NaN}, @{literal 1} if {@code a<b} OR {@code a==NaN}
	 * 
	 */
	public static int flippedCompareNanLast(double a, double b) {
		if (Double.isNaN(a) && Double.isNaN(b)) {
			return 0;
		} else if (Double.isNaN(a)) {
			return 1;
		} else if (Double.isNaN(b)) {
			return -1;
		}
		return Double.compare(b, a);
	}

}

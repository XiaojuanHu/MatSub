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

import java.util.function.Predicate;

/**
 * Factory methods for often used predicates.
 * 
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.1
 *
 */
public class Predicates {

	private Predicates() {
		;
	}

	public static <T> Predicate<T> notNull() {
		return v -> v != null;
	}

	public static <T> Predicate<T> satisfied() {
		return v -> true;
	}
	
	public static <T> Predicate<T> notSatisfied() {
		return v -> false;
	}

	public static <U extends Comparable<U>> Predicate<U> largerThan(U threshold) {
		return v -> threshold.compareTo(v) < 0;
	}

	public static <U extends Comparable<U>> Predicate<U> inOpenRange(U lower, U upper) {
		return v -> lower.compareTo(v) < 0 && upper.compareTo(v) > 0;
	}

	public static <U extends Comparable<U>> Predicate<U> inClosedRange(U lower, U upper) {
		return v -> lower.compareTo(v) <= 0 && upper.compareTo(v) >= 0;
	}

}

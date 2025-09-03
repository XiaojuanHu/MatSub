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
package de.unibonn.realkd.common.testing;

import java.util.Random;
import java.util.function.Supplier;

/**
 * <p>
 * Singleton that provides a single source of randomness and avoids
 * code-duplication for randomized tests.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.1.2.1
 *
 */
public final class TestRandomSupplier implements Supplier<Random> {

	private static final int SEED = 17;

	private static final Random RANDOM = new Random(SEED);

	public static final TestRandomSupplier INSTANCE = new TestRandomSupplier();

	private TestRandomSupplier() {
		;
	}

	public Random get() {
		return RANDOM;
	}

}

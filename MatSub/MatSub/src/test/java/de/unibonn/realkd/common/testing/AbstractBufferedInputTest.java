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

import java.util.function.Supplier;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * <p>
 * Provides buffering mechanism for parameterized tests that have expensive
 * input setup-costs (which thus should not be repeated for every test method).
 * Extending test classes should be parameterized with a supplier of the actual
 * input type. Then this base-class assures that each supplier computes its
 * input only once per test class. This is achieved by using a static field as
 * buffer for test input.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.1.2.1
 * 
 * @see Parameterized
 * 
 * @param T
 *            the type of the actual input needed by test
 *
 */
@RunWith(Parameterized.class)
public abstract class AbstractBufferedInputTest<T> {

	private static Object INPUT_BUFFER;

	private static Object PROVIDER_OF_BUFFERED_INPUT;

	private T currentInput;

	@SuppressWarnings("unchecked")
	public AbstractBufferedInputTest(Supplier<T> inputSuppler) {
		if (PROVIDER_OF_BUFFERED_INPUT != inputSuppler) {
			INPUT_BUFFER = inputSuppler.get();
			PROVIDER_OF_BUFFERED_INPUT = inputSuppler;
		}
		currentInput = (T) INPUT_BUFFER;
	}

	/**
	 * 
	 * @return the input to be used by test methods
	 */
	public T getCurrentInput() {
		return currentInput;
	}

}

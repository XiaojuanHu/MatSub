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
package edu.uab.cftp.sampling.distribution.tool;

import java.util.BitSet;

import com.google.common.collect.ImmutableMap;

import ua.ac.be.mime.mining.TidList;

/**
 * @author Sandy Moens
 *
 * @since 0.6.0
 * 
 * @version 0.6.0
 * 
 */
public class BiasTest {
	
	protected static final double PRECISION = 0.00000000001;
	
	private StarOperation starOperation;

	protected BiasTest(StarOperation starOperation) {
		this.starOperation = starOperation;
		
	}

	protected Bias bias() {
		return Biases.bias(starOperation, 1);
	}
	
	protected Bias biasWithItemBiases() {
		return Biases.bias(starOperation, 1, ImmutableMap.of(0, 5.0, 1, 1.5, 2, 0.5, 3, 0.1, 4, 3.5));
	}
	
	protected TidList tidList_of_zero() {
		return new TidList();
	}
	
	protected TidList tidList_of_one() {
		BitSet bitSet = new BitSet(1);
		bitSet.set(0);
		
		return new TidList(bitSet);
	}
	
	protected TidList tidList_of_three() {
		BitSet bitSet = new BitSet(3);
		bitSet.set(0);
		bitSet.set(1);
		bitSet.set(2);
		
		return new TidList(bitSet);
	}
	
	protected TidList tidList_of_five() {
		BitSet bitSet = new BitSet(5);
		bitSet.set(0);
		bitSet.set(1);
		bitSet.set(2);
		bitSet.set(3);
		bitSet.set(4);
		
		return new TidList(bitSet);
	}
	
}

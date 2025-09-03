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

import ua.ac.be.mime.mining.TidList;

/**
 * This interface provides some basic functions for a bias computer. A bias
 * computer computes the bias of the itemsets that can be generated given the
 * current solution (i.e. already accepted and rejected items) and the items
 * that are still available
 * 
 * @author Sandy Moens
 * @since 0.6.0
 * @version 0.6.0
 */
public interface Bias {

	public double inducedSetsBias(TidList tidList, TidList[] negativeTidList,
			TidList X, TidList Y);

	public double inducedSetsBiasConditioned(TidList tidList,
			TidList[] negativeTidList, TidList X, TidList Y, int conditionIndex);

	public double powerSetBias(TidList tidList);

	public double singletonsBias(TidList tidList);

	public double emptySetBias();

	public double getWeight(TidList posIntersection, TidList[] negativeParts);

	public double getWeight(TidList tidList, TidList[] negativeTidList,
			TidList X, TidList Y, TidList difference);

	public double getConditionedWeight(TidList tidList,
			TidList[] negativeTidList, TidList X, TidList Y, int conditionIndex);
	
}
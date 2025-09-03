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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Sandy Moens
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public class AdditiveBiasNoSingletonsBPITest extends BiasTest {

	public AdditiveBiasNoSingletonsBPITest() {
		super(StarOperation.ADDITIVE);
	}
	
	@Test
	public void powerSetBiasTest() {
		{
			Bias bias = bias();
			
			assertEquals(0.0, bias.powerSetBias(null), PRECISION);
			assertEquals(0.0, bias.powerSetBias(tidList_of_zero()), PRECISION);
			assertEquals(1.0, bias.powerSetBias(tidList_of_one()), PRECISION);
			assertEquals(12.0, bias.powerSetBias(tidList_of_three()), PRECISION);
			assertEquals(80.0, bias.powerSetBias(tidList_of_five()), PRECISION);
		}
		
		{
			Bias bias = biasWithItemBiases();
			
			assertEquals(0.0, bias.powerSetBias(null), PRECISION);
			assertEquals(0.0, bias.powerSetBias(tidList_of_zero()), PRECISION);
			assertEquals(5.0, bias.powerSetBias(tidList_of_one()), PRECISION);
			assertEquals(28.0, bias.powerSetBias(tidList_of_three()), PRECISION);
			assertEquals(169.6, bias.powerSetBias(tidList_of_five()), PRECISION);
		}
	}
	
	@Test
	public void singletonsBiasTest() {
		{
			Bias bias = bias();
			
			assertEquals(0.0, bias.singletonsBias(null), PRECISION);
			assertEquals(0.0, bias.singletonsBias(tidList_of_zero()), PRECISION);
			assertEquals(1.0, bias.singletonsBias(tidList_of_one()), PRECISION);
			assertEquals(3.0, bias.singletonsBias(tidList_of_three()), PRECISION);
			assertEquals(5.0, bias.singletonsBias(tidList_of_five()), PRECISION);
		}
		
		{
			Bias bias = biasWithItemBiases();
			
			assertEquals(0.0, bias.singletonsBias(null), PRECISION);
			assertEquals(0.0, bias.singletonsBias(tidList_of_zero()), PRECISION);
			assertEquals(5.0, bias.singletonsBias(tidList_of_one()), PRECISION);
			assertEquals(7.0, bias.singletonsBias(tidList_of_three()), PRECISION);
			assertEquals(10.6, bias.singletonsBias(tidList_of_five()), PRECISION);
		}
	}
	
	@Test
	public void emptySetBiasTest() {
		{
			Bias bias = bias();
			
			assertEquals(0.0, bias.emptySetBias(), PRECISION);
		}
		
		{
			Bias bias = biasWithItemBiases();
			
			assertEquals(0.0, bias.emptySetBias(), PRECISION);
		}
	}
	
}

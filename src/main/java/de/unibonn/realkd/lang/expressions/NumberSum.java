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
package de.unibonn.realkd.lang.expressions;

import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.lang.types.NumericValue;
import de.unibonn.realkd.lang.types.Types;

public class NumberSum implements NumberExpression {

	private final Expression<NumericValue> left;

	private final Expression<NumericValue> right;

	public NumberSum(Expression<NumericValue> left, Expression<NumericValue> right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public NumericValue evaluate(Workspace workspace) throws InterpretationException {
		return Types.numericValue(left.evaluate(workspace).asDouble() + right.evaluate(workspace).asDouble());
	}

	@Override
	public Class<NumericValue> resultType() {
		return NumericValue.class;
	}

}
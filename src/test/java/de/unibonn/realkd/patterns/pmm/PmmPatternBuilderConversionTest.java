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
package de.unibonn.realkd.patterns.pmm;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.unibonn.realkd.common.testing.AbstractBuilderConversionTest;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.table.DataFormatException;
import de.unibonn.realkd.patterns.Pattern;

/**
 * @author Mario
 *
 */
@RunWith(Parameterized.class)
public class PmmPatternBuilderConversionTest extends
		AbstractBuilderConversionTest<Pattern<?>> {

	/*
	 * WARNING: do not use "{1}" for identifier generation as parameter for the
	 * annotation. This results in the test runner not terminating. The reason
	 * is perhaps the multi-line string representation of patterns
	 */
	@Parameters
	public static Iterable<Object[]> getData() throws DataFormatException {
		return PMMTestInputs.getPmmPatternTestInput();
	}

	public PmmPatternBuilderConversionTest(Workspace context, Pattern<?> object) {
		super(context, object);
	}

}

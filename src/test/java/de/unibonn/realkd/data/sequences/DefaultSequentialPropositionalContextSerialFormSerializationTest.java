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
package de.unibonn.realkd.data.sequences;

import static de.unibonn.realkd.data.sequences.SequentialPropositionalContextTestInputs.getDateSequentialPropositionalContextTestInput;
import static de.unibonn.realkd.data.sequences.SequentialPropositionalContextTestInputs.getOrdinalSequentialPropositionalContextTestInput;

import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.unibonn.realkd.common.testing.AbstractJsonSerializationTest;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.table.DataFormatException;

/**
 * @author Sandy Moens
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
@RunWith(Parameterized.class)
public class DefaultSequentialPropositionalContextSerialFormSerializationTest
		extends AbstractJsonSerializationTest<SerialForm<PropositionalContext>> {

	@Parameters
	public static Iterable<Object[]> getData() throws DataFormatException {
		List<Object[]> data = Lists.newArrayList();
		data.addAll(ImmutableList.copyOf(getOrdinalSequentialPropositionalContextTestInput()));
		data.addAll(ImmutableList.copyOf(getDateSequentialPropositionalContextTestInput()));
		return data;
	}

	public DefaultSequentialPropositionalContextSerialFormSerializationTest(Workspace workspace, DefaultSequentialPropositionalContext context) {
		super(context.serialForm(), ImmutableList.of(SerialForm.class));
	}

}

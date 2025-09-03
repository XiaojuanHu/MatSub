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
package de.unibonn.realkd.lang;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.propositions.PropositionalContextFromTableBuilder;
import de.unibonn.realkd.data.propositions.PropositionalizationRule;
import de.unibonn.realkd.lang.expressions.Constant;
import de.unibonn.realkd.lang.types.NumericValue;
import de.unibonn.realkd.lang.types.Types;

/**
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class BuiltInConstants implements ConstantProvider {

	public static final Constant<NumericValue> PI = new Constant<>("PI", Types.numericValue(Math.PI));

	public static final Constant<NumericValue> E = new Constant<>("E", Types.numericValue(Math.E));

	public static final Constant<Set<PropositionalizationRule>> DEFAULT_MAPPERS = new Constant<>(
			"DFT_ATTR_TO_STMNT_MAPS", PropositionalContextFromTableBuilder.DEFAULT_MAPPERS);

	public static final List<Constant<PropositionalizationRule>> MAPPERS = PropositionalContextFromTableBuilder.ALL_MAPPERS
			.stream().map(m -> new Constant<>(m.toString(), m)).collect(Collectors.toList());

	public static final ImmutableList<Constant<?>> ALL_SIMPLE = ImmutableList.of(PI, E, DEFAULT_MAPPERS);

	public static final List<Constant<?>> ALL = new ImmutableList.Builder<Constant<?>>().addAll(ALL_SIMPLE)
			.addAll(MAPPERS).build();

	@Override
	public List<Constant<?>> get() {
		return ALL;
	}

	@Override
	public String name() {
		return "built-in constants";
	}

}

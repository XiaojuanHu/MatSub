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
package de.unibonn.realkd.util;

import static java.util.stream.IntStream.range;

import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.unibonn.realkd.data.propositions.PropositionalContext;

/**
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class PropositionalLogicExport {

	public static Iterable<String> fimiIterable(PropositionalContext propositionalLogic) {
		return () -> {
			Stream<Set<Integer>> truthSets = StreamSupport
					.stream(propositionalLogic.population().objectIds().spliterator(), false)
					.map(i -> propositionalLogic.truthSet(i));
			return truthSets.map(truth -> {
				String string = truth.toString();
				return string.substring(1, string.length() - 1);
			}).iterator();
		};
	}

	public static Iterable<String> fimiMetaDataStream(PropositionalContext propositionalLogic) {
		return () -> range(0, propositionalLogic.propositions().size())
				.mapToObj(i -> i + ", " + propositionalLogic.proposition(i).name()).iterator();
		// return () -> propositionalLogic.propositions().stream().map(p ->
		// p.getId() + ", " + p.name()).iterator();
	}

}

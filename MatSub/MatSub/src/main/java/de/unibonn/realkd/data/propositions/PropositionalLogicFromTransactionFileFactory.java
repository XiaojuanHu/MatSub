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
package de.unibonn.realkd.data.propositions;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static de.unibonn.realkd.common.IndexSets.copyOf;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.Populations;

/**
 * Creates a propositional logic directly from a transactional data file.
 * 
 * @author Sandy Moens
 * 
 * @since 0.2.1
 * 
 * @version 0.2.1
 *
 */
public class PropositionalLogicFromTransactionFileFactory {

	private static final Logger LOGGER = Logger.getLogger(PropositionalLogicFromTransactionFileFactory.class.getName());

	public static DefaultPropositionalContext build(String filename) {
		return build(filename, ";");
	}
	public static DefaultPropositionalContext build(String filename, String delimiter) {
		LOGGER.fine("Compiling proposition list");

		Map<String, Integer> indexMap = newHashMap();
		List<List<Integer>> supportSets = newArrayList();
		List<String> objectNames = newArrayList();
		List<String> population = newArrayList();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			int tid = 0;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split(delimiter);
				for (String token : split) {
					Integer id = indexMap.get(token);
					if (id == null) {
						objectNames.add(token);
						id = supportSets.size();
						indexMap.put(token, id);
						supportSets.add(new ArrayList<>());
					}
					supportSets.get(id).add(tid);
				}
				population.add(String.valueOf(tid));
				tid++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<Proposition> propositions = IntStream.range(0, supportSets.size()).mapToObj(
				i -> Propositions.proposition(i, objectNames.get(i), copyOf(supportSets.get(i))))
				.collect(toList());

		LOGGER.info("Done compiling proposition list (" + supportSets.size() + " propositions added)");

		String name = "Statements about table 1";
		return new DefaultPropositionalContext(name, name,
				Populations.population(Identifier.id("population_of_" + name), "Population of " + name, "", population),
				propositions);
	}

}

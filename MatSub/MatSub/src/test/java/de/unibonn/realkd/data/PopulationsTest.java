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
package de.unibonn.realkd.data;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.data.Populations.population;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;

import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.common.JsonSerialization;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.common.workspace.Workspaces;
import de.unibonn.realkd.data.Populations.PopulationSerialForm;

/**
 * Test for populations created by {@link Populations}.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.7.0
 *
 */
public class PopulationsTest {

	private static final Population POPULATION_WITH_NAMES = population(id("Test"), "Test", "",
			ImmutableList.of("a", "b", "c"));

	private static final Population ANONYMOUS_POPULATION = population(id("Test"), "Test", "", 10);

	@Test
	public void testObjectNames() {
		assertEquals(ImmutableList.of("a", "b", "c"), POPULATION_WITH_NAMES.objectNames());
		assertEquals(10, ANONYMOUS_POPULATION.objectNames().size());
	}

	@Test
	public void testObjectIds() {
		assertEquals(IndexSets.copyOf(ImmutableList.of(0, 1, 2)), POPULATION_WITH_NAMES.objectIds());
		assertEquals(IndexSets.copyOf(ImmutableList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
				ANONYMOUS_POPULATION.objectIds());
	}

	@Test
	public void serializationTest() throws IOException {
		Workspace workspace = Workspaces.workspace();

		PopulationSerialForm serialFormWithNames = POPULATION_WITH_NAMES.serialForm();
		PopulationSerialForm anonymousSerialForm = ANONYMOUS_POPULATION.serialForm();

		String namedSerialString = JsonSerialization.serialString(serialFormWithNames);
		String anonymousSerialString = JsonSerialization.serialString(anonymousSerialForm);

		PopulationSerialForm restoredSerialFormWithNames = JsonSerialization.deserialization(namedSerialString,
				PopulationSerialForm.class);
		PopulationSerialForm restoredAnonymousSerialForm = JsonSerialization.deserialization(anonymousSerialString,
				PopulationSerialForm.class);

		Population restoredPopulationWithNames = restoredSerialFormWithNames.build(workspace);
		Population restoredAnonymousPopulation = restoredAnonymousSerialForm.build(workspace);

		new EqualsTester().addEqualityGroup(serialFormWithNames, restoredSerialFormWithNames)
				.addEqualityGroup(anonymousSerialForm, restoredAnonymousSerialForm).testEquals();

		new EqualsTester().addEqualityGroup(POPULATION_WITH_NAMES, restoredPopulationWithNames)
				.addEqualityGroup(ANONYMOUS_POPULATION, restoredAnonymousPopulation).testEquals();
	}

}

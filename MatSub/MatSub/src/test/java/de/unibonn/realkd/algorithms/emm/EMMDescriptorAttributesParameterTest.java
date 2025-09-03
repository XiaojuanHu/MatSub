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
package de.unibonn.realkd.algorithms.emm;

import static de.unibonn.realkd.data.Populations.population;
import static de.unibonn.realkd.data.table.DataTables.table;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.algorithms.common.MiningParameters;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.SubCollectionParameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.common.workspace.Workspaces;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.propositions.PropositionalContextFromTableBuilder;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.data.table.attribute.TestAttributeFactory;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroups;

/**
 * Test case for EMM descriptor attribute parameter (produced by
 * {@link EMMParameters#getEMMDescriptorAttributesParameter} ) with 5
 * attributes, two of which (4 and 5) form a joint macro attribute.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 *
 */
public class EMMDescriptorAttributesParameterTest {

	private static final int NUMBER_OF_ENTITIES_IN_TEST_DATA = 10;

	private static final int RANDOM_SEED = 8;

	private Workspace workspace;

	private TestAttributeFactory attributeFactory = new TestAttributeFactory(new Random(RANDOM_SEED));

	private Parameter<DataTable> dataTableParameter;

	private Parameter<List<Attribute<?>>> targetAttributeParameter;

	private SubCollectionParameter<Attribute<?>, Set<Attribute<?>>> testParameter;

	private MetricAttribute attribute5;

	private MetricAttribute attribute4;

	private MetricAttribute attribute3;

	private CategoricAttribute<String> attribute2;

	private CategoricAttribute<String> attribute1;

	@Before
	public void setUp() {
		this.workspace = Workspaces.workspace();

		attribute1 = attributeFactory.getCategoricalAttributeWithUniformNonRedundantCategories("Attribute 1",
				Arrays.asList("Category 1", "Category 2"), NUMBER_OF_ENTITIES_IN_TEST_DATA);
		attribute2 = attributeFactory.getCategoricalAttributeWithUniformNonRedundantCategories("Attribute 2",
				Arrays.asList("Category 1", "Category 2"), NUMBER_OF_ENTITIES_IN_TEST_DATA);
		attribute3 = attributeFactory.getMetricAttributeWithUniformValues("Attribute 3", 1,
				NUMBER_OF_ENTITIES_IN_TEST_DATA);
		attribute4 = attributeFactory.getMetricAttributeWithUniformValues("Attribute 4", 1,
				NUMBER_OF_ENTITIES_IN_TEST_DATA);
		attribute5 = attributeFactory.getMetricAttributeWithUniformValues("Attribute 5", 1,
				NUMBER_OF_ENTITIES_IN_TEST_DATA);

		List<Attribute<?>> attributes = ImmutableList.of(attribute1, attribute2, attribute3, attribute4, attribute5);

		List<String> entityNames = new ArrayList<String>();
		for (int i = 0; i < NUMBER_OF_ENTITIES_IN_TEST_DATA; i++) {
			entityNames.add("Entity " + i);
		}

//		AttributeGroupStore attributeGroupStore = new AttributeGroupStore(ImmutableList
//				.of(new JointMacroAttribute("Group of attributes 4 and 5", ImmutableList.of(attribute4, attribute5))));
		// attributeGroupStore.addAttributeGroup(
		// );

		Population population = population(Identifier.id("Entities"), "Entities", "", entityNames);

		DataTable dataTable = table(Identifier.id("testTable"), "Test data table", "Contains random data.",
				population, attributes, ImmutableList
				.of(AttributeGroups.functionalGroup("Group of attributes 4 and 5", ImmutableList.of(attribute4, attribute5))));

		workspace.add(population);
		workspace.add(dataTable);
		workspace.add(new PropositionalContextFromTableBuilder().apply(dataTable));

		dataTableParameter = MiningParameters.dataTableParameter(workspace);
		targetAttributeParameter = EMMParameters.getEMMTargetAttributesParameter(dataTableParameter);
		testParameter = EMMParameters.getEMMDescriptorAttributesParameter(dataTableParameter, targetAttributeParameter);
	}

	@Test
	public void testNotNull() {
		assertNotNull("EMM descriptor proposition parameter was not created.", testParameter);
	}

	@Test
	public void testDependency() {
		assertEquals("Must depend on datatable parameter and target attribute parameter",
				ImmutableSet.of(dataTableParameter, targetAttributeParameter),
				ImmutableSet.copyOf(testParameter.getDependsOnParameters()));

	}

	@Test
	public void testIsContextValud() {
		assertFalse("Context must be not valid is long as upstream parameters not set.",
				testParameter.isContextValid());

		targetAttributeParameter.set(Arrays.asList(attribute4));

		assertTrue("Context be valid when all upstream parameters set.", testParameter.isContextValid());
	}

	@Test
	public void testGetCollection() {
		targetAttributeParameter.set(Arrays.asList(attribute4));
		assertEquals(ImmutableSet.of(attribute1, attribute2, attribute3), testParameter.getCollection());
	}

	@Test
	public void testInitialization() {
		// test first initialization
		targetAttributeParameter.set(Arrays.asList(attribute4));
		assertEquals(ImmutableSet.of(attribute1, attribute2, attribute3), testParameter.getCollection());
		assertEquals(ImmutableSet.of(), testParameter.current());

		// test re-initialization
		targetAttributeParameter.set(Arrays.asList(attribute3));
		assertEquals(ImmutableSet.of(attribute1, attribute2, attribute4, attribute5), testParameter.getCollection());
	}

	@Test
	public void testIsValid() {
		assertFalse("Must be not valid is long as upstream parameters not set.", testParameter.isValid());
	}

}

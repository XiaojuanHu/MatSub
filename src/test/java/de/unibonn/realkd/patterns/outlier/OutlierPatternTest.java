package de.unibonn.realkd.patterns.outlier;

import static de.unibonn.realkd.common.IndexSets.copyOf;
import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.base.Identifier.identifier;
import static de.unibonn.realkd.data.Populations.population;
import static de.unibonn.realkd.data.table.DataTables.table;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.DefaultAttribute;

/**
 * Test for basic outlier object consistency.
 * 
 * @author Sebastian Bothe
 *
 * @since 0.1.2
 * 
 * @version 0.1.2
 */
public class OutlierPatternTest {

	private IndexSet rows1;
	private IndexSet rows2;
	private Set<Attribute<?>> attributes;
	private Set<Attribute<?>> attributes2;
	private DataTable table;

	@Before
	public void setUp() {

		// Prepare two rowsets row1 \subset row2
		rows1 = copyOf(Arrays.asList(new Integer[] { 1, 2, 3 }));
		rows2 = copyOf(Arrays.asList(new Integer[] { 1, 2, 3, 4 }));

		DefaultAttribute<String> attr1 = new DefaultAttribute<String>(identifier("ATTR_NAME_1"), "ATTR_NAME_1",
				"DESCRIPTION_1", Arrays.asList("VALUE11", "VALUE2"), String.class);

		DefaultAttribute<String> attr2 = new DefaultAttribute<String>(identifier("ATTR_NAME_2"), "ATTR_NAME_2",
				"DESCRIPTION_2", Arrays.asList("VALUE11", "VALUE2"), String.class);

		attributes = new HashSet<>();
		attributes.add(attr1);

		attributes2 = new HashSet<>();
		attributes2.addAll(attributes);
		attributes2.add(attr2);

		Population population = population(id("Population"), 5);
		table = table(id("Table"), "", "", population, ImmutableList.copyOf(attributes));
	}

	@Test
	public void testEqualsDifferentSupportedSetSameAttributes() {
		Outlier a = new Outlier(table, rows1, attributes, 0f, 0f);
		Outlier b = new Outlier(table, rows2, attributes, 0f, 0f);

		// Must match identity
		assertTrue(a.equals(a));
		assertTrue(b.equals(b));

		// and not equal for different rowsets
		assertTrue(!a.equals(b));
		assertTrue(!b.equals(a));
	}

	@Test
	public void testEqualsDifferentAttributes() {
		Outlier a = new Outlier(table, rows1, attributes, 0f, 0f);
		Outlier b = new Outlier(table, rows1, attributes2, 0f, 0f);

		// Must match identity
		assertTrue(a.equals(a));
		assertTrue(b.equals(b));

		// and not equal for different attributes
		assertTrue(!a.equals(b));
		assertTrue(!b.equals(a));
	}

}

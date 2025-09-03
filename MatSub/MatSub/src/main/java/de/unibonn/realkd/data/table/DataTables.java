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
package de.unibonn.realkd.data.table;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.unibonn.realkd.data.table.attribute.Attributes.orderedCategoricAttribute;
import static de.unibonn.realkd.util.Search.findSmallest;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntPredicate;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.OrderedCategoricAttribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroup;

/**
 * @author Mario Boley
 * 
 * @since 0.3.1
 * @version 0.3.1
 *
 */
public class DataTables {

	private DataTables() {
		; // not to be instantiated
	}

	public static DataTable table(Identifier id, String name, String description, Population population,
			List<Attribute<?>> attributes, List<AttributeGroup> attributeGroups) {
		checkNotNull(name);
		checkNotNull(population);
		checkNotNull(attributes);
		checkNotNull(description);
		return new DefaultDataTable(id, name, description, population, attributes, attributeGroups);
	}

	public static DataTable table(Identifier id, String name, String description, Population population,
			List<Attribute<?>> attributes) {
		return table(id, name, description, population, attributes, ImmutableList.of());
	}

	/**
	 * <p>
	 * Creates a new datatable based on another table by filtering out
	 * attributes. Importantly, the serialized form of the new datatable is
	 * incremental and depends on the old datatable.
	 * </p>
	 * <p>
	 * All attribute groups will be filtered if they do not contain only
	 * attributes present in the new datatable.
	 * </p>
	 * 
	 * @param id
	 *            the id of the new datatable
	 * @param name
	 *            the name of the new datatable
	 * @param description
	 *            the description of the new datatable
	 * @param originalTable
	 *            the table on which the new datatable will be based on
	 * @param hiddenAttributeNames
	 *            the names of the attributes to be filtered out
	 * 
	 * @return A new datatable based on the original datatable without the
	 *         specified set of attributes
	 */
	public static DataTable projectedTable(Identifier id, String name, String description, DataTable originalTable,
			Collection<String> hiddenAttributeNames) {
		checkNotNull(name);
		checkNotNull(description);
		checkNotNull(originalTable);
		checkNotNull(hiddenAttributeNames);
		return new ProjectedDataTable(id, name, description, originalTable, hiddenAttributeNames);
	}

	public static void test() {
		DataTable testTable = null;
		discretization(testTable, equalFrequencyDiscretization(5));
	}

	// public static DataTable imputeMissing(DataTable dataTable)
	// {
	//
	//
	//
	// }

	public static DiscreteDataTable discretization(DataTable dataTable,
			Function<? super OrdinalAttribute<?>, ? extends CategoricAttribute<?>> ordinalToCategoric) {
		List<? extends Attribute<?>> listOfAttributes = dataTable.attributes();
		List<CategoricAttribute<?>> newListOfAttributes = new ArrayList<>();
		for (Attribute<?> attr : listOfAttributes) {
			if (attr instanceof CategoricAttribute<?>) {
				newListOfAttributes.add((CategoricAttribute<?>) attr);
			} else if (attr instanceof OrdinalAttribute<?>) {
				newListOfAttributes.add(ordinalToCategoric.apply((OrdinalAttribute<?>) attr));
			}
		}
		return new DiscreteDataTableImplementation(Identifier.id(dataTable.identifier() + "_discretized"), dataTable.caption(),
				dataTable.description(), dataTable.population(), newListOfAttributes, ImmutableList.of());
	}

	public static Function<OrdinalAttribute<?>, OrderedCategoricAttribute<Integer>> equalFrequencyDiscretization(
			int numBins) {
		return o -> {
			double stepSize = 1.0 / numBins;
			// get the quantiles
			List<?> distinctQuantiles = rangeClosed(1, numBins).mapToDouble(i -> i * stepSize)
					.mapToObj(q -> o.quantile(q)).distinct().collect(toList());

			// find the bin index for every value
			Function<Object, Integer> binIndex = v -> {
				IntPredicate vLessOrEqualQuantile = i -> o.lessOrEqual(v, distinctQuantiles.get(i));
				return findSmallest(0, distinctQuantiles.size() - 1, vLessOrEqualQuantile) + 1;
			};

			List<Integer> newValues = new ArrayList<>(); // the new values
			for (int i = 0; i <= o.maxIndex(); i++) {
				Optional<?> value = o.getValueOption(i);
				newValues.add(value.map(binIndex).orElse(null));
			}

			return orderedCategoricAttribute(distinctQuantiles.size() + " bins " + o.caption(),
					o.description() + "\n Discretized into " + distinctQuantiles.size() + " equal frequency bins.",
					newValues, Integer.class);
		};
	};

}

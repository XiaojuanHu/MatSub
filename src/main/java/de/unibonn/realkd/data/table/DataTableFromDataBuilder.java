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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.Populations;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.Attributes;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;

/**
 * @author Janis Kalofolias
 * 
 * @since 0.5.1
 * 
 * @version 0.5.1
 *
 */
public class DataTableFromDataBuilder {
	protected final List<Attribute<?>> attributes;
	protected Identifier identifier;
	protected String name;
	protected String description;
	protected Population population;

	public enum AttributeType {
		METRIC{
			public DataTableFromDataBuilder addAttribute(DataTableFromDataBuilder builder, String identifier, String name, String description, List<?> values) {
				@SuppressWarnings("unchecked")
				List<Double> dblValues = (List<Double>) values;
				return builder.addMetricAttribute(identifier, name, description, dblValues);
			}
		},
		INTEGERCATEGORICAL{
			public DataTableFromDataBuilder addAttribute(DataTableFromDataBuilder builder, String identifier, String name, String description, List<?> values) {
				@SuppressWarnings("unchecked")
				List<Integer> intValues = (List<Integer>) values;
				return builder.addIntegerAttribute(identifier, name, description, intValues, true);
			}
		},
		INTEGERORDINAL{
			public DataTableFromDataBuilder addAttribute(DataTableFromDataBuilder builder, String identifier, String name, String description, List<?> values) {
				@SuppressWarnings("unchecked")
				List<Integer> intValues = (List<Integer>) values;
				return builder.addIntegerAttribute(identifier, name, description, intValues, false);
			}
		},
		CATEGORICAL{
			public DataTableFromDataBuilder addAttribute(DataTableFromDataBuilder builder, String identifier, String name, String description, List<?> values) {
				@SuppressWarnings("unchecked")
				List<String> strValues = (List<String>) values;
				return builder.addCategoricalAttribute(identifier, name, description, strValues);
			}
		};
		public abstract DataTableFromDataBuilder addAttribute(DataTableFromDataBuilder builder, String identifier, String name, String description, List<?> values);
		}
	
	/**
	 * 
	 */
	public DataTableFromDataBuilder() {
		attributes = new ArrayList<Attribute<?>>();
		name = "DataTable";
		description = "DataTable from Data";
		identifier = Identifier.id("DataTable");
	}
	
	public DataTableFromDataBuilder addAttribute(Attribute<?> attribute) {
		attributes.add(attribute);
		return this;
	}
	public DataTableFromDataBuilder addAttribute(String identifier, String name, String description, AttributeType type, List<?> values) {
		type.addAttribute(this,identifier, name, description, values);
		return this;
	}
	@SuppressWarnings("rawtypes")
	public DataTableFromDataBuilder addAttribute(String identifier, String name, String description, AttributeType type, Object valueArray) {
		List values = valuesFromArray(valueArray);
		type.addAttribute(this,identifier, name, description, values);
		return this;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DataTableFromDataBuilder addAttribute(String identifier, String name, String description, AttributeType type, Object valueArray, Object missing) {
		List values = valuesFromArray(valueArray);
		if(missing instanceof boolean[]) {
			boolean[] isMissing = (boolean[]) missing;
			setMissingLogical(values, isMissing);
		} else if(missing instanceof int[]) {
			int[] indexMissing = (int[]) missing;
			setMissingIndex(values, indexMissing);
		} else {
			throw new RuntimeException("Missing value specification must either be an int array of the missing indices or a boolean array specifying whether each entry is missing.");
		}
		type.addAttribute(this,identifier, name, description, values);
		return this;
	}
	public DataTableFromDataBuilder addAttributes(String[] identifiers, String[] names, String[] descriptions, String[] types, Object[] valueArrays, Object[] missing) {
		int numAttrs = identifiers.length;
		assert names.length == numAttrs &&
				descriptions.length == numAttrs  &&
				valueArrays.length == numAttrs  &&
				missing.length == numAttrs: "Length mismatch: all entries must be of equal size.";
		for(int ai=0; ai<numAttrs; ++ai) {
			String stype = types[ai];
			AttributeType etype = AttributeType.valueOf(stype.toUpperCase());
			if(etype == null) {
				String known = Stream.of(AttributeType.values())
						.map(AttributeType::toString)
						.collect(Collectors.joining(",","\"","\""));
				throw new RuntimeException("Type "+stype + " is not a known attribute type. Known types are: ["+known+"].");
			}
			addAttribute(identifiers[ai], names[ai], descriptions[ai], etype, valueArrays[ai], missing[ai]);
		}
		return this;
	}
	public DataTableFromDataBuilder addMetricAttribute(String identifier, String name, String description, List<Double> values) {
		final Identifier id = Identifier.identifier(identifier);
		Attribute<?> attribute = Attributes.metricDoubleAttribute(id, name, description, values);
		addAttribute(attribute);
		return this;
	}
	public DataTableFromDataBuilder addIntegerAttribute(String identifier, String name, String description, List<Integer> values,
			boolean categorical) {
		final Identifier id = Identifier.identifier(identifier);
		OrdinalAttribute<Integer> attribute = categorical
				? Attributes.orderedCategoricAttribute(id, name, description, values, Integer.class)
				: Attributes.ordinalAttribute(id, name, description, values, Integer.class);
		addAttribute(attribute);
		return this;
	}
	public DataTableFromDataBuilder addCategoricalAttribute(String identifier, String name, String description, List<String> values) {
		final Identifier id = Identifier.identifier(identifier);
		CategoricAttribute<String> attribute = Attributes.categoricalAttribute(id, name, description, values);
		addAttribute(attribute);
		return this;
	}

	public DataTableFromDataBuilder describeDataTable(Identifier identifier, String name, String description) {
		this.identifier = identifier;
		this.name = name;
		this.description = description;
		return this;
	}

	public DataTableFromDataBuilder makePopulation(Identifier identifier, String name, String description,
			List<String> entityNames) {
		population = Populations.population(identifier, name, description, entityNames);
		return this;
	}

	public DataTableFromDataBuilder makePopulation(Identifier identifier, String name, String description) {
		List<String> entityNames = attributes.stream().map(a -> identifier.toString()).collect(Collectors.toList());
		return makePopulation(identifier, name, description, entityNames);
	}

	public DataTableFromDataBuilder makePopulation(String description) {
		return makePopulation(defaultPopulationIdentifier(), defaultPopulationName(), description);
	}

	public DataTableFromDataBuilder makePopulation() {
		return makePopulation(defaultPopulationDescription());
	}

	public DataTableFromDataBuilder setEntityNames(List<String> entityNames) {
		return makePopulation(defaultPopulationIdentifier(), defaultPopulationName(), defaultPopulationDescription(),
				entityNames);
	}

	private Identifier defaultPopulationIdentifier() {
		if (population == null) {
			return identifier == null ? Identifier.id("Population") : Identifier.id("Population_of_" + identifier);
		} else {
			return population.identifier();
		}
	}

	private String defaultPopulationName() {
		if (population == null) {
			return name == null ? "Population" : "Population_of_" + identifier;
		} else {
			return population.caption();
		}
	}

	private String defaultPopulationDescription() {
		if (population == null) {
			return name == null ? "Population" : "Population of " + identifier;
		} else {
			return population.description();
		}
	}

	public DataTable build() {
		if (population == null) {
			makePopulation();
		}
		DataTable dataTable = DataTables.table(identifier, name, description, population, attributes);
		return dataTable;
	}
	public static DataTable build(Identifier identifier, String name, String description, 
			String[] attributeIdentifiers, String[] attributeNames, String[] attributeDescriptions,
			String[] attributeTypes, Object[] attributeValueArrays, Object[] attributeMissing) {
		return new DataTableFromDataBuilder()
				.describeDataTable(identifier, name, description)
				.addAttributes(attributeIdentifiers, attributeNames, attributeDescriptions,
						attributeTypes, attributeValueArrays, attributeMissing)
				.build();
	}
	
	public static <T> void setMissingLogical(List<T> value, List<Boolean> isMissing) {
		assert isMissing.size()==value.size(): "Arguments must have the same size.";
		for(int i=0;i<isMissing.size();++i) {
			if(isMissing.get(i)) {
				value.set(i,  null);
			}
		}
	}
	public static <T> void setMissingLogical(List<T> value, boolean[] isMissing) {
		assert isMissing.length==value.size(): "Arguments must have the same size.";
		for(int i=0;i<isMissing.length;++i) {
			if(isMissing[i]) {
				value.set(i,  null);
			}
		}
	}
	public static <T> void setMissingIndex(List<T> value, List<Integer> indexMissing) {
		for(int i : indexMissing) {
			value.set(i,  null);
		}
	}
	public static <T> void setMissingIndex(List<T> value, int[] indexMissing) {
		for(int i : indexMissing) {
			value.set(i,  null);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static List valuesFromArray(Object value) {
		if (value instanceof int[]) {
			return IntStream.of((int[]) value).boxed().collect(Collectors.toList());
		} else if (value instanceof double[]) {
			return DoubleStream.of((double[]) value).boxed().collect(Collectors.toList());
		} else if (value instanceof String[]) {
			return Arrays.asList((String[]) value);
		} else {
			throw new RuntimeException("Class " + value.getClass().getName() + " is not supported.");
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List valuesFromArray(Object values, boolean[] isMissing) {
		List lstValues = valuesFromArray(values);
		setMissingLogical(lstValues, isMissing);
		return lstValues;
	}
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static List valuesFromArray(Object values, int[] indexMissing) {
		List lstValues = valuesFromArray(values);
		setMissingIndex(lstValues, indexMissing);
		return lstValues;
	}
}

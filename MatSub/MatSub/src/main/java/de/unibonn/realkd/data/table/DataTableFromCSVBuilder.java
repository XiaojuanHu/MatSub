/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
 */

package de.unibonn.realkd.data.table;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.unibonn.realkd.common.base.Identifier.identifier;
import static de.unibonn.realkd.data.table.attribute.Attributes.metricDoubleAttribute;
import static de.unibonn.realkd.data.table.attribute.Attributes.orderedCategoricAttribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.Populations;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.Attributes;
import de.unibonn.realkd.data.table.attribute.DefaultAttribute;
import de.unibonn.realkd.data.table.attribute.FiniteOrder;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroup;

/**
 * Builder that creates a DataTable object from raw csv data.
 * 
 * @author Björn Jacobs
 * 
 * @since 0.1.0
 * 
 * @version 0.5.0
 * 
 */
public class DataTableFromCSVBuilder {

	private static final Logger LOGGER = Logger.getLogger(DataTableFromCSVBuilder.class.getName());

	private static final int ATTRIBUTE_NAME_COLUMN = 0;
	private static final int ATTRIBUTE_TYPE_COLUMN = 1;
	private static final int ATTRIBUTE_DESCRIPTION_COLUMN = 2;
	private static final int ATTRIBUTE_VALUES_SPECIFICATION = 3;

	private static final String ATTRIBUTE_VALUES_SPECIFICATION_DELIMITER = ",";

	private static final String TYPE_METRIC = "numeric";
	private static final String TYPE_ORDINAL = "ordinal";
	private static final String TYPE_INTEGER = "integer";
	private static final String TYPE_CATEGORICAL = "categoric";
	private static final String TYPE_NAME = "name";
	private static final String TYPE_DATE = "date";

	private static final char ATTRIBUTE_GROUP_FIELD_DELIMITER = ';';

	private static int idPrefix = 1;

	private Identifier id = Identifier.id("table" + idPrefix++);
	private Optional<String> name = Optional.empty();
	private String description = "";
	private char delimiter = ';';
	private String missingSymbol = "?";
	private String attributeCSV = null;
	private String attributeGroupCSV = "";
	private String dataCSV = null;
	private List<AttributesFromGroupMapper> groupMappers = ImmutableList.copyOf(AttributesFromGroupMapper.values());

	public DataTableFromCSVBuilder() {
		;
	}

	public DataTableFromCSVBuilder groupMappers(List<AttributesFromGroupMapper> groupMappers) {
		this.groupMappers = groupMappers;
		return this;
	}

	/**
	 * Id of the datatable to be build.
	 */
	public DataTableFromCSVBuilder id(Identifier id) {
		this.id = id;
		return this;
	}

	/**
	 * Name of the datatable to be build.
	 */
	public DataTableFromCSVBuilder name(String name) {
		this.name = Optional.ofNullable(name);
		return this;
	}

	/**
	 * Description of the datatable to be build.
	 */
	public DataTableFromCSVBuilder description(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Delimiter used in CSV strings to separate values (default ';').
	 */
	public DataTableFromCSVBuilder delimiter(Character delimiter) {
		this.delimiter = delimiter;
		return this;
	}

	/**
	 * Symbol used in CSV strings to indicate missing values (default '?').
	 */
	public DataTableFromCSVBuilder missingSymbol(String symbol) {
		this.missingSymbol = symbol;
		return this;
	}

	public DataTableFromCSVBuilder attributeMetadataCSV(String attributeCSV) {
		this.attributeCSV = attributeCSV;
		return this;
	}

	public DataTableFromCSVBuilder attributeGroupCSV(String attributeGroupCSV) {
		this.attributeGroupCSV = attributeGroupCSV;
		return this;
	}

	public DataTableFromCSVBuilder dataCSV(String dataCSV) {
		this.dataCSV = dataCSV;
		return this;
	}

	public DataTable build() throws DataFormatException {
		checkNotNull(id, "Did not set id");
		checkNotNull(attributeCSV, "Did not set attribute metadata csv.");
		checkNotNull(dataCSV, "Did not set data csv.");

		List<List<String>> rawAttributeData = CSV.csvStringToList(attributeCSV, delimiter);

		List<List<String>> rawData = CSV.csvStringToList(dataCSV, delimiter);

		LOGGER.fine("Creating attributes");

		List<Integer> nameAttributeIndices = getNameAttributeIndices(rawAttributeData);
		List<String> objectNames = objectNames(nameAttributeIndices, rawData);
		Population population = Populations.population(Identifier.id("population_of_" + id), "Population of " + name.orElse(id.toString()),
				"This population has been created automatically by csv import.", objectNames);

		List<Attribute<?>> attributes = new ArrayList<>();
		List<Attribute<?>> attributesForGroupConstruction = new ArrayList<>();

		// loop over all columns to create attributes (except the first, because
		// there only row names are stored)
		for (int i = 0; i < rawAttributeData.size(); i++) {
			// Filter values for the current attribute from raw data
			List<String> values = new ArrayList<>(rawData.size());
			for (List<String> aRawData : rawData) {
				values.add(aRawData.get(i));
			}

			List<String> currentRawAttrData = rawAttributeData.get(i);
			String attributeType = currentRawAttrData.get(ATTRIBUTE_TYPE_COLUMN).trim();
			String attributeName = currentRawAttrData.get(ATTRIBUTE_NAME_COLUMN).trim();
			String attributeDescription = currentRawAttrData.get(ATTRIBUTE_DESCRIPTION_COLUMN).trim();

			{
				Attribute<?> attribute = null;
				switch (attributeType) {
				case TYPE_CATEGORICAL:
					List<String> categoryValues = new ArrayList<>(values.size());
					for (String stringValue : values) {
						if (stringValue.equals(missingSymbol)) {
							categoryValues.add(null);
						} else {
							categoryValues.add(stringValue);
						}
					}
					attribute = Attributes.categoricalAttribute(attributeName, attributeDescription, categoryValues);
					break;

				case TYPE_ORDINAL:
					// Special case for ordinal attributes, should have 4th
					// column
					String attributeValueSpecificationString = currentRawAttrData.get(ATTRIBUTE_VALUES_SPECIFICATION);
					String[] split = attributeValueSpecificationString.split(ATTRIBUTE_VALUES_SPECIFICATION_DELIMITER);
					FiniteOrder comparator = new FiniteOrder(Arrays.asList(split));
					List<String> cleanedValues = new ArrayList<>(values.size());
					for (String stringValue : values) {
						if (stringValue.equals(missingSymbol)) {
							cleanedValues.add(null);
						} else {
							cleanedValues.add(stringValue);
						}
					}
					attribute = orderedCategoricAttribute(attributeName, attributeDescription, cleanedValues,
							comparator, String.class);
					break;

				case TYPE_INTEGER:
					List<Integer> intValues = new ArrayList<>(values.size());
					for (String stringValue : values) {
						try {
							intValues.add(Integer.parseInt(stringValue));
						} catch (NumberFormatException nfe) {
							intValues.add(null);
						}
					}
					attribute = orderedCategoricAttribute(attributeName, attributeDescription, intValues,
							Integer.class);
					break;

				case TYPE_METRIC:
					List<Double> doubleValues = new ArrayList<>(values.size());
					for (String stringValue : values) {
						try {
							doubleValues.add(Double.parseDouble(stringValue));
						} catch (NumberFormatException nfe) {
							doubleValues.add(null);
						}
					}
					attribute = metricDoubleAttribute(attributeName, attributeDescription, doubleValues);
					break;

				case TYPE_DATE:
					List<Date> dateValues = new ArrayList<>(values.size());
					SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					for (String stringValue : values) {
						try {
							dateValues.add(dt.parse(stringValue));
						} catch (NumberFormatException | ParseException e) {
							dateValues.add(null);
						}
					}
					attribute = Attributes.dateAttribute(attributeName, attributeDescription, dateValues);
					break;

				case TYPE_NAME:
					/*
					 * attributes for group construction need extra attribute in
					 * order to maintain compatibility with user-provided
					 * attribute indices in the attribute group file.
					 */
					attributesForGroupConstruction.add(EMPTY_ATTRIBUTE);
					continue;

				default:
					LOGGER.warning("skipping attribute of unknown type '" + attributeType + "'");
					continue;
				}

				attributes.add(attribute);
				attributesForGroupConstruction.add(attribute);
			}
		}

		LOGGER.info("Done creating attributes (" + attributes.size() + " attributes created)");

		LOGGER.fine("Creating attribute groups");
		List<List<String>> rawAttributeGroups = CSV.csvStringToList(attributeGroupCSV,
				ATTRIBUTE_GROUP_FIELD_DELIMITER);

		List<AttributeGroup> groups = rawAttributeGroups.stream().map(
				rawGroupData -> AttributeGroupFromCSVBuilder.getGroup(rawGroupData, attributesForGroupConstruction))
				.collect(Collectors.toList());
		// AttributeGroupStore attributeGroupStore = new
		// AttributeGroupStore(groups);

		LOGGER.info(() -> "Done creating attribute groups (" + groups.size() + " groups created)");

		Function<? super AttributeGroup, ? extends Stream<? extends Attribute<?>>> groupToAttributes = group -> groupMappers
				.stream().flatMap(m -> m.apply(group).stream());
		List<Attribute<?>> derivedAttributes = groups.stream().flatMap(groupToAttributes).collect(Collectors.toList());
		attributes.addAll(derivedAttributes);
		LOGGER.info(
				() -> "Done creating derived attributes (" + derivedAttributes.size() + " derived attributes created)");

		DataTable dataTable = DataTables.table(id, name.orElse(id.toString()), description, population, attributes, groups);

		return dataTable;
	}

	/**
	 * Either creates a list of numbers corresponding to the index or composes a
	 * list of names that are composed by the values of the name-attributes.
	 */
	private List<String> objectNames(List<Integer> nameAttributeIndices, List<List<String>> rawData) {
		List<String> result = new ArrayList<>();
		if (nameAttributeIndices.size() == 0) {
			// Just add index if no name attributes present
			for (int i = 1; i <= rawData.size(); i++) {
				result.add(String.valueOf(i));
			}
		} else {
			// Otherwise compose name
			for (List<String> data : rawData) {
				StringBuffer objName = new StringBuffer();
				Iterator<Integer> iterator = nameAttributeIndices.iterator();
				while (iterator.hasNext()) {
					int k = iterator.next();
					objName.append(data.get(k));
					if (iterator.hasNext()) {
						objName.append(", ");
					}
				}
				result.add(objName.toString());
			}
		}
		return result;
	}

	private List<Integer> getNameAttributeIndices(List<List<String>> rawAttributeData) {
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < rawAttributeData.size(); i++) {
			List<String> data = rawAttributeData.get(i);
			String type = data.get(ATTRIBUTE_TYPE_COLUMN);
			if (TYPE_NAME.equals(type)) {
				result.add(i);
			}
		}
		return result;
	}

	private static final DefaultAttribute<?> EMPTY_ATTRIBUTE = new DefaultAttribute<>(identifier("empty"), "empty",
			"empty", new ArrayList<Object>(), Object.class);

}

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

import static de.unibonn.realkd.data.table.attributegroups.AttributeGroups.distributionGroup;
import static de.unibonn.realkd.data.table.attributegroups.AttributeGroups.orderedDistributionGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroup;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroups;
import de.unibonn.realkd.data.table.attributegroups.DefaultAttributeGroup;
import de.unibonn.realkd.data.table.attributegroups.DefaultMetricSequenceWithAbsoluteDifferences;

/**
 * Builder of attribute groups from CSV.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.3.0
 * 
 */
public class AttributeGroupFromCSVBuilder {
	
	private static final int ATTRIBUTE_GROUP_NAME_INDEX = 1;
	private static final int ATTRIBUTE_GROUP_TYPE_INDEX = 2;
	private static final int ATTRIBUTE_GROUP_CONTENT_INDEX = 3;
	private static final String ATTRIBUTE_GROUP_2ND_FIELD_DELIMITER = ",";
	private static final String ATTRIBUTE_GROUP_3RD_FIELD_DELIMITER = ":";

	private enum AttributeGroupFactory {

		JOINT_MACRO_ATTRIBUTE("joint_macro_attribute") {
			@Override
			public AttributeGroup getAttributeGroup(List<String> content, List<Attribute<?>> attributeList) {

				List<String> stringGroupIndices = Arrays
						.asList(content.get(ATTRIBUTE_GROUP_CONTENT_INDEX)
								.split(ATTRIBUTE_GROUP_2ND_FIELD_DELIMITER));

				return AttributeGroups.functionalGroup(content.get(ATTRIBUTE_GROUP_NAME_INDEX), toAttributeList(stringGroupIndices, attributeList));
			}
		},
		DISTRIBUTION("distribution") {
			@Override
			public AttributeGroup getAttributeGroup(List<String> content, List<Attribute<?>> attributeList) {

				List<String> stringGroupIndices = Arrays
						.asList(content.get(ATTRIBUTE_GROUP_CONTENT_INDEX)
								.split(ATTRIBUTE_GROUP_2ND_FIELD_DELIMITER));

				return distributionGroup(content.get(ATTRIBUTE_GROUP_NAME_INDEX),
						toDoubleAttributeList(stringGroupIndices, attributeList));
			}
		},
		ORDERED_DISTRIBUTION("ordered_distribution") {
			@Override
			public AttributeGroup getAttributeGroup(List<String> content, List<Attribute<?>> attributeList) {

				List<String> stringGroupIndices = Arrays
						.asList(content.get(ATTRIBUTE_GROUP_CONTENT_INDEX)
								.split(ATTRIBUTE_GROUP_2ND_FIELD_DELIMITER));

				return orderedDistributionGroup(content.get(ATTRIBUTE_GROUP_NAME_INDEX),
						toDoubleAttributeList(stringGroupIndices, attributeList));
			}
		},
		HIERARCHY("hierarchy") {
			@Override
			public AttributeGroup getAttributeGroup(List<String> content, List<Attribute<?>> attributeList) {

				List<String> stringGroupIndices = Arrays
						.asList(content.get(ATTRIBUTE_GROUP_CONTENT_INDEX)
								.split(ATTRIBUTE_GROUP_2ND_FIELD_DELIMITER));

				return new DefaultAttributeGroup(content.get(ATTRIBUTE_GROUP_NAME_INDEX),
						toAttributeList(stringGroupIndices, attributeList));
			}
		},
		SEQUENCE("sequence") {

			/*
			 * within sequence record
			 */
			private static final int ATTRIBUTE_INDEX = 0;

			/*
			 * within sequence record
			 */
			private static final int ELEMENT_NAME_INDEX = 1;

			@Override
			public AttributeGroup getAttributeGroup(List<String> content, List<Attribute<?>> attributeList) {

				List<String> stringGroupRecords = Arrays
						.asList(content.get(ATTRIBUTE_GROUP_CONTENT_INDEX)
								.split(ATTRIBUTE_GROUP_2ND_FIELD_DELIMITER));

				List<String> stringGroupIndices = new ArrayList<>(stringGroupRecords.size());
				List<String> elementNames = new ArrayList<>(stringGroupRecords.size());
				for (String groupRecord : stringGroupRecords) {
					String[] groupRecordElements = groupRecord
							.split(ATTRIBUTE_GROUP_3RD_FIELD_DELIMITER);
					stringGroupIndices.add(groupRecordElements[ATTRIBUTE_INDEX]);
					elementNames.add(groupRecordElements[ELEMENT_NAME_INDEX]);
				}

				return new DefaultMetricSequenceWithAbsoluteDifferences(
						content.get(ATTRIBUTE_GROUP_NAME_INDEX),
						toDoubleAttributeList(stringGroupIndices, attributeList), elementNames);
			}
		},
		CATEGORY_TAG("category_tag") {
			@Override
			public AttributeGroup getAttributeGroup(List<String> content, List<Attribute<?>> attributeList) {

				List<String> stringGroupIndices = Arrays
						.asList(content.get(ATTRIBUTE_GROUP_CONTENT_INDEX)
								.split(ATTRIBUTE_GROUP_2ND_FIELD_DELIMITER));

				return new DefaultAttributeGroup(content.get(ATTRIBUTE_GROUP_NAME_INDEX),
						toAttributeList(stringGroupIndices, attributeList));
			}
		};
		
		private static List<Attribute<?>> toAttributeList(List<String> stringGroupElements,
				List<Attribute<?>> attributeList) {
			List<Attribute<?>> result = new ArrayList<>();
			for (String stringGroupElement : stringGroupElements) {
				int attributeIndex = Integer.parseInt(stringGroupElement) - 1; // contract
																				// adjustment
				result.add(attributeList.get(attributeIndex));
			}
			return result;
		}
		
		private static List<MetricAttribute> toDoubleAttributeList(List<String> stringGroupElements,
				List<Attribute<?>> attributeList) {
			List<MetricAttribute> result = new ArrayList<>();
			for (String stringGroupElement : stringGroupElements) {
				int attributeIndex = Integer.parseInt(stringGroupElement) - 1; // contract
																				// adjustment
				result.add((MetricAttribute) attributeList.get(attributeIndex));
			}
			return result;
		}

		private String dbRepresentationString;

		AttributeGroupFactory(String dbRepresentationString) {
			this.dbRepresentationString = dbRepresentationString;
		}

		private String getDatabaseRepresentation() {
			return this.dbRepresentationString;
		}

		public abstract AttributeGroup getAttributeGroup(List<String> content, List<Attribute<?>> attributeList);
	}

	public static AttributeGroupFactory getFactoryMatchingDBRepresentation(String dbRepresentation) {
		for (AttributeGroupFactory factory : AttributeGroupFactory.values()) {
			if (factory.getDatabaseRepresentation().equals(dbRepresentation)) {
				return factory;
			}
		}
		throw new IllegalArgumentException("no AttributeGroupType matching string representation");
	}

	public static AttributeGroup getGroup(List<String> dbEntry, List<Attribute<?>> attributeList) {
		AttributeGroupFactory factory = getFactoryMatchingDBRepresentation(
				dbEntry.get(ATTRIBUTE_GROUP_TYPE_INDEX));
		return factory.getAttributeGroup(dbEntry, attributeList);
	}

}
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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
package de.unibonn.realkd.data.xarf;

import static de.unibonn.realkd.data.table.attributegroups.AttributeGroups.distributionGroup;
import static de.unibonn.realkd.data.table.attributegroups.AttributeGroups.orderedDistributionGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroup;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroups;
import de.unibonn.realkd.data.table.attributegroups.DefaultAttributeGroup;
import de.unibonn.realkd.data.xarf.XarfParsing.StringToken;
import de.unibonn.realkd.data.xarf.XarfParsing.Token;

public class GroupDeclaration {

	private static final Logger LOGGER = Logger.getLogger(GroupDeclaration.class.getName());

	public final String name;
	public final String groupType;
	public final Collection<String> members;

	public GroupDeclaration(String name, String groupType, Collection<String> members) {
		this.name = name;
		this.groupType = groupType;
		this.members = members;
	}

	public static Optional<GroupDeclaration> groupEntry(String line) {
		Token[] tokens = XarfParsing.tokens(line);
		if (tokens.length < 4) {
			LOGGER.warning("Insufficient number of tokens; skipping group declaration: " + line);
			return Optional.empty();
		}
		Optional<Collection<String>> members = tokens[3].asStringCollection();
		if (!(tokens[1] instanceof StringToken) || !(tokens[2] instanceof StringToken)
				|| !(members.isPresent())) {
			LOGGER.warning("Malformed group declaration; skipping group declaration: " + line);
			return Optional.empty();
		}
		String name = ((StringToken) tokens[1]).value();
		String type = ((StringToken) tokens[2]).value();
		return Optional.of(new GroupDeclaration(name, type, members.get()));
	}

	private enum AttributeGroupFactory {
		JOINT_MACRO_ATTRIBUTE("functional_group") {
			@Override
			public AttributeGroup group(GroupDeclaration entry, Map<Identifier, Attribute<?>> attributes) {
				return AttributeGroups.functionalGroup(entry.name, toAttributeList(entry.members, attributes));
			}

		},
		DISTRIBUTION("distribution") {
			@Override
			public AttributeGroup group(GroupDeclaration entry, Map<Identifier, Attribute<?>> attributes) {
				return distributionGroup(entry.name, toDoubleAttributeList(entry.members, attributes));
			}
		},
		ORDERED_DISTRIBUTION("ordered_distribution") {
			@Override
			public AttributeGroup group(GroupDeclaration entry, Map<Identifier, Attribute<?>> attributes) {
				return orderedDistributionGroup(entry.name, toDoubleAttributeList(entry.members, attributes));
			}
		},
		HIERARCHY("hierarchy") {
			@Override
			public AttributeGroup group(GroupDeclaration entry, Map<Identifier, Attribute<?>> attributes) {
				return new DefaultAttributeGroup(entry.name, toAttributeList(entry.members, attributes));
			}
		},
		// SEQUENCE("sequence") {
		//
		// /*
		// * within sequence record
		// */
		// private static final int ATTRIBUTE_INDEX = 0;
		//
		// /*
		// * within sequence record
		// */
		// private static final int ELEMENT_NAME_INDEX = 1;
		//
		// @Override
		// public AttributeGroup getAttributeGroup(List<String> content,
		// List<Attribute<?>> attributeList) {
		//
		// List<String> stringGroupRecords = Arrays
		// .asList(content.get(ATTRIBUTE_GROUP_CONTENT_INDEX).split(ATTRIBUTE_GROUP_2ND_FIELD_DELIMITER));
		//
		// List<String> stringGroupIndices = new
		// ArrayList<>(stringGroupRecords.size());
		// List<String> elementNames = new
		// ArrayList<>(stringGroupRecords.size());
		// for (String groupRecord : stringGroupRecords) {
		// String[] groupRecordElements =
		// groupRecord.split(ATTRIBUTE_GROUP_3RD_FIELD_DELIMITER);
		// stringGroupIndices.add(groupRecordElements[ATTRIBUTE_INDEX]);
		// elementNames.add(groupRecordElements[ELEMENT_NAME_INDEX]);
		// }
		//
		// return new
		// DefaultMetricSequenceWithAbsoluteDifferences(content.get(ATTRIBUTE_GROUP_NAME_INDEX),
		// toDoubleAttributeList(stringGroupIndices, attributeList),
		// elementNames);
		// }
		// },
		CATEGORY_TAG("category_tag") {
			@Override
			public AttributeGroup group(GroupDeclaration entry, Map<Identifier, Attribute<?>> attributes) {
				return new DefaultAttributeGroup(entry.name, toAttributeList(entry.members, attributes));
			}
		};

		private static List<Attribute<?>> toAttributeList(Collection<String> attributeIdCollection,
				Map<Identifier, Attribute<?>> attributes) {
			List<Attribute<?>> result = new ArrayList<>();
			for (String idString : attributeIdCollection) {
				Identifier id = Identifier.identifier(idString);
				if (!attributes.containsKey(id)) {
					LOGGER.warning("No attribute with identifier: " + id + "; skipping");
					continue;
				}
				result.add(attributes.get(id));
			}
			return result;
		}

		private static List<MetricAttribute> toDoubleAttributeList(Collection<String> attributeIdCollection,
				Map<Identifier, Attribute<?>> attributes) {
			List<MetricAttribute> result = new ArrayList<>();
			for (String idString : attributeIdCollection) {
				Identifier id = Identifier.identifier(idString);
				Attribute<?> attribute = attributes.get(id);
				if (!(attribute instanceof MetricAttribute)) {
					LOGGER.warning("No metric attribute with identifier: " + id + "; skipping");
					continue;
				}
				result.add((MetricAttribute) attributes.get(id));
			}
			return result;
		}

		public static Optional<AttributeGroupFactory> getFactoryMatchingXarfTypeString(String dbRepresentation) {
			for (AttributeGroupFactory factory : AttributeGroupFactory.values()) {
				if (factory.xarfGroupTypeString.equals(dbRepresentation)) {
					return Optional.of(factory);
				}
			}
			return Optional.empty();
		}

		private String xarfGroupTypeString;

		AttributeGroupFactory(String dbRepresentationString) {
			this.xarfGroupTypeString = dbRepresentationString;
		}

		public abstract AttributeGroup group(GroupDeclaration entry, Map<Identifier, Attribute<?>> attributes);

	}

	public Optional<AttributeGroup> toGroup(Map<Identifier, Attribute<?>> attributeMap) {
		Optional<AttributeGroupFactory> factory = AttributeGroupFactory.getFactoryMatchingXarfTypeString(groupType);
		if (!factory.isPresent()) {
			LOGGER.warning("Unknown group type '" + groupType + "'");
		}
		return factory.map(f -> f.group(this, attributeMap));
	}

}
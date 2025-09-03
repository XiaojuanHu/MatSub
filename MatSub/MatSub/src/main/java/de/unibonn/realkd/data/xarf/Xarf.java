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

import static de.unibonn.realkd.data.Populations.population;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.AttributesFromGroupMapper;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.DataTables;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroup;
import de.unibonn.realkd.data.xarf.XarfParsing.CsvPortion;
import de.unibonn.realkd.data.xarf.XarfParsing.StringToken;

/**
 * <p>
 * Represents a XARF file parsed according to the top-level specification. This
 * consists of:
 * </p>
 * <ol>
 * <li>sequence of leading comment lines that represent the declared datatable's
 * description</li>
 * <li>declaration of the described relation</li>
 * <li>declaration of the contained attributes</li>
 * <li>declaration of attribute groups</li>
 * <li>declaration of the data format</li>
 * <li>a csv part (which can contain an optional header)</li>
 * </ol>
 *
 * @see XarfParsing
 * 
 * @author Panagiotis Mandros
 * @author Michael Hedderich
 * @author Mario Boley
 * 
 * @since 0.7.0
 * @version 0.7.0
 *
 */
public class Xarf {

	private static final Logger LOGGER = Logger.getLogger(Xarf.class.getName());

	private final List<String> leadingCommentRows;
	private final RelationDeclaration relationDeclaration;
	private final List<AttributeDeclaration> attributeDeclarations;
	private final List<GroupDeclaration> groupDeclarations;
	private final DataDeclaration dataDeclaration;
	private final List<String> csvRows;

	Xarf(List<String> leadingCommentRows, RelationDeclaration relationDeclaration,
			List<AttributeDeclaration> attributeDeclarations, List<GroupDeclaration> groupDeclarations,
			DataDeclaration dataDeclaration, List<String> data) {
		this.leadingCommentRows = leadingCommentRows;
		this.relationDeclaration = relationDeclaration;
		this.attributeDeclarations = attributeDeclarations;
		this.groupDeclarations = groupDeclarations;
		this.dataDeclaration = dataDeclaration;
		this.csvRows = data;
	}

	private Population extractPopulation(List<List<String>> parsedData) {
		Identifier id = Identifier.id("population_of_" + relationDeclaration.id);
		String caption = "Population of " + relationDeclaration.caption;
		String description = "This population has been created automatically by xarf import.";
		Optional<Integer> nameAttributeIndex = XarfParsing.nameAttributeIndex(attributeDeclarations);
		Optional<List<String>> names = nameAttributeIndex.flatMap(i -> XarfParsing.column(i, parsedData));
		return (names.isPresent()) ? population(id, caption, description, names.get())
				: population(id, caption, description, parsedData.size());
	}

	private List<Attribute<?>> implicitMatching(CsvPortion csvPortion) {
		List<Attribute<?>> attributes = new ArrayList<>();
		for (int i = 0; i < attributeDeclarations.size(); i++) {
			try {
				Optional<Attribute<?>> attribute = attributeDeclarations.get(i).attribute(csvPortion.column(i));
				if (attribute.isPresent()) {
					attributes.add(attribute.get());
				}

			} catch (Exception exc) {
				LOGGER.severe("Error; skipping attribute for declaration: " + attributeDeclarations.get(i));
			}
		}
		return attributes;
	}

	/*
	 * Returns a list of attributes by matching with id. If an attribute cannot be
	 * matched via ID, then default matching occurs
	 */
	private List<Attribute<?>> idMatching(CsvPortion csvPortion) {
		List<Attribute<?>> attributes = new ArrayList<>();

		HashMap<Integer, Identifier> headerIndexToHeaderID = new HashMap<>();
		HashMap<Identifier, AttributeDeclaration> attributeIDFromDeclarationToDeclaration = new HashMap<>();

		if (csvPortion.headers.isPresent()) {
			IntStream.range(0, csvPortion.numberOfColumns())
					.forEach(i -> headerIndexToHeaderID.put(i, Identifier.id(csvPortion.headers.get().get(i))));

			attributeDeclarations.stream().forEach(i -> attributeIDFromDeclarationToDeclaration.put(i.id(), i));
		}

		for (int i = 0; i < csvPortion.numberOfColumns(); i++) {
			AttributeDeclaration attributeDeclaration;
			if (headerIndexToHeaderID.containsKey(i)) {
				Identifier headerID = headerIndexToHeaderID.get(i);
				if (attributeIDFromDeclarationToDeclaration.containsKey(headerID)) {
					attributeDeclaration = attributeIDFromDeclarationToDeclaration.get(headerID);
				} else {
					String type = XarfParsing.sniffForAttributeType(csvPortion.data, i);
					LOGGER.info("Sniffed: " + type + " for attribute with index: " + (i + 1));
					attributeDeclaration = new AttributeDeclaration(Identifier.id("Attribute_" + (i + 1)),
							new StringToken(type), Optional.empty(), new HashMap<String, Object>());
				}
			} else {
				String type = XarfParsing.sniffForAttributeType(csvPortion.data, i);
				attributeDeclaration = new AttributeDeclaration(Identifier.id("Attribute_" + (i + 1)),
						new StringToken(type), Optional.empty(), new HashMap<String, Object>());
				LOGGER.info("Sniffed: " + type + " for attribute with index: " + (i + 1));

			}

			try {
				Optional<Attribute<?>> attribute = attributeDeclaration.attribute(csvPortion.column(i));
				if (attribute.isPresent()) {
					attributes.add(attribute.get());
				}
			} catch (Exception exc) {
				LOGGER.severe("Error; skipping attribute for declaration: " + attributeDeclaration);
			}
		}

		return attributes;
	}

	/*
	 * Returns a list of attributes with implicit or id matching
	 */
	private List<Attribute<?>> attributes(CsvPortion csvPortion) {
		LOGGER.fine("Creating attributes");

		List<Attribute<?>> attributes = new ArrayList<>();

		if (!attributeDeclarations.isEmpty() && attributeDeclarations.size() == csvPortion.numberOfColumns()) {
			attributes = implicitMatching(csvPortion);
		} else {
			attributes = idMatching(csvPortion);

		}

		LOGGER.info("Done creating attributes (" + attributes.size() + " attributes created)");
		return attributes;
	}

	public DataTable toDatatable(List<AttributesFromGroupMapper> groupMappers) {
		String description = createDescription();
		CsvPortion csvPortion = dataDeclaration.parseDataPortion(csvRows);
		Population population = extractPopulation(csvPortion.data);
		List<Attribute<?>> attributes = attributes(csvPortion);
		List<AttributeGroup> groups = createGroups(attributes);
		attributes.addAll(derivedAttributes(groups, groupMappers));
		return DataTables.table(relationDeclaration.id, relationDeclaration.caption, description, population,
				attributes, groups);
	}

	private String createDescription() {
		StringBuilder descriptionBuilder = new StringBuilder("");
		for (String line : leadingCommentRows) {
			descriptionBuilder.append(line.replaceFirst(XarfParsing.COMMENTTOKEN, ""));
			descriptionBuilder.append(System.lineSeparator());
		}
		return descriptionBuilder.toString();
	}

	private List<AttributeGroup> createGroups(List<Attribute<?>> attributes) {
		Map<Identifier, Attribute<?>> attributeMap = new HashMap<>();
		attributes.forEach(a -> attributeMap.put(a.identifier(), a));
		ArrayList<AttributeGroup> result = new ArrayList<>();
		for (GroupDeclaration declaration : groupDeclarations) {
			Optional<AttributeGroup> group = declaration.toGroup(attributeMap);
			if (!group.isPresent()) {
				continue;
			}
			result.add(group.get());
		}
		return result;
	}

	private List<Attribute<?>> derivedAttributes(List<AttributeGroup> groups,
			List<AttributesFromGroupMapper> groupMappers) {
		Function<? super AttributeGroup, ? extends Stream<? extends Attribute<?>>> groupToAttributes = group -> groupMappers
				.stream().flatMap(m -> m.apply(group).stream());
		List<Attribute<?>> result = groups.stream().flatMap(groupToAttributes).collect(Collectors.toList());
		LOGGER.info(() -> "Done creating derived attributes (" + result.size() + " derived attributes created)");
		return result;
	}

//	// create or extend or correct attribute declarations
//	private List<AttributeDeclaration> completeAttributeDeclerations(List<AttributeDeclaration> userAttrDecl,
//			CsvPortion csvPortion) {
//
//		AttributeDeclaration[] toBeCompleteAttributeDeclerations = new AttributeDeclaration[csvPortion
//				.numberOfColumns()];
//		boolean hasBeenMatched[] = new boolean[csvPortion.numberOfColumns()];
//
//		// try to match ID from header
//		if (csvPortion.headers.isPresent()) {
//			LOGGER.info("A csv header was detected. Trying to match header to attribute declarations");
//			HashMap<Identifier, Integer> attributeIDFromHeaderToHeaderPositionMapper = new HashMap<>();
//
//			// get the potential IDs from the header and map them to their respective
//			// position
//			Identifier[] attributeIDs = csvPortion.headers.get().stream().map(i -> Identifier.id(i))
//					.toArray(n -> new Identifier[n]); // csvHeader.get().split(",");
//			IntStream.range(0, attributeIDs.length)
//					.forEach(i -> attributeIDFromHeaderToHeaderPositionMapper.put(attributeIDs[i], i));
//
//			// for every user declaration
//			Iterator<AttributeDeclaration> iterator = userAttrDecl.iterator();
//			while (iterator.hasNext()) {
//				AttributeDeclaration userAttributeDeclaration = iterator.next();
//
//				// get the ID
//				Identifier attributeIDFromUserDecl = userAttributeDeclaration.id();
//
//				// if the user declaration ID matches the header ID
//				if (attributeIDFromHeaderToHeaderPositionMapper.containsKey(attributeIDFromUserDecl)) {
//					int attributePosition = attributeIDFromHeaderToHeaderPositionMapper.get(attributeIDFromUserDecl);
//					// has not been matched yet
//					if (!hasBeenMatched[attributePosition]) {
//						toBeCompleteAttributeDeclerations[attributePosition] = userAttributeDeclaration;
//						hasBeenMatched[attributePosition] = true;
//						iterator.remove();
//					} else { // if has been mathced before
//						// check for duplicate declaration (ok mistake)
//						if (userAttributeDeclaration.equals(toBeCompleteAttributeDeclerations[attributePosition])) {
//							LOGGER.warning("Skipping duplicate attribute declaration: " + userAttributeDeclaration);
//							iterator.remove();
//							// TODO else process shoud stop
//						} else {
//							LOGGER.severe("Attribute number " + attributePosition + " has already been matched");
//						}
//					}
//				} else {
//					LOGGER.warning("Could not match attribute ID from csv header to metadata declaration for: "
//							+ userAttributeDeclaration + " (proceeding with implicit position matching)");
//					continue;
//				}
//			}
//
//		}
//
//		// try to match with explicit position
//		// TODO
//
//		// implicit matching
//		// happens when remaining user declarations are equal to
//		// remaining unmatched columns
//		int numUnMatchedAttributes = 0;
//		for (boolean matched : hasBeenMatched) {
//			if (!matched) {
//				numUnMatchedAttributes++;
//			}
//		}
//
//		if ((numUnMatchedAttributes == userAttrDecl.size()) && userAttrDecl.size() != 0) {
//			LOGGER.info("Implict position matching for remaining unmatched attribute declarations and attributes");
//			Iterator<AttributeDeclaration> iterator = userAttrDecl.iterator();
//			while (iterator.hasNext()) {
//				AttributeDeclaration userAttributeDeclaration = iterator.next();
//				for (int i = 0; i < hasBeenMatched.length; i++) {
//					if (hasBeenMatched[i] == false) {
//						toBeCompleteAttributeDeclerations[i] = userAttributeDeclaration;
//						hasBeenMatched[i] = true;
//						iterator.remove();
//						break;
//					}
//				}
//			}
//		} else if ((numUnMatchedAttributes != userAttrDecl.size()) && !userAttrDecl.isEmpty()) {
//			LOGGER.info(
//					"There were more attribute declarations than actual attributes in the data; resulting to defaults for all unmatched attributes");
//		}
//
//		return XarfParsing.sniffForAttributeTypesAndReturnComplete(toBeCompleteAttributeDeclerations, csvPortion.data);
//	}

}

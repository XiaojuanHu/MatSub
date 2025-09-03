package de.unibonn.realkd.knowledgemodeling.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.DefaultCategoricAttribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.knowledgemodeling.constraints.MaxEntConstraint;

/**
 * @author bkang
 */
public class ValueSpaceUtility {

	private static final ValueSpaceUtility INSTANCE = new ValueSpaceUtility();

	public static ValueSpaceUtility getInstance() {
		return INSTANCE;
	}

	public static Map<String, Integer> computeCardinalityOfPartitions(
			Map<String, ValueSpace> constraintCombinationToValueSpaceMap) {
		Map<String, Integer> result = new HashMap<>();
		for (String key : constraintCombinationToValueSpaceMap.keySet()) {
			int cardinality = 0;
			List<String> keysOfMissingConstraintsCombination = computeKeysOfMissingConstraintsCombination(key);
			for (String keyOfMissingConstraintsCombination : keysOfMissingConstraintsCombination) {
				ValueSpace valueSpace = constraintCombinationToValueSpaceMap
						.get(combineTwoKeys(key,
								keyOfMissingConstraintsCombination));
				cardinality += ((getNumberOfConstraints(keyOfMissingConstraintsCombination) % 2 == 1) ? -1
						: 1)
						* (valueSpace == null ? 0 : valueSpace.getCardinality());
			}
			result.put(key, cardinality);
		}
		return result;
	}

	/**
	 * <p>
	 * Computes compatible value spaces for all combinations of a set of input
	 * constraints (restricted to some set of attributes).
	 * </p>
	 * <p>
	 * Value spaces are stored within a map wherein each value space is
	 * identified by a binary string indicating the subset of constraints that
	 * has been used to construct the corresponding value space. For example,
	 * the value space resulting from the first, second, and fourth constraint
	 * out of four is indexed by the string "1101".
	 * </p>
	 * 
	 * @param constraints
	 *            the set of constraints to which the resulting value space must
	 *            be compatible to
	 * @param attributes
	 *            the attributes for which the value spaces are supposed to be
	 *            spanned (usually the intersection of all attributes referred
	 *            to in the constraints)
	 * @return the key/value space map
	 * 
	 */
	public static Map<String, ValueSpace> computeCompatibleSubValueSpacesForAttributes(
			List<MaxEntConstraint> constraints, List<Attribute<?>> attributes) {
		Map<String, ValueSpace> valueSpaceMap = new HashMap<>();
		Queue<String> valueSpaceKeyQueue = new LinkedList<>();
		for (MaxEntConstraint constraint : constraints) {
			ValueSpace valueSpace = new ValueSpace(constraint, attributes);
			if (valueSpace.getCardinality() != 0) {
				String key = generateKeyOfValueSpace(valueSpace, constraints);
				valueSpaceMap.put(key, valueSpace);
				valueSpaceKeyQueue.add(key);
			}
		}
		while (valueSpaceKeyQueue.size() != 0) {
			String key = valueSpaceKeyQueue.poll();
			List<MaxEntConstraint> constraintsToCombine = getConstraintsToCombine(
					key, constraints);
			for (MaxEntConstraint constraint : constraintsToCombine) {
				ValueSpace newValueSpace = valueSpaceMap.get(key).getSubSpace(
						constraint);
				if (newValueSpace.getCardinality() != 0) {
					String newKey = generateKeyOfValueSpace(newValueSpace,
							constraints);
					valueSpaceKeyQueue.add(newKey);
					valueSpaceMap.put(newKey, newValueSpace);
				}
			}
		}
		return valueSpaceMap;
	}

	public static List<MaxEntConstraint> getConstraintByDecodingKey(String key,
			List<MaxEntConstraint> constraints) {
		List<MaxEntConstraint> result = new ArrayList<>();
		for (int i = 0; i < key.length(); i++) {
			if (key.charAt(i) == '1') {
				result.add(constraints.get(key.length() - i - 1));
			}
		}
		return result;
	}

	public static List<String> getKeysOfCombinationsThatContainConstraint(
			MaxEntConstraint constraint, Set<String> keySet,
			List<MaxEntConstraint> constraints) {
		List<String> result = new ArrayList<>();
		int indexOfConstraint = constraints.indexOf(constraint);
		for (String key : keySet) {
			if (key.charAt(key.length() - indexOfConstraint - 1) == '1') {
				result.add(key);
			}
		}
		return result;
	}

	private static String combineTwoKeys(String key1, String key2) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < key1.length(); i++) {
			if (key1.charAt(i) == key2.charAt(i)) {
				if (key1.charAt(i) == '1') {
					throw new IllegalArgumentException();
				} else {
					sb.append('0');
				}
			} else {
				sb.append('1');
			}
		}
		return sb.toString();
	}

	private static int getNumberOfConstraints(String key) {
		int result = 0;
		for (int i = 0; i < key.length(); i++) {
			if (key.charAt(i) == '1') {
				result++;
			}
		}
		return result;
	}

	private static List<String> computeKeysOfMissingConstraintsCombination(
			String key) {
		List<String> result = new ArrayList<>();
		int numberOfConstraintCombinations = (int) Math.pow(2, key.length());
		int intKey = Integer.parseInt(key, 2);
		for (int i = 0; i < numberOfConstraintCombinations; i++) {
			if ((i & intKey) == 0) {
				result.add(getFixedLengthBinaryString(
						Integer.toBinaryString(i), key.length()));
			}
		}

		return result;
	}

	private static String getFixedLengthBinaryString(String binary, int length) {
		int lenDiff = length - binary.length();
		if (lenDiff == 0) {
			return binary;
		} else if (lenDiff > 0) {
			StringBuilder sb = new StringBuilder(binary);
			for (int i = 0; i < lenDiff; i++) {
				sb.insert(0, '0');
			}
			return sb.toString();
		} else {
			throw new IllegalArgumentException();
		}
	}

	private static String generateKeyOfValueSpace(ValueSpace valueSpace,
			List<MaxEntConstraint> allConstraint) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < allConstraint.size(); i++) {
			if (valueSpace.getAssociatedConstraints().contains(
					allConstraint.get(i))) {
				sb.append('1');
			} else {
				sb.append('0');
			}
		}
		return sb.reverse().toString();

	}

	private static List<MaxEntConstraint> getConstraintsToCombine(String key,
			List<MaxEntConstraint> allConstraint) {
		List<MaxEntConstraint> result = new ArrayList<>();
		int pos = 0;
		while (pos < key.length()) {
			if (key.charAt(pos) == '1') {
				break;
			}
			pos++;
		}
		pos = key.length() - pos;
		for (; pos < key.length(); pos++) {
			result.add(allConstraint.get(pos));
		}
		return result;
	}

	public static int computeValueSpaceCardinalityFromAttributes(
			Set<Attribute<?>> attributes) {
		int result = 1;
		for (Attribute<?> attribute : attributes) {
			result *= computeValueSpaceCardinalityOfOneAttribute(attribute,
					new HashSet<AttributeBasedProposition>());
		}
		return result;
	}

	private static int computeValueSpaceCardinalityOfOneAttribute(
			Attribute<?> attribute, Set<AttributeBasedProposition> propositions) {
		if (propositions.size() == 0) {
			if (attribute instanceof DefaultCategoricAttribute) {
				return ((DefaultCategoricAttribute) attribute)
						.categories().size();
			} else {
				return attribute.nonMissingValues().size()
						+ attribute.missingPositions().size();
			}
		} else {
			if (attribute instanceof DefaultCategoricAttribute) {
				return computeCategoricalSupportValueSet(
						propositions,
						((DefaultCategoricAttribute) attribute)
								.categories()).size();
			} else {
				return computeNumericSupportValueSet(propositions).size();
			}

		}
	}

	private static Set<String> computeCategoricalSupportValueSet(
			Set<AttributeBasedProposition> propositions, List<String> categories) {
		boolean isPassedAllPropositions;
		Set<String> results = new HashSet<>();
		for (String category : categories) {
			isPassedAllPropositions = true;
			for (AttributeBasedProposition proposition : propositions) {
				if (!proposition.constraint().holds(category)) {
					isPassedAllPropositions = false;
					break;
				}
			}
			if (isPassedAllPropositions) {
				results.add(category);
			}
		}
		return results;
	}

	private static Set<Integer> computeNumericSupportValueSet(
			Set<AttributeBasedProposition> propositions) {
		Set<Integer> support = new HashSet<Integer>(ImmutableList.copyOf(
				((AttributeBasedProposition) propositions.toArray()[0])
						.supportSet()));
		for (AttributeBasedProposition proposition : propositions) {
			support.retainAll(ImmutableList.copyOf(proposition.supportSet()));
		}
		return support;
	}

	public static double getFractionOfSupportValuesOfPropSet(
			Set<AttributeBasedProposition> propositions, int tableSize) {
		double fraction = 1.;
		Map<Attribute<?>, Set<AttributeBasedProposition>> attributeWisePropositions = computeAttributeWisePropositions(propositions);
		for (Attribute<?> attribute : attributeWisePropositions.keySet()) {
			if (attribute instanceof DefaultCategoricAttribute) {
				fraction *= computeValueSpaceCardinalityOfOneAttribute(
						attribute, attributeWisePropositions.get(attribute))
						* 1.
						/ ((DefaultCategoricAttribute) attribute)
								.categories().size();
			} else if (attribute instanceof MetricAttribute) {
				fraction *= computeValueSpaceCardinalityOfOneAttribute(
						attribute, attributeWisePropositions.get(attribute))
						* 1. / tableSize;
			}
		}

		return fraction;
	}

	private static Map<Attribute<?>, Set<AttributeBasedProposition>> computeAttributeWisePropositions(
			Set<AttributeBasedProposition> propositions) {
		Map<Attribute<?>, Set<AttributeBasedProposition>> map = new HashMap<>();
		for (AttributeBasedProposition proposition : propositions) {
			if (map.containsKey(proposition.attribute())) {
				map.get(proposition.attribute()).add(proposition);
			} else {
				Set<AttributeBasedProposition> propositionSet = new HashSet<>();
				propositionSet.add(proposition);
				map.put(proposition.attribute(), propositionSet);
			}
		}
		return map;
	}
}

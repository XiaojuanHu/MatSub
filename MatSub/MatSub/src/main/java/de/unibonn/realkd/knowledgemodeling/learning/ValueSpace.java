package de.unibonn.realkd.knowledgemodeling.learning;

import static de.unibonn.realkd.common.IndexSets.copyOf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.DefaultCategoricAttribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.knowledgemodeling.constraints.FrequencyConstraint;
import de.unibonn.realkd.knowledgemodeling.constraints.MaxEntConstraint;

/**
 * @author bkang
 */
public class ValueSpace {

	private final int cardinality;

	private final Map<Attribute<?>, List<String>> values;

	private final List<MaxEntConstraint> associatedConstraints;

	private final List<Attribute<?>> associatedAttributes;

	public ValueSpace(MaxEntConstraint constraint, List<Attribute<?>> associatedAttributes) {
		this.associatedConstraints = new ArrayList<>();
		this.associatedConstraints.add(constraint);
		this.values = initValueSpace(constraint);
		this.cardinality = computeCardinality(values, associatedAttributes);
		this.associatedAttributes = associatedAttributes;
	}

	public ValueSpace(List<MaxEntConstraint> associatedConstraints, Map<Attribute<?>, List<String>> values,
			List<Attribute<?>> associatedAttributes) {
		this.associatedConstraints = associatedConstraints;
		this.values = values;
		this.cardinality = computeCardinality(values, associatedAttributes);
		;
		this.associatedAttributes = associatedAttributes;
	}

	private int computeCardinality(Map<Attribute<?>, List<String>> values, List<Attribute<?>> attributes) {
		int result = 1;
		Set<Attribute<?>> keySet = values.keySet();
		for (Attribute<?> attribute : attributes) {
			if (keySet.contains(attribute)) {
				result *= values.get(attribute).size();
				if (result == 0) {
					return 0;
				}
			} else {
				result *= computeSubSpaceCardOfAttribute(attribute);
			}
		}
		return result;
	}

	private int computeSubSpaceCardOfAttribute(Attribute<?> attribute) {
		if (attribute instanceof DefaultCategoricAttribute) {
			return ((DefaultCategoricAttribute) attribute).categories().size();
		} else if (attribute instanceof MetricAttribute) {
			return (attribute.numberOfNonMissingValues() + attribute.missingPositions().size());
		} else {
			throw new IllegalArgumentException();
		}
	}

	private Map<Attribute<?>, List<String>> initValueSpace(MaxEntConstraint constraint) {
		Map<Attribute<?>, List<String>> result = new HashMap<>();
		Map<Attribute<?>, List<AttributeBasedProposition<?>>> attributePropositionMap = computeAttributePropositionMap(
				((FrequencyConstraint) constraint).getAssociatedPropostions());
		for (Attribute<?> attribute : attributePropositionMap.keySet()) {
			result.put(attribute, computeOneDimSubValueSpace(attributePropositionMap.get(attribute), attribute));
		}
		return result;
	}

	private Map<Attribute<?>, List<AttributeBasedProposition<?>>> computeAttributePropositionMap(
			List<AttributeBasedProposition<?>> propositions) {
		Map<Attribute<?>, List<AttributeBasedProposition<?>>> result = new HashMap<>();
		for (AttributeBasedProposition<?> proposition : propositions) {
			Attribute<?> attribute = proposition.attribute();
			if (!result.containsKey(proposition.attribute())) {
				result.put(attribute, new ArrayList<AttributeBasedProposition<?>>());
			}
			result.get(attribute).add(proposition);
		}
		return result;
	}

	private List<String> computeOneDimSubValueSpace(List<AttributeBasedProposition<?>> propositions,
			Attribute<?> attribute) {
		if (attribute instanceof DefaultCategoricAttribute) {
			return filterCategoricalValuesAgainstPropositions(((DefaultCategoricAttribute) attribute).categories(),
					propositions);
		} else if (attribute instanceof MetricAttribute) {
			return convertSupportSetToList(
					filterMetricSupportAgainstPropositions(propositions.get(0).supportSet(), propositions));
		} else {
			throw new IllegalArgumentException();
		}
	}

	private List<String> filterCategoricalValuesAgainstPropositions(List<String> values,
			List<AttributeBasedProposition<?>> propositions) {
		boolean isPassedAllPropositions;
		List<String> result = new ArrayList<>();
		for (String value : values) {
			isPassedAllPropositions = true;
			for (AttributeBasedProposition proposition : propositions) {
				if (!proposition.constraint().holds(value)) {
					isPassedAllPropositions = false;
					break;
				}
			}
			if (isPassedAllPropositions) {
				result.add(value);
			}
		}
		return result;
	}

	private List<String> convertSupportSetToList(Set<Integer> support) {
		List<String> result = new ArrayList<>();
		for (Integer integer : support) {
			result.add(integer.toString());
		}
		return result;
	}

	private Set<Integer> filterMetricSupportAgainstPropositions(IndexSet support,
			List<AttributeBasedProposition<?>> propositions) {
		Set<Integer> result = new HashSet<>(ImmutableList.copyOf(support));

		for (AttributeBasedProposition<?> proposition : propositions) {
			result.retainAll(ImmutableList.copyOf(proposition.supportSet()));
		}
		return result;
	}

	public ValueSpace getSubSpace(MaxEntConstraint additionalConstraint) {
		Map<Attribute<?>, List<AttributeBasedProposition<?>>> additionalAttributePropositionMap = computeAttributePropositionMap(
				((FrequencyConstraint) additionalConstraint).getAssociatedPropostions());
		Map<Attribute<?>, List<String>> newValues = new HashMap<>();
		for (Attribute<?> attribute : values.keySet()) {
			if (additionalAttributePropositionMap.keySet().contains(attribute)) {
				List<AttributeBasedProposition<?>> additionalPropositions = additionalAttributePropositionMap
						.get(attribute);
				if (attribute instanceof DefaultCategoricAttribute) {
					List<String> valuesOfOneAttribute = filterCategoricalValuesAgainstPropositions(
							values.get(attribute), additionalPropositions);
					newValues.put(attribute, valuesOfOneAttribute);
				} else if (attribute instanceof MetricAttribute) {
					Set<Integer> support = new HashSet<>();
					for (String s : values.get(attribute)) {
						support.add(Integer.valueOf(s));
					}
					newValues.put(attribute, convertSupportSetToList(
							filterMetricSupportAgainstPropositions(copyOf(support), additionalPropositions)));
				} else {
					throw new IllegalArgumentException();
				}
			} else {
				newValues.put(attribute, new ArrayList<>(values.get(attribute)));
			}
		}
		for (Attribute<?> attribute : additionalAttributePropositionMap.keySet()) {
			if (!values.keySet().contains(attribute)) {
				newValues.put(attribute,
						computeOneDimSubValueSpace(additionalAttributePropositionMap.get(attribute), attribute));

			}
		}

		List<MaxEntConstraint> newConstraints = new ArrayList<>(associatedConstraints);
		newConstraints.add(additionalConstraint);
		return new ValueSpace(newConstraints, newValues, associatedAttributes);
	}

	public int getCardinality() {
		return cardinality;
	}

	public List<MaxEntConstraint> getAssociatedConstraints() {
		return associatedConstraints;
	}

	public Map<Attribute<?>, List<String>> getValues() {
		return values;
	}

}

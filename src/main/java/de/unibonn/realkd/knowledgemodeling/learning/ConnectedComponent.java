package de.unibonn.realkd.knowledgemodeling.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.knowledgemodeling.constraints.FrequencyConstraint;
import de.unibonn.realkd.knowledgemodeling.constraints.MaxEntConstraint;
import de.unibonn.realkd.patterns.association.Associations;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptors;

/**
 * @author Bo Kang
 * 
 * @since 0.2.0
 * 
 * @version 0.5.0
 * 
 */
public class ConnectedComponent {

	private final List<MaxEntConstraint> constraints;

	private final Map<String, Integer> cardinalityOfPartitions;

	private final Map<String, ValueSpace> valuesPacesOfConstraintCombinations;

	private final List<Attribute<?>> attributesWithInConnectedComponent;

	private Map<String, Double> constCombinationToPartitionValueMap;

	private double partitionFunctionValue;

	public ConnectedComponent(List<MaxEntConstraint> constraints) {
		this.constraints = constraints;

		this.attributesWithInConnectedComponent = computeAttributesWithinConnectedComponent();
		this.valuesPacesOfConstraintCombinations = ValueSpaceUtility
				.computeCompatibleSubValueSpacesForAttributes(constraints, attributesWithInConnectedComponent);
		this.cardinalityOfPartitions = ValueSpaceUtility
				.computeCardinalityOfPartitions(valuesPacesOfConstraintCombinations);
		recomputePartitionFunctionValue();
	}

	public double computeSumOfMultipliersActivatedByWildCardProps(Set<Proposition> propositions) {
		// TODO there is a whole lot of implicit assumptions here.
		// In fact this whole object should only be created with an explicit
		// final reference to a propositional logic
		Population pop = ((LogicalDescriptor) ((FrequencyConstraint) constraints.get(0))
				.getPattern().descriptor()).population();
//		MaxEntConstraint newConstraint = new FrequencyConstraint(
//				new DefaultAssociationBuilder().apply((LogicalDescriptors.create(propositionalLogic, propositions))));

		MaxEntConstraint newConstraint = new FrequencyConstraint(
				Associations.association(
						LogicalDescriptors.create(pop, propositions), ImmutableList.of()));

		List<MaxEntConstraint> augmentedConstraints = new ArrayList<>(constraints);
		augmentedConstraints.add(newConstraint);

		Map<String, ValueSpace> valueSpacesOfAugmentedConstraints = ValueSpaceUtility
				.computeCompatibleSubValueSpacesForAttributes(augmentedConstraints, attributesWithInConnectedComponent);
		Map<String, Integer> cardinalityOfAugmentedPartitions = ValueSpaceUtility
				.computeCardinalityOfPartitions(valueSpacesOfAugmentedConstraints);

		List<String> keys = ValueSpaceUtility.getKeysOfCombinationsThatContainConstraint(newConstraint,
				cardinalityOfAugmentedPartitions.keySet(), augmentedConstraints);

		double result = 0.;
		for (String key : keys) {
			int cardinality = cardinalityOfAugmentedPartitions.get(key);
			if (cardinality != 0) {
				List<MaxEntConstraint> constraintInCombination = ValueSpaceUtility.getConstraintByDecodingKey(key,
						augmentedConstraints);
				double sum = 0;
				for (MaxEntConstraint constraint : constraintInCombination) {
					sum += constraint.getMultiplier();
				}
				sum = Math.exp(sum);
				result += sum * cardinality;
			}
		}

		return result;
	}

	public void recomputePartitionFunctionValue() {
		partitionFunctionValue = 0.;
		constCombinationToPartitionValueMap = new HashMap<>();
		int cardOfNotPassedValues = ValueSpaceUtility
				.computeValueSpaceCardinalityFromAttributes(new HashSet<>(attributesWithInConnectedComponent));
		for (String key : cardinalityOfPartitions.keySet()) {
			int cardinality = cardinalityOfPartitions.get(key);
			if (cardinality != 0) {
				List<MaxEntConstraint> constraintsInCombination = ValueSpaceUtility.getConstraintByDecodingKey(key,
						constraints);
				double sum = 0.;
				for (MaxEntConstraint constraint : constraintsInCombination) {
					sum += constraint.getMultiplier();
				}
				sum = Math.exp(sum);
				cardOfNotPassedValues -= cardinality;
				constCombinationToPartitionValueMap.put(key, sum * cardinality);
				partitionFunctionValue += sum * cardinality;
			} else {
				constCombinationToPartitionValueMap.put(key, 0.);
			}

		}
		partitionFunctionValue += cardOfNotPassedValues;
	}

	private List<Attribute<?>> computeAttributesWithinConnectedComponent() {
		Set<Attribute<?>> attributeSet = new HashSet<>();
		for (MaxEntConstraint constraint : constraints) {
			for (AttributeBasedProposition proposition : ((FrequencyConstraint) constraint)
					.getAssociatedPropostions()) {
				attributeSet.add(proposition.attribute());
			}
		}
		return new ArrayList<>(attributeSet);
	}

	public List<MaxEntConstraint> getConstraints() {
		return constraints;
	}

	public double getPartitionFunctionValue() {
		return partitionFunctionValue;
	}

	public List<Attribute<?>> getAttributesWithInConnectedComponent() {
		return attributesWithInConnectedComponent;
	}

	public double getSumOfMultipliersActivatedByConstraint(MaxEntConstraint constraint) {
		List<String> keys = ValueSpaceUtility.getKeysOfCombinationsThatContainConstraint(constraint,
				cardinalityOfPartitions.keySet(), constraints);
		double result = 0.;
		for (String key : keys) {
			result += constCombinationToPartitionValueMap.get(key);
		}
		return result;
	}

}

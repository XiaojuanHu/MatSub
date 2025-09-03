package de.unibonn.realkd.knowledgemodeling;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.knowledgemodeling.constraints.MaxEntConstraint;
import de.unibonn.realkd.knowledgemodeling.learning.ConnectedComponent;
import de.unibonn.realkd.knowledgemodeling.learning.ValueSpaceUtility;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;

/**
 * @author Bo Kang
 * 
 * @since 0.1.0
 * 
 * @version 0.1.0
 * 
 */
public class UserKnowledgeModel {

	private List<MaxEntConstraint> constraints;

	private List<ConnectedComponent> connectedComponents;

	private DataTable dataTable;

	public UserKnowledgeModel(DataTable dataTable, List<ConnectedComponent> connectedComponents) {
		this.dataTable = dataTable;
		this.constraints = new ArrayList<>();
		this.connectedComponents = connectedComponents;
	}

	public double getExpectedMeasurement(Pattern<?> pattern) {
		checkArgument(pattern.descriptor() instanceof LogicalDescriptor, "Assuming logically described pattern.");
		LogicalDescriptor descriptor = (LogicalDescriptor) pattern.descriptor();
		HashSet<AttributeBasedProposition> attributeBasedPropositions = new HashSet<>();
		descriptor.elements().forEach(p -> {
			checkArgument(p instanceof AttributeBasedProposition<?>, "All descriptor elements must be attribute-based");
			attributeBasedPropositions.add((AttributeBasedProposition) p);
		});

		// List<AttributeBasedProposition> propositions =
		// descriptor.getElements();
		if (constraints.size() == 0) {
			// return ValueSpaceUtility.getInstance()
			// .getFractionOfSupportValuesOfPropSet(
			// new HashSet<>(propositions), dataTable.getSize());
			return ValueSpaceUtility.getFractionOfSupportValuesOfPropSet(attributeBasedPropositions,
					dataTable.population().size());
		} else {
			return computeExpectedMeasurementByQuery(attributeBasedPropositions);
		}

	}

	public void update(List<MaxEntConstraint> constraints, List<ConnectedComponent> connectedComponents) {
		this.constraints = constraints;
		this.connectedComponents = connectedComponents;
	}

	// private double computeExpectedMeasurementByQuery(
	// LogicalDescriptor descriptor) {
	private double computeExpectedMeasurementByQuery(Collection<AttributeBasedProposition> propositions) {
		Map<ConnectedComponent, Set<Proposition>> connectedComponentWisePropositionLists = new HashMap<>();

		List<AttributeBasedProposition> independentPropositions = new ArrayList<>();
		for (AttributeBasedProposition<?> proposition : propositions) {
			boolean isContained = false;
			for (ConnectedComponent connectedComponent : connectedComponents) {
				Attribute<?> attribute = proposition.attribute();
				if (connectedComponent.getAttributesWithInConnectedComponent().contains(attribute)) {
					if (!connectedComponentWisePropositionLists.containsKey(connectedComponent)) {
						connectedComponentWisePropositionLists.put(connectedComponent, new HashSet<Proposition>());
					}
					connectedComponentWisePropositionLists.get(connectedComponent).add(proposition);
					isContained = true;
				}
			}
			if (!isContained) {
				independentPropositions.add(proposition);
			}
		}

		double measure = 1.;

		for (ConnectedComponent connectedComponent : connectedComponentWisePropositionLists.keySet()) {
			measure *= connectedComponent.computeSumOfMultipliersActivatedByWildCardProps(
					connectedComponentWisePropositionLists.get(connectedComponent))
					/ connectedComponent.getPartitionFunctionValue();
		}

		for (AttributeBasedProposition proposition : independentPropositions) {
			Set<AttributeBasedProposition> propositionSet = new HashSet<>();
			propositionSet.add(proposition);
			measure *= ValueSpaceUtility.getFractionOfSupportValuesOfPropSet(propositionSet,
					dataTable.population().size());
		}

		return measure;
	}
}

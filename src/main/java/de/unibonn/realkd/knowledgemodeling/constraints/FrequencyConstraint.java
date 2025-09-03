package de.unibonn.realkd.knowledgemodeling.constraints;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.Frequency;
import de.unibonn.realkd.patterns.LocalPatternDescriptor;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.TableSubspaceDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;

/**
 * @author Bo Kang
 * 
 * @since 0.1.0
 * 
 * @version 0.1.2.1
 * 
 */
public class FrequencyConstraint implements MaxEntConstraint {

	private final Pattern<?> pattern;

	private double multiplier;

	private final Set<Integer> rowIndices;

	private final Set<Integer> attributeIndices;

	private final List<AttributeBasedProposition<?>> associatedPropositions;

	/*
	 * WARNING: only defined when there is a datatable
	 */
	public FrequencyConstraint(Pattern<?> pattern) {
		checkArgument(pattern.descriptor() instanceof TableSubspaceDescriptor,
				"Pattern descriptor must describe subset of table attributes.");
		checkArgument(pattern.descriptor() instanceof LogicalDescriptor, "Pattern must be logically described.");
		((LogicalDescriptor) pattern.descriptor()).elements().forEach(p -> {
			checkArgument(p instanceof AttributeBasedProposition<?>,
					"All descriptor elements must be attribute-based propositions.");
		});

		this.pattern = pattern;
		this.multiplier = 0.;
		// TODO use IndexSet directly
		this.rowIndices = ImmutableSet.copyOf((pattern.descriptor() instanceof LocalPatternDescriptor)
				? ((LocalPatternDescriptor) pattern.descriptor()).supportSet() : pattern.population().objectIds());
		this.attributeIndices = new HashSet<>();
		for (Attribute<?> attribute : ((TableSubspaceDescriptor) pattern.descriptor()).getReferencedAttributes()) {
			DataTable table = ((TableSubspaceDescriptor) pattern.descriptor()).table();
			attributeIndices.add(table.attributes().indexOf(attribute));
		}

		this.associatedPropositions = new ArrayList<>();
		for (Proposition proposition : ((LogicalDescriptor) pattern.descriptor()).elements()) {
			associatedPropositions.add((AttributeBasedProposition<?>) proposition);
		}

	}

	public Pattern<?> getPattern() {
		return pattern;
	}

	public List<AttributeBasedProposition<?>> getAssociatedPropostions() {
		return associatedPropositions;
	}

	@Override
	public Set<Integer> getRowIndices() {
		return rowIndices;
	}

	@Override
	public Set<Integer> getAttributeIndices() {
		return attributeIndices;
	}

	@Override
	public double getMultiplier() {
		return multiplier;
	}

	@Override
	public void updateMultiplier(double newMultiplier) {
		this.multiplier = newMultiplier;
	}

	@Override
	public double getMeasurement() {
		return pattern.value(Frequency.FREQUENCY);
	}

	@Override
	public String getDescription() {
		return pattern.getClass().toString();
	}
}

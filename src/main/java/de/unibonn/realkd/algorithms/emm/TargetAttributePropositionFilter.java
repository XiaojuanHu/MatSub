package de.unibonn.realkd.algorithms.emm;

import java.util.List;
import java.util.function.Predicate;

import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 * Proposition predicate for EMM algorithms that wish to filter out propositions
 * that relate to their target attributes.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.2.1
 * 
 */
public class TargetAttributePropositionFilter implements Predicate<Proposition> {

	private final Parameter<DataTable> targetTableParameter;

	private final Parameter<List<Attribute<?>>> targetAttributesParameter;

	public TargetAttributePropositionFilter(Parameter<DataTable> targetTableParameter,
			Parameter<List<Attribute<?>>> targetAttributesParameter) {
		this.targetTableParameter = targetTableParameter;
		this.targetAttributesParameter = targetAttributesParameter;
	}

	/**
	 * @return false if and only if proposition relates to current value of
	 *         target attributes parameter
	 */
	public boolean test(Proposition proposition) {
		if (!(proposition instanceof AttributeBasedProposition)) {
			return true;
		}

		Attribute<?> attribute = ((AttributeBasedProposition<?>) proposition).attribute();
		List<Attribute<?>> targetAttributes = targetAttributesParameter.current();
		DataTable dataTable = targetTableParameter.current();
		if (targetAttributes.contains(attribute)
				|| dataTable.containsDependencyBetweenAnyOf(attribute, targetAttributes)) {
			return false;
		}
		return true;
	}

}

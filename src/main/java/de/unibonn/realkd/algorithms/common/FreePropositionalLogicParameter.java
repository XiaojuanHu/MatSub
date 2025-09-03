package de.unibonn.realkd.algorithms.common;

import static de.unibonn.realkd.common.base.Identifier.identifier;

import java.util.List;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.DefaultRangeEnumerableParameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.PropositionalContext;

/**
 * Propositional logic parameter that accepts any propositional logic in some
 * given data workspace.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.1.0
 * 
 */
public class FreePropositionalLogicParameter extends DefaultRangeEnumerableParameter<PropositionalContext> {

	/**
	 * 
	 */
	private static final Identifier ID = identifier("props");

	private static final String DESCRIPTION = "The collections of basic statements avaible to the algorithm to construct patterns.";

	private static final String NAME = "Propositions";

	public FreePropositionalLogicParameter(final Workspace dataWorkspace) {
		super(ID,NAME, DESCRIPTION, PropositionalContext.class, new RangeComputer<PropositionalContext>() {
			@Override
			public List<PropositionalContext> get() {
				return dataWorkspace.propositionalContexts();
			}
		});
	}

}

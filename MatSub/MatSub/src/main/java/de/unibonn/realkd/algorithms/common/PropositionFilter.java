package de.unibonn.realkd.algorithms.common;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContext;

/**
 * Interface for proposition filter strategies. Can be used for algorithm that
 * want to allow the specification of a customized subset of propositions to be
 * used for mining.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 * 
 */
public interface PropositionFilter {

	public List<? extends Proposition> filter(PropositionalContext propositionalLogic);

	/**
	 * Default proposition filter that does not filter out any proposition.
	 */
	public static PropositionFilter NO_FILTER = new PropositionFilter() {
		@Override
		public List<? extends Proposition> filter(PropositionalContext propositionalLogic) {
			return propositionalLogic.propositions();
		}
	};

	public static PropositionFilter fromPredicate(Predicate<? super Proposition> filterPredicate) {
		return new PropositionFilter() {

			@Override
			public List<Proposition> filter(PropositionalContext propositionalLogic) {
				return propositionalLogic.propositions().stream().filter(filterPredicate)
						.collect(Collectors.toList());
			}
			
		};
	}

}

package de.unibonn.realkd.algorithms.emm.dssd;

import static de.unibonn.realkd.common.base.Identifier.identifier;

import java.util.function.Predicate;

import de.unibonn.realkd.algorithms.common.NumberOfResultsParameter;
import de.unibonn.realkd.common.parameter.DefaultParameter;

/**
 * Number of intermediate results for
 * {@link de.unibonn.realkd.algorithms.emm.dssd.DiverseSubgroupSetDiscovery
 * DSSD}, i.e. a number of subgroups that are passed from beam search to the
 * post-selector.
 *
 * @author Vladimir Dzyuba, KU Leuven
 */
public class NumberOfIntermediateResultsParameter extends
		DefaultParameter<Integer> {
	private static final String NAME = "Number of intermediate results";
	private static final String DESCRIPTION = "Number of subgroups that are passed from beam search to the post-selector";
	private static final int DEFAULT = 10000;
	private static final String HINT = "Must be positive";

	public NumberOfIntermediateResultsParameter(
			final NumberOfResultsParameter numberOfResultsParameter) {
		super(identifier("num_inter_res"),NAME, DESCRIPTION, Integer.class, DEFAULT, input -> Integer
				.valueOf(input), new LargerThanNumberOfResultsValidator(
				numberOfResultsParameter), HINT, numberOfResultsParameter);
	}

	private static final class LargerThanNumberOfResultsValidator implements
			Predicate<Integer> {
		private final NumberOfResultsParameter mainParameter;

		public LargerThanNumberOfResultsValidator(
				final NumberOfResultsParameter mainParameter) {
			this.mainParameter = mainParameter;
		}

		@Override
		public boolean test(final Integer value) {
			return value > mainParameter.current();
		}
	}
}

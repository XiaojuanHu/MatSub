package de.unibonn.realkd.algorithms.emm.dssd;

import static de.unibonn.realkd.common.base.Identifier.identifier;

import java.util.function.Predicate;

import de.unibonn.realkd.common.parameter.DefaultParameter;

/**
 * Maximal search depth parameter for
 * {@link de.unibonn.realkd.algorithms.emm.dssd.DiverseSubgroupSetDiscovery
 * DSSD}, currently interpreted as a constraint on the pattern description
 * length.
 *
 * @author Vladimir Dzyuba, KU Leuven
 */
public class MaxDepthParameter extends DefaultParameter<Integer> {
	private static final String NAME = "Max.depth";
	private static final String DESCRIPTION = "Maximal search depth, i.e. maximal length of pattern descriptions";
	private static final int DEFAULT = 5;
	private static final String HINT = "Must be non-negative, where 0 implies no limit.";

	private static Predicate<Integer> MUST_NOT_BE_NEGATIVE = v-> v >= 0;

	public MaxDepthParameter() {
		super(identifier("max_depth"), NAME, DESCRIPTION, Integer.class, DEFAULT, input -> Integer
				.valueOf(input), MUST_NOT_BE_NEGATIVE, HINT);
	}
}

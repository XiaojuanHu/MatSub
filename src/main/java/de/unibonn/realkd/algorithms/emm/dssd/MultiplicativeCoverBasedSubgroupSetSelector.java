package de.unibonn.realkd.algorithms.emm.dssd;

import static de.unibonn.realkd.common.base.Identifier.id;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.parameter.DefaultParameter;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.ParameterContainer;

/**
 * <p>
 * Multiplicative cover-based subgroup selection.
 * </p>
 * The coverage bonus is computed as {@code Math.pow(weight, coverCount)}.
 *
 * @see de.unibonn.realkd.algorithms.emm.dssd.CoverBasedSubgroupSetSelector
 *
 * @author Vladimir Dzyuba, KU Leuven
 */
public class MultiplicativeCoverBasedSubgroupSetSelector extends
		CoverBasedSubgroupSetSelector implements ParameterContainer {
	protected final Parameter<Double> weight;

	/**
	 * Names of parameters have to be unique within one algorithm, hence
	 * <code>phase</code> can be used to enforce uniqueness for 'alpha'. E.g. in
	 * DSSD, <code>phase = "beam selection"</code> is used for
	 * {@link DiverseSubgroupSetDiscovery#beamSelectorParameter} and
	 * <code>phase = "post-selection"</code> is used for
	 * {@link DiverseSubgroupSetDiscovery#postSelectorParameter}.
	 *
	 * @param weight
	 *            alpha/weight parameter
	 * @param phase
	 *            Name of the phase when the selector is applied
	 */
	public MultiplicativeCoverBasedSubgroupSetSelector(final double weight,
			final String phase) {
		this.weight = new DefaultParameter<>(id("alpha"),
				"alpha, " + phase,
				"Weight parameter for multiplicative cover-based subgroup set selection",
				Double.class, weight, input -> Double.valueOf(input),
				(value -> value > 0.0 && value < 1.0),
				"Must be between 0 and 1 non-inclusive");
	}

	/*
	 * public MultiplicativeCoverBasedSubgroupSetSelector() { this(0.9,
	 * "set selection"); }
	 */

	@Override
	protected double bonusForAlreadyCoveredRecord(final int count) { // weight ^
																		// count
		double update = weight.current();
		for (int i = 0; i < count - 1; i++)
			update *= weight.current();

		return update;
	}

	@Override
	public String toString() {
		return "Multiplicative cover-based selector";
	}

	@Override
	public List<Parameter<?>> getTopLevelParameters() {
		return ImmutableList.of(weight);
	}
}

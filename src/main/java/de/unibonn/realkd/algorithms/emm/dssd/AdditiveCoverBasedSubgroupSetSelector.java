package de.unibonn.realkd.algorithms.emm.dssd;

/**
 * <p>Additive cover-based subgroup selection.</p>
 * The coverage bonus is computed as {@code 1.0 / (1 + coverCount)}.
 *
 * @see de.unibonn.realkd.algorithms.emm.dssd.CoverBasedSubgroupSetSelector
 *
 * @author Vladimir Dzyuba, KU Leuven
 */
public class AdditiveCoverBasedSubgroupSetSelector extends CoverBasedSubgroupSetSelector {
    @Override
    protected double bonusForAlreadyCoveredRecord(final int count) {
        return 1. / (1 + count);
    }

    @Override
    public String toString() {
        return "Additive cover-based selector";
    }
}

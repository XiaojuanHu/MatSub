package de.unibonn.realkd.algorithms.emm.dssd;

/**
 * <p>Sequential cover-based subgroup selection.</p>
 * The coverage bonus is always equals to {@code 0}, i.e.
 * subgroups do not receive any bonus for covering an already covered record.
 *
 * @see de.unibonn.realkd.algorithms.emm.dssd.CoverBasedSubgroupSetSelector
 *
 * @author Vladimir Dzyuba, KU Leuven
 */
public class SequentialCoverBasedSubgroupSetSelector extends CoverBasedSubgroupSetSelector {
    @Override
    protected double bonusForAlreadyCoveredRecord(final int count) {
        return 0.0;
    }

    @Override
    public String toString() {
        return "Sequential cover-based selector";
    }
}

package de.unibonn.realkd.algorithms.emm.dssd;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.realkd.patterns.Pattern;

/**
 * <p>Quality-based subgroup set selector.</p>
 *
 * It does not perform any diverse set selection and simply returns k best patterns.
 * Therefore, it is equivalent to the standard, non-diverse beam search.
 *
 * @see de.unibonn.realkd.algorithms.emm.dssd.SubgroupSetSelector
 *
 * @author Vladimir Dzyuba, KU Leuven
 */
public class QualityBasedSubgroupSetSelector extends SubgroupSetSelector {
    @Override
    /**
     * Simply returns {@code k} best candidates.
     *
     * @see de.unibonn.realkd.algorithms.emm.dssd.SubgroupSetSelector#selectDiverseSet
     */
    protected List<Pattern<?>> selectDiverseSet(final List<Pattern<?>> candidates, final int k) {
        final List<Pattern<?>> results = new ArrayList<>(k);
        for (int i = 0; i < k; i++)
            results.add(candidates.get(i));

        return results;
    }

    @Override
    public String toString() {
        return "Quality-based selector";
    }
}

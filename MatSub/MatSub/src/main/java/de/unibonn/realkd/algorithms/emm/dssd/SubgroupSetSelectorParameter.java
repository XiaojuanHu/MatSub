package de.unibonn.realkd.algorithms.emm.dssd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.DefaultRangeEnumerableParameter;

/**
 * @author Vladimir Dzyuba, KU Leuven
 */
public class SubgroupSetSelectorParameter extends DefaultRangeEnumerableParameter<SubgroupSetSelector> {
	private static final List<SubgroupSetSelector> OPTIONS;
	static {
		OPTIONS = new ArrayList<>(4);
		OPTIONS.add(SubgroupSetSelector.QUALITY);
		OPTIONS.add(SubgroupSetSelector.DESCRIPTION);
		OPTIONS.add(SubgroupSetSelector.COVER_SEQUENTIAL);
		OPTIONS.add(SubgroupSetSelector.COVER_ADDITIVE);
	}

	private static RangeComputer<SubgroupSetSelector> rangeComputer(final String phase) {
		final List<SubgroupSetSelector> effectiveOptions = new ArrayList<>(5);
		effectiveOptions.addAll(OPTIONS);
		effectiveOptions.add(new MultiplicativeCoverBasedSubgroupSetSelector(0.9, phase));

		final List<SubgroupSetSelector> ul = Collections.unmodifiableList(effectiveOptions);
		return () -> ul;
	}

	public SubgroupSetSelectorParameter(Identifier id, String name, String description, String phase) {
		super(id, name, description, SubgroupSetSelector.class, rangeComputer(phase));
	}
}

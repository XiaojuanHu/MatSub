package de.unibonn.realkd.algorithms.common;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.Frequency;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;

public interface PatternConstraint {

	public abstract boolean satisfies(Pattern<?> pattern);

	public static PatternConstraint POSITIVE_FREQUENCY = new PatternConstraint() {

		@Override
		public boolean satisfies(Pattern<?> pattern) {
			checkArgument(pattern.hasMeasure(Frequency.FREQUENCY),
					"Constraint only defined for patterns with frequency.");
			// if (!(pattern instanceof LogicallyDescribedLocalPattern)) {
			// throw new IllegalArgumentException(
			// "Constraint only defined for logically described pattern");
			// }
			return pattern.value(Frequency.FREQUENCY) > 0;
		}
	};

	public static PatternConstraint DESCRIPTOR_DOES_NOT_CONTAIN_TWO_ELEMENTS_REFERRING_TO_SAME_META_ATTRIBUTE = new PatternConstraint() {

		@Override
		public boolean satisfies(Pattern<?> pattern) {
			if (!(pattern.descriptor() instanceof LogicalDescriptor)) {
				throw new IllegalArgumentException("Constraint only defined for logically described pattern");
			}

			LogicalDescriptor description = (LogicalDescriptor) pattern.descriptor();

			List<Attribute<?>> prevAttrs = new ArrayList<>();
			for (Proposition p : description) {
				if (!(p instanceof AttributeBasedProposition<?>)) {
					continue;
				}
				AttributeBasedProposition<?> _p = (AttributeBasedProposition<?>) p;
				if (_p.table().containsDependencyBetweenAnyOf(_p.attribute(), prevAttrs)) {
					return false;
				}
				prevAttrs.add(_p.attribute());
			}
			return true;
		
		}
	};

	public static class MinimumMeasureValeConstraint implements PatternConstraint {

		private final double threshold;

		private final Measure measure;

		public MinimumMeasureValeConstraint(Measure interestingnessMeasure, double threshold) {
			this.threshold = threshold;
			this.measure = interestingnessMeasure;
		}

		@Override
		public boolean satisfies(Pattern<?> pattern) {
			if (!pattern.hasMeasure(measure)) {
				throw new IllegalArgumentException(
						"Constraint only defined for patterns that have " + measure.caption());
			}
			return pattern.value(measure) >= threshold;
		}

	}

}

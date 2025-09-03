package de.unibonn.realkd.algorithms.emm.dssd;

import static com.google.common.base.Preconditions.checkArgument;

import de.unibonn.realkd.algorithms.common.PatternConstraint;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;

/**
 * @author Vladimir Dzyuba, KU Leuven
 */
public final class MaxDescriptionLengthConstraint implements PatternConstraint {
	private final int maxLength;

	public MaxDescriptionLengthConstraint(final int maxLength) {
		this.maxLength = maxLength;
	}

	@Override
	public boolean satisfies(final Pattern<?> pattern) {
		checkArgument(pattern instanceof ExceptionalModelPattern);
		final int descriptionLength = ((ExceptionalModelPattern) pattern)
				.descriptor().extensionDescriptor().size();
		return descriptionLength <= maxLength;
	}
}

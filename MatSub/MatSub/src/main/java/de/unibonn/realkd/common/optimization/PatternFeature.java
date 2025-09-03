package de.unibonn.realkd.common.optimization;

import de.unibonn.realkd.patterns.Pattern;

/**
 * 
 * @author Mario Boley
 * 
 * @deprecated
 *
 */
public interface PatternFeature extends LinearFeature<Pattern<?>> {

	public enum Category {
		NONE, TYPE_OF_KNOWLEDGE, MAGNITUDE_OF_INTERESTINGNESS, DOMAIN_SPECIFIC_PREFERENCE;
	}

	/**
	 * Returns textual description of feature that can, e.g., be displayed in
	 * research view.
	 */
	public String getDescription();

	/**
	 * Computes the feature value for a single pattern. Must be 0 for
	 * Pattern.ZERO_PATTERN.
	 * 
	 */
	@Override
	public double value(Pattern<?> pattern);

	public Category getCategory();

}
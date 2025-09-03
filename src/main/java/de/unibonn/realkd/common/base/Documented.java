package de.unibonn.realkd.common.base;

/**
 * Interface of objects that provide a user-interpretable description of
 * themselves.
 * 
 * @author Mario Boley
 *
 * @since 0.1.1
 * 
 * @version 0.5.0
 * 
 */
public interface Documented {

	/**
	 * @return name that is short, expressive, and user-interpretable
	 * 
	 */
	public String caption();

	/**
	 * 
	 * @return short version of name that can be used wherever space is scarce
	 */
	public default String symbol() {
		return caption();
	}

	/**
	 * @return extended description of object that is user-interpretable
	 */
	public String description();

}

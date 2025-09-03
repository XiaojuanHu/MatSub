package de.unibonn.realkd.algorithms.derived;

import de.unibonn.realkd.common.parameter.Parameter;

/**
 * Parameter terminators automatically set one specific mining parameter to a
 * valid value.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @see Parameter
 * 
 */
public interface ParameterTerminator extends ParameterWrapper {

	/**
	 * Determines and sets parameter value based on implementation on selector.
	 */
	public void setParameter();



}

package de.unibonn.realkd.knowledgemodeling.constraints;

import java.util.Set;

/**
 * Represents constraints on the dataset of the form function=value; the
 * function part is only represented in the concrete implementations because it
 * varies with the form of the contraint.
 * 
 * @author bkang
 * 
 */
public interface MaxEntConstraint {
	
	public Set<Integer> getRowIndices();
	
	public Set<Integer> getAttributeIndices();
	
	public double getMultiplier();
	
	public void updateMultiplier(double newMultiplier);
	
	public double getMeasurement();
	
	public String getDescription();
}

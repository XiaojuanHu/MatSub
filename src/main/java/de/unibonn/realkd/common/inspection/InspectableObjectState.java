package de.unibonn.realkd.common.inspection;

import java.util.List;

/**
 * Provide the internal state of an {@link Inspectable} with a table and a table
 * header.
 * 
 * @author Bo Kang
 * 
 * @since 0.1.0
 * 
 * @version 0.1.0.1
 *
 */
public interface InspectableObjectState {

	public List<String> getHeader();

	public List<List<String>> getTable();

}

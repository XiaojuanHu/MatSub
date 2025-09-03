package de.unibonn.realkd.common.inspection;

import java.util.List;

import de.unibonn.realkd.common.base.Documented;

/**
 * <p>
 * Unified interface for classes that want to expose internal state for
 * inspection. In addition to name and description (inhereted from
 * {@link Documented}, information is provided by an {@link InspectableObjectState}
 * , which essentially is a dynamic table.
 * </p>
 * <p>
 * Also, an inspectable can have several sub-inspectables, for which the state
 * can be retrieved in a recursive manner.
 * </p>
 * 
 * @author Bo Kang
 * 
 * @since 0.1.0
 * 
 * @version 0.1.1.1
 * 
 */
public interface Inspectable extends Documented {

	public List<Inspectable> getSubInspectables();

	public InspectableObjectState getInspectableState();

}

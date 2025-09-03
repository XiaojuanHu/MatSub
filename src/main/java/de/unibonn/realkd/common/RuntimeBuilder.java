package de.unibonn.realkd.common;

import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.ParameterContainer;

/**
 * Interface for classes that can build domain objects of a certain type within
 * a certain context. While typically domain objects are supposed to be
 * immutable and consistently initialized, builders typically can be persisted
 * and gradually brought into a valid state. Since this process can involve the
 * user, builders often also should implement the interface
 * {@link ParameterContainer}.
 * 
 * @author Mario Boley
 *
 * @param <T>
 *            type of objects created by builder
 * @param <K>
 *            type of context required for construction
 * 
 * @since 0.1.2
 * 
 * @version 0.1.2
 * 
 */
public interface RuntimeBuilder<T, K> {

	public T build(K context) throws ValidationException;

}

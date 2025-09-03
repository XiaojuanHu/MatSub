/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.unibonn.realkd.common.parameter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import de.unibonn.realkd.common.base.Identifier;

/**
 * <p>
 * Interface for documented mutable fields (parameters) of objects of type
 * {@link ParameterContainer} (a list with elements of this type is returned by
 * getParameters() of). A parameter wraps a value of a generic type, allows to
 * get and set this value, and documents its function for being displayed in a
 * user interface. Moreover, it provides a validity check for the currently set
 * value.
 * </p>
 * <p>
 * Clients of type {@link ParameterListener} can subscribe for value change
 * notifications.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @see ParameterContainer
 * @see DependentParameter
 * @see ParameterListener
 * 
 * @param <T>
 *            is the type of the parameter values
 * 
 * @since 0.1.0
 * 
 * @version 0.6.0
 * 
 */
public interface Parameter<T> {

	/**
	 * @return identifier of parameter that must be unique within enclosing
	 *         container
	 */
	public Identifier id();

	/**
	 * @return the name of the parameter in a form that could be displayed by a user
	 *         interface
	 */
	public String getName();

	/**
	 * @return a brief description of a parameter that can be displayed by a user
	 *         interface
	 */
	public String getDescription();

	/**
	 * Provides the type argument at runtime.
	 * 
	 * @return the type that possible values have to be of
	 */
	public Class<?> getType();

	/**
	 * Sets the value of this parameter. Invalid values can be set.
	 */
	public void set(T value);

	/**
	 * <p>
	 * Sets the value of this parameter parsed from a string.
	 * </p>
	 * <p>
	 * <b>Update:</b> since version 0.2.1 needs to satisfy that
	 * setByString(getCurrentValue().toString()) results in equivalent state (if
	 * current value not null).
	 * </p>
	 * 
	 * @throws IllegalArgumentException
	 *             if argument can not be parsed.
	 */
	public void setByString(String value);

	/**
	 * @return the current value of this parameter (possibly null)
	 */
	public T current();

	/**
	 * 
	 * @return current value as an optional (empty if current value is null)
	 * 
	 * @since 0.2.1
	 */
	public default Optional<T> get() {
		return Optional.ofNullable(current());
	}

	/**
	 * <p>
	 * Validity check for currently set value. This may depend on current context,
	 * i.e., result of method can change for the same value.
	 * </p>
	 * <p>
	 * Rules for this check are: If the context is invalid (
	 * {@link #isContextValid()} is false) then the value of this can never be
	 * valid, a value not instance of {@link getType} (including null) can never be
	 * valid, and a value that is again instance of {@link ParameterContainer} in
	 * invalid state can never be valid.
	 * </p>
	 * 
	 * @return weather the current value is valid for this parameter in current
	 *         context
	 */
	public boolean isValid();

	/**
	 * A textual hint of how this parameter has to be set/modified in order to make
	 * it valid. Returned string may depend on currently set value or just provide
	 * general static information.
	 */
	public String getValueCorrectionHint();

	/**
	 * Registers a dependent object for which
	 * {@link ParameterListener#notifyValueChanged(Parameter)} will be called with
	 * this as argument whenever value of this changes.
	 */
	public void addListener(ParameterListener listener);

	/**
	 * Unregisters a dependent object if that object has been registered with
	 * {@link #addListener(ParameterListener)} previously (does nothing otherwise).
	 */
	public void removeListener(ParameterListener listener);

	/**
	 * @return true if and only if all dependsOnParameters have valid values.
	 */
	public boolean isContextValid();

	/**
	 * Returns parameters that this object depends on. Within a specific parameter
	 * container parameters are only allowed to have dependencies to siblings.
	 * 
	 * @return list of parameters this objects depends on.
	 */
	public List<Parameter<?>> getDependsOnParameters();

	public default void dfsTraverseDependentParameters(Consumer<Parameter<?>> action) {
		for (Parameter<?> parameter : getDependsOnParameters()) {
			action.accept(parameter);
			parameter.dfsTraverseDependentParameters(action);
		}
	}

	public default Set<Parameter<?>> getDependsOnParametersTransitively() {
		Set<Parameter<?>> result = new HashSet<>();
		dfsTraverseDependentParameters(result::add);
		return result;
	}

	/**
	 * Whether parameter is supposed to be hidden from user. Hidden parameters might
	 * still be referenced by value assignments.
	 * 
	 * @since 0.3.2
	 * 
	 */
	public boolean hidden();

}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 University of Bonn
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

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 University of Bonn
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.base.ValidationException;

/**
 * <p>
 * Interface of objects that contain mutable fields (parameters) that are
 * supposed to be manipulated at runtime and for which a consistency check and
 * documentation is provided. Parameters within a container are ordered to take
 * into account parameter dependencies: if a parameter x dependents on another
 * parameter y then y must precede x in the order.
 * </p>
 * 
 * <p>
 * Values of parameters can again be parameter containers in some cases. Hence
 * some methods work in recursive fashion.
 * </p>
 * 
 * @see Parameter
 * @see DependentParameter
 * 
 * @author Björn Jacobs
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.6.0
 * 
 */
public interface ParameterContainer {

	public static final Logger LOGGER = Logger.getLogger(ParameterContainer.class.getName());

	/**
	 * @return List of parameters in an order that is compatible with parameter
	 *         dependencies. Parameters can have mixed type arguments.
	 */
	public List<Parameter<?>> getTopLevelParameters();

	/**
	 * Finds reference to parameter with specified name within container. This
	 * includes recursively nested parameters that are currently available as
	 * parameters of values of parameters. Search proceeds in depth-first order.
	 * Consequently, parameters can be hidden by (nested parameters of) parameters
	 * that occur earlier in parameter list.
	 * 
	 * @return the parameter with the specified name.
	 * @throws IllegalArgumentException
	 *             if no such parameter present.
	 * 
	 */
	public default Parameter<?> findParameterByName(String name) {
		// for (Parameter<?> parameter : getTopLevelParameters()) {
		// if (parameter.getName().equals(name)) {
		// return parameter;
		// } else if (parameter.current() instanceof ParameterContainer) {
		// Parameter<?> nestedParameterWithName = null;
		// try {
		// nestedParameterWithName = ((ParameterContainer)
		// parameter.current()).findParameterByName(name);
		// } catch (IllegalArgumentException ignored) {
		// ;
		// } finally {
		// if (nestedParameterWithName != null) {
		// return nestedParameterWithName;
		// }
		// }
		// }
		// }
		// throw new IllegalArgumentException("Parameter of name '" + name + "'
		// is unknown for '" + this + "'.");

		Optional<Parameter<?>> result = getAllParameters().stream().filter(p -> p.getName().equals(name)).findFirst();
		return result.orElseThrow(
				() -> new IllegalArgumentException("Parameter of name '" + name + "' is unknown for '" + this + "'."));
	}

	/**
	 * @param id
	 *            identifier of desired parameter
	 * @return optional parameter or empty if no such parameter available
	 */
	public default Optional<Parameter<?>> parameter(Identifier id) {
		return getAllParameters().stream().filter(p -> p.id().equals(id)).findFirst();
	}

	/**
	 * Generates a list of all parameters for which values can be passed to this
	 * container. This includes nested parameters, i.e., parameters of current
	 * values of other parameters. List is returned in depth-first traversal order
	 * and as such it is compatible with the passing order of
	 * {@link #passValuesToParameters(Map)}.
	 * 
	 * @return List of all parameters resulting from depth first traversal
	 * 
	 */
	public default List<Parameter<?>> getAllParameters() {
		List<Parameter<?>> result = new ArrayList<Parameter<?>>();
		for (Parameter<?> parameter : getTopLevelParameters()) {
			// TODO: since traversal order is supposed to be DFS, following
			// lines should not be able to detect cyclic dependency and have
			// thus been commented out.
			// --------
			//
			// if (result.contains(parameter)) {
			// throw new IllegalStateException(
			// "cyclic parameter containment in container " + this + " involving
			// " + parameter.id());
			// }
			result.add(parameter);
			if (parameter.current() instanceof ParameterContainer) {
				result.addAll(((ParameterContainer) parameter.current()).getAllParameters());
			}
		}
		return result;
	}

	/**
	 * Convenience method that returns true if and only if the value of all
	 * contained parameters is valid.
	 * 
	 */
	public default boolean isStateValid() {
		for (Parameter<?> param : this.getTopLevelParameters()) {
			if (!param.isValid()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks all top-level parameters for validity and in case of finding an
	 * invalid one throws {@link ValidationException} with message pointing to first
	 * parameter with invalid value.
	 * 
	 * @throws ValidationException
	 * 
	 * @since 0.1.1
	 * @version 0.6.0
	 * 
	 */
	public default void validate() throws ValidationException {
		for (Parameter<?> param : getTopLevelParameters()) {
			if (!param.isValid()) {
				throw new ValidationException(
						"In container '" + this + "': " + "Value '" + param.current() + "' is invalid for parameter '"
								+ param.getName(),
						(param instanceof RangeEnumerableParameter
								? " " + ((RangeEnumerableParameter<?>) param).getRangeOptionString()
								: "") + "'.");
			}
		}
	}

	/**
	 * <p>
	 * Recursively sets all parameters specified in key set of map to the values
	 * specified by value map. Parameters are set in the order compatible with the
	 * order of parameters returned by {@link #getTopLevelParameters()}. In
	 * addition, depth first order is applied in case nested parameters are present,
	 * i.e., parameters of values of other parameters. This way a parameter value
	 * can first be completely transfered to a valid state before potentially
	 * dependent parameters are set.
	 * </p>
	 * <p>
	 * Catches exception raised due to illegal string values (may issue warning).
	 * </p>
	 * 
	 * @param nameValueMap
	 */
	public default void passValuesToParameters(Map<String, String> nameValueMap) {
		Map<String, String> crossOutMap = new HashMap<>(nameValueMap);
		this.unloadMapValuesToParameters(crossOutMap);
		crossOutMap.entrySet().forEach(
				e -> LOGGER.warning("Parameter value of " + e.getKey() + " could not be passed to container."));
	}

	/**
	 * Like {@link #passValuesToParameters(Map)} but based on parameter ids.
	 * 
	 * @param nameValueMap
	 */
	public default void pass(Map<Identifier, String> nameValueMap) {
		Map<Identifier, String> crossOutMap = new HashMap<>(nameValueMap);
		this.unload(crossOutMap);
		crossOutMap.entrySet().forEach(
				e -> LOGGER.warning("Parameter value of " + e.getKey() + " could not be passed to container."));
	}

	/**
	 * <p>
	 * Same as {@link #passValuesToParameters} but removes those key value pairs
	 * that have been used from input map. Can be used by clients in order to
	 * determine which parameters could be passed and which not.
	 * </p>
	 * <p>
	 * WARNING: this modifies the input argument.
	 * </p>
	 * 
	 * @param crossOutMap
	 */
	public default void unloadMapValuesToParameters(final Map<String, String> crossOutMap) {
		applyRecursively(p -> {
			if (crossOutMap.containsKey(p.getName())) {
				String value = crossOutMap.get(p.getName());
				try {
					p.setByString(value);
				} catch (IllegalArgumentException illegalArgumentException) {
					LOGGER.warning(String.format("'" + value + "' could not be parsed for '" + p.getName()
							+ "' (reason: " + illegalArgumentException.getMessage() + ")"));
				}
				// cross out used parameter
				crossOutMap.remove(p.getName());
			}
		});
		// // traverser in order provided by getParameters
		// for (Parameter<?> parameter : this.getTopLevelParameters()) {
		// if (crossOutMap.containsKey(parameter.getName())) {
		// String value = crossOutMap.get(parameter.getName());
		// try {
		// parameter.setByString(value);
		// } catch (IllegalArgumentException illegalArgumentException) {
		// LOGGER.warning("'" + value + "' could not be parsed for '" +
		// parameter.getName() + "' (reason: "
		// + illegalArgumentException.getMessage() + ")");
		// }
		// // cross out used parameter
		// crossOutMap.remove(parameter.getName());
		// }
		//
		// // recursion into current value if itself parameter container
		// if (parameter.current() instanceof ParameterContainer) {
		// ((ParameterContainer)
		// parameter.current()).unloadMapValuesToParameters(crossOutMap);
		// }
		// }
	}

	/**
	 * Like {@link #unloadMapValuesToParameters(Map)} but based on parameter ids.
	 * 
	 */
	public default void unload(final Map<Identifier, String> crossOutMap) {
		applyRecursively(p -> {
			if (crossOutMap.containsKey(p.id())) {
				String value = crossOutMap.get(p.id());
				try {
					p.setByString(value);
				} catch (IllegalArgumentException illegalArgumentException) {
					LOGGER.warning(String.format("'" + value + "' could not be parsed for '" + p.id() + "' (reason: "
							+ illegalArgumentException.getMessage() + ")"));
				}
				// cross out used parameter
				crossOutMap.remove(p.id());
			}
		});
	}

	/**
	 * <p>
	 * Applies some action recursively to all contained parameters and the
	 * parameters of current parameter-values.
	 * </p>
	 * <p>
	 * Application order is compatible with {@link #getTopLevelParameters()}
	 * recursing into value parameters as they occur _after_ application to
	 * containing parameter.
	 * </p>
	 * 
	 * @param action
	 *            the action to be applied to
	 * 
	 */
	public default void applyRecursively(Consumer<Parameter<?>> action) {
		for (Parameter<?> p : getTopLevelParameters()) {
			action.accept(p);
			if (p.current() instanceof ParameterContainer) {
				((ParameterContainer) p.current()).applyRecursively(action);
			}
		}
	}

	/**
	 * Convenience method for checking whether parameter of specific name can be
	 * found in container (is either top-level or nested parameter) via
	 * {@link #findParameterByName}. Search order is similar to
	 * {@link #findParameterByName}.
	 * 
	 * @param name
	 *            the parameter name to be checked
	 * 
	 * @return true if and only if parameter with name can be found via
	 *         {@link ParameterContainer#findParameterByName(String)}
	 * 
	 * @since 0.1.2
	 * 
	 */
	public default boolean hasParameter(String name) {
		for (Parameter<?> parameter : getTopLevelParameters()) {
			if (parameter.getName().equals(name)) {
				return true;
			} else if (parameter.current() instanceof ParameterContainer) {
				if (((ParameterContainer) parameter.current()).hasParameter(name)) {
					return true;
				}
			}
		}
		return false;
	}

}

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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.unibonn.realkd.common.base.Identifier;

/**
 * <p>
 * Base class for parameters with static name and description that can also
 * depend on other "upstream" parameters. As long as there is at least one
 * invalid upstream parameter, this is also considered invalid.
 * </p>
 * 
 * <p>
 * Can be extended by specific parameters in order to provide convenience
 * constructor and/or to add functionality corresponding to specializations of
 * the MiningParameter interface.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.2.1
 * 
 */
public class DefaultParameter<T> implements Parameter<T>, ParameterListener {

	private static final Logger LOGGER = Logger.getLogger(DefaultParameter.class.getName());

	private T value = null;

	private final Identifier id;

	private final String name;

	private final String description;

	private final Function<String, T> stringParser;

	private final Class<?> type;

	private final List<ParameterListener> listener;

	private final Parameter<?>[] dependsOnParameters;

	private final String solutionHint;

	private final Predicate<? super T> validator;

	private final Supplier<? extends T> initializer;

	private final Supplier<Boolean> hidden;

	/**
	 * 
	 * @param initValue
	 *            value with which this parameter is initialized (may be null)
	 * @param parser
	 *            string parser that is used by implementation of setByString
	 *            method
	 * @param validator
	 *            object to which value validation is delegated after initial
	 *            checks have been performed that are mandatory by the contract
	 *            of {@link Parameter}. These checks are
	 *            <ul>
	 *            <li>whether the context of the parameter is valid, all
	 *            upstream parameters that this parameter depends on are set to
	 *            valid values,
	 *            <li>whether the value is not null,
	 *            <li>whether the value is in a valid state if the value itself
	 *            is a parameter container.
	 *            </ul>
	 */
	public DefaultParameter(Identifier id, String name, String description, Class<?> type, Function<String, T> parser,
			Predicate<? super T> validator, String hint, Supplier<? extends T> initializer, Supplier<Boolean> hidden,
			Parameter<?>... dependsOnParameters) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.stringParser = parser;
		this.type = type;
		this.listener = new ArrayList<>();
		this.validator = validator;
		this.dependsOnParameters = dependsOnParameters;
		this.solutionHint = hint;
		this.initializer = initializer;
		this.hidden = hidden;

		if (initializer != null) {
			// TODO no way the listener registration should depend on the
			// initializer being non-null!??!!
			for (Parameter<?> parameter : dependsOnParameters) {
				parameter.addListener(this);
			}
			tryToInitialize();
		}
	}

	/**
	 * 
	 * @param initValue
	 *            value with which this parameter is initialized (may be null)
	 * @param parser
	 *            string parser that is used by implementation of setByString
	 *            method
	 * @param validator
	 *            object to which value validation is delegated
	 */
	public DefaultParameter(Identifier id, String name, String description, Class<?> type, T initValue, Function<String, T> parser,
			Predicate<? super T> validator, String hint, Parameter<?>... dependsOnParameters) {
		this(id, name, description, type, parser, validator, hint, null, () -> false, dependsOnParameters);
		this.value = initValue;
	}

	/**
	 * 
	 * @param initializer
	 *            supplier that is called to set value during parameter
	 *            construction and whenever the value of an upstream parameter
	 *            changes if (in both cases) context valid
	 * @param parser
	 *            string parser that is used by implementation of setByString
	 *            method
	 * @param validator
	 *            object to which value validation is delegated
	 */
	public DefaultParameter(Identifier id, String name, String description, Class<?> type, Function<String, T> parser,
			Predicate<? super T> validator, String hint, Supplier<T> initializer, Parameter<?>... dependsOnParameters) {
		this(id, name, description, type, parser, validator, hint, initializer, () -> false, dependsOnParameters);
	}

	private void tryToInitialize() {
		if (initializer != null && isContextValid()) {
			set(this.initializer.get());
		}
	}

	@Override
	public final Identifier id() {
		return id;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final String getDescription() {
		return description;
	}

	@Override
	public final Class<?> getType() {
		return type;
	}

	@Override
	public final T current() {
		return value;
	}

	@Override
	public final void set(T newValue) {
		// encapsulated field is allowed to hold "null/not yet set/invalid"
		// values

		if ((newValue == null && value == null)) {
			return;
		}

		if (newValue != null && newValue.equals(value)) {
			return;
		}

		this.value = newValue;

		for (ParameterListener dependentParameter : listener) {
			dependentParameter.notifyValueChanged(this);
		}
	}

	@Override
	public final void setByString(String strings) {
		if (stringParser == null) {
			throw new UnsupportedOperationException("Parameter '" + getName() + "' cannot be set by string.");
		}
		set(stringParser.apply(strings));
	}

	@Override
	public final void addListener(ParameterListener dependentParameter) {
		checkNotNull(dependentParameter);
		this.listener.add(dependentParameter);
	}

	public final List<Parameter<?>> getDependsOnParameters() {
		return new ArrayList<Parameter<?>>(Arrays.asList(dependsOnParameters));
	}

	@Override
	public final boolean isContextValid() {
		for (Parameter<?> param : this.dependsOnParameters) {
			if (!param.isValid()) {
				LOGGER.fine("invalid because '" + param.current() + "' is invalid for '" + param.getName() + "'.");
				return false;
			}
		}
		return true;
	}

	@Override
	public final String getValueCorrectionHint() {
		return solutionHint;
	}

	@Override
	public final boolean isValid() {
		// can never be valid if context (parameter this depends on) is not
		// valid
		if (!isContextValid()) {
			return false;
		}
		// null value is never valid
		if (current() == null) {
			return false;
		}
		// values that are parameter containers themselves in invalid state can
		// never be valid
		if (current() instanceof ParameterContainer && !((ParameterContainer) current()).isStateValid()) {
			return false;
		}
		return validator.test(current());
	}

	@Override
	public void notifyValueChanged(Parameter<?> parameter) {
		tryToInitialize();
	}

	@Override
	public void removeListener(ParameterListener listener) {
		this.listener.remove(listener);
	}

	@Override
	public boolean hidden() {
		return hidden.get();
	}

}

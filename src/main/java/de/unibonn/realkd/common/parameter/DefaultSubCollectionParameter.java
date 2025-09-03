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

import static de.unibonn.realkd.common.base.Identifier.identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.util.Strings;

/**
 * Default implementation of sub-collection parameter. Can be configured as
 * either sublist or subset parameter.
 * 
 * @author Mario Boley
 * @author Bo Kang
 * 
 * @since 0.1.0
 * 
 * @version 0.2.1
 * 
 */
public class DefaultSubCollectionParameter<G, T extends Collection<? extends G>>
		implements SubCollectionParameter<G, T> {

	private static Logger LOGGER = Logger.getLogger(DefaultSubCollectionParameter.class.getName());

	/**
	 * Implementation of StringParser that parses a single string in js format
	 * ([e1,e2,...]) to the sub-collection of objects of getCollection whose
	 * string representation corresponds to 'e1, e2,...'. If a matching element
	 * is not found for a string specifier it is ignored and a warning is
	 * logged.
	 */
	public static class JsonStringToCollectionParser<G, T extends Collection<G>> implements Function<String, T> {

		private final Supplier<Collection<G>> rangeCollectionSupplier;

		private final Function<List<G>, T> targetCollectionConverter;

		public JsonStringToCollectionParser(Supplier<Collection<G>> collectionComputer,
				Function<List<G>, T> targetCollectionConverter) {
			this.rangeCollectionSupplier = collectionComputer;
			this.targetCollectionConverter = targetCollectionConverter;
		}

		private Optional<G> findByString(String searchString) {
			Optional<Identifier> searchId = Identifier.isValidIdentifier(searchString) ? Optional.of(identifier(searchString))
					: Optional.empty();
			Collection<G> range = rangeCollectionSupplier.get();
			for (G element : range) {
				if ((searchId.isPresent() && element instanceof Identifiable
						&& ((Identifiable) element).identifier().equals(searchId.get())) || element.toString().trim().equals(searchString)) {
					return Optional.of(element);
				}
			}
			LOGGER.warning("Could not find element '" + searchString + "' in collection.");
			return Optional.empty();
		}

		@Override
		public T apply(String input) {
			List<String> argumentAsStrings = Strings.jsonArrayToStringList(input);
			Stream<Optional<G>> foundElementOptions = argumentAsStrings.stream().map(s -> findByString(s));
			List<G> elementsList = foundElementOptions.filter(o -> o.isPresent()).map(o -> o.get())
					.collect(Collectors.toList());
			return targetCollectionConverter.apply(elementsList);
		}
	}

	public static class ValidateIsSubcollection<G, T extends Collection<G>> implements Predicate<T> {

		private Supplier<T> listComputer;

		public ValidateIsSubcollection(Supplier<T> listComputer) {
			this.listComputer = listComputer;
		}

		@Override
		public boolean test(T value) {
			return listComputer.get().containsAll(value);
		}

	}

	private static final String HINT = "Choose an element from list";

	private final DefaultParameter<T> defaultParameter;

	private final Supplier<? extends T> rangeSupplier;

	DefaultSubCollectionParameter(Identifier id, String name, String description, Class<?> type, Supplier<? extends T> rangeSupplier,
			Function<String, T> parser, Predicate<T> validator, Supplier<T> initializer,
			Parameter<?>... dependenParams) {
		this.rangeSupplier = rangeSupplier;
		this.defaultParameter = new DefaultParameter<T>(id, name, description, type, parser, validator, HINT, initializer,
				dependenParams);
	}

	@Override
	public final T getCollection() {
		if (!isContextValid()) {
			@SuppressWarnings("unchecked")
			T res = (T) ImmutableSet.of();
			return res;
		}
		return rangeSupplier.get();
	}

	@Override
	public final boolean isContextValid() {
		return this.defaultParameter.isContextValid();
	}

	@Override
	public final List<Parameter<?>> getDependsOnParameters() {
		return this.defaultParameter.getDependsOnParameters();
	}

	@Override
	public final boolean isValid() {
		return this.defaultParameter.isValid();
	}

	@Override
	public final String getValueCorrectionHint() {
		return this.defaultParameter.getValueCorrectionHint();
	}

	public final Identifier id() {
		return this.defaultParameter.id();
	}

	@Override
	public final String getName() {
		return this.defaultParameter.getName();
	}

	@Override
	public final String getDescription() {
		return this.defaultParameter.getDescription();
	}

	@Override
	public final Class<?> getType() {
		return this.defaultParameter.getType();
	}

	@Override
	public final void set(T value) {
		this.defaultParameter.set(value);
	}

	@Override
	public final void setByString(String value) {
		this.defaultParameter.setByString(value);
	}

	@Override
	public final T current() {
		return defaultParameter.current();
	}

	private Map<ParameterListener, ParameterListener> listenerReferrer = new HashMap<>();

	@Override
	public final void addListener(final ParameterListener listener) {
		// adding referrer that notifies listener with update for this parameter
		// (instead of wrapped)
		ParameterListener referrer = new ParameterListener() {
			@Override
			public void notifyValueChanged(Parameter<?> parameter) {
				listener.notifyValueChanged(DefaultSubCollectionParameter.this);
			}
		};
		this.defaultParameter.addListener(referrer);

		// storing referrer for listener in order to support deregistration
		this.listenerReferrer.put(listener, referrer);
	}

	@Override
	public void removeListener(ParameterListener listener) {
		if (!listenerReferrer.containsKey(listener)) {
			return;
		}
		this.defaultParameter.removeListener(listenerReferrer.get(listener));
	}

	@Override
	public boolean hidden() {
		return defaultParameter.hidden();
	}

}

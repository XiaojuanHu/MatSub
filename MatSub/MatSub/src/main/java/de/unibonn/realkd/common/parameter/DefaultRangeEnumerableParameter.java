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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;

/**
 * <p>
 * Default implementation for RangeEnumerableParameters. Per definition the
 * value is valid whenever it is an element of the current set of options.
 * </p>
 * 
 * <p>
 * This check is currently performed through equals (as defined by
 * {@link List#contains(Object)}). WARNING: this was incorrectly documented as
 * using toString in previous versions (before 0.1.2.1).
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.6.0
 * 
 */
public class DefaultRangeEnumerableParameter<T> implements RangeEnumerableParameter<T> {

	Logger LOGGER = Logger.getLogger(DefaultRangeEnumerableParameter.class.getName());

	private class ElementOfRangeParser implements Function<String, T> {

		@Override
		public T apply(String input) {
			Optional<Identifier> id = Identifier.isValidIdentifier(input) ? Optional.of(identifier(input))
					: Optional.empty();
			List<? extends T> range = DefaultRangeEnumerableParameter.this.getRange();
			for (T element : range) {
				if ((id.isPresent() && element instanceof Identifiable
						&& ((Identifiable) element).identifier().equals(id.get())) || element.toString().trim().equals(input)) {
					return element;
				}
			}
			String rangeString = (id.isPresent()) ? range.stream()
					.map(e -> (e instanceof Identifiable) ? ((Identifiable) e).identifier().toString() : e.toString())
					.collect(Collectors.joining(",", "[", "]")) : range.toString();

			LOGGER.warning(
					String.format("did not find element '%s' in range; return null (range '%s')", input, rangeString));
			return null;
		}
	}

	private class ValidatorIsElementOfRange implements Predicate<T> {

		@Override
		public boolean test(T value) {
			return DefaultRangeEnumerableParameter.this.getRange().contains(value);
		}

	}

	public interface RangeComputer<T> extends Supplier<List<? extends T>> {

		/**
		 * Can be called whenever context is valid.
		 * 
		 * @return non-null range
		 */
		public List<? extends T> get();

	}

	private static final String HINT = "Choose an element from list";

	private final DefaultParameter<T> defaultParameter;

	private final Supplier<? extends List<? extends T>> rangeComputer;

	protected DefaultRangeEnumerableParameter(Identifier id, String name, String description, Class<?> type,
			Supplier<? extends List<? extends T>> rangeComputer, Supplier<Boolean> hidden,
			Parameter<?>... dependenParams) {
		this.rangeComputer = rangeComputer;
		this.defaultParameter = new DefaultParameter<T>(id, name, description, type, new ElementOfRangeParser(),
				new ValidatorIsElementOfRange(), HINT, () -> getFirstInRangeOrNull(), hidden, dependenParams);
	}

	protected DefaultRangeEnumerableParameter(Identifier id, String name, String description, Class<?> type,
			Supplier<? extends List<? extends T>> rangeComputer, Parameter<?>... dependenParams) {
		this.rangeComputer = rangeComputer;
		this.defaultParameter = new DefaultParameter<T>(id, name, description, type, new ElementOfRangeParser(),
				new ValidatorIsElementOfRange(), HINT, () -> getFirstInRangeOrNull(), dependenParams);
	}

	private T getFirstInRangeOrNull() {
		List<? extends T> currentRange = this.rangeComputer.get();
		if (!currentRange.isEmpty()) {
			return currentRange.get(0);
		} else {
//			LOGGER.warning("empty range; initialize to null");
			return null;
		}
	}

	@Override
	public final List<? extends T> getRange() {
		if (!isContextValid()) {
			return Arrays.asList();
		}
		return rangeComputer.get();
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

	@Override
	public final Identifier id() {
		return defaultParameter.id();
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
				listener.notifyValueChanged(DefaultRangeEnumerableParameter.this);
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
		// this.listenerReferrer.remove(listener);
	}

	@Override
	public boolean hidden() {
		return defaultParameter.hidden();
	}

}

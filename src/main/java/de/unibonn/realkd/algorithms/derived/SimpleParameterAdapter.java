/**
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
 *
 */
package de.unibonn.realkd.algorithms.derived;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.ParameterListener;

/**
 * <p>
 * Adapter that pushes forward values of an "outer" parameter to an "inner"
 * parameter. The outer parameter is published by the adapter as its own
 * parameter for a possible exposure to the user or other clients.
 * 
 * Values are pushed on adapter creation and on all updates of outer values. If
 * value of inner parameter is changed independently from the adapter, behavior
 * can be configured to be either:
 * </p>
 * <ul>
 * <li>a warning is issued (default), or</li>
 * <li>the new inner value is propagated to the outer parameter, or</li>
 * <li>a runtime exception is thrown.</li>
 * </ul>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.1.1
 * 
 */
public class SimpleParameterAdapter<T> implements ParameterListener,
		ParameterAdapter {

	private interface InnerChangeOption extends
			Function<SimpleParameterAdapter<?>, InnerChangeStrategy> {
		;
	}

	public static final InnerChangeOption INNERCHANGEOPTION_THROW_EXCEPTION = adapter -> adapter.new ThrowException();

	public static final InnerChangeOption INNERCHANGEOPTION_ISSUE_WARNING = adapter -> adapter.new PrintWarning();

	public static final InnerChangeOption INNERCHANGEOPTION_PROPAGATE_TO_OUTER = adapter -> adapter.new PropagateToOuter();

	private static class InvalidModificationException extends RuntimeException {

		private static final long serialVersionUID = 6895737044852554668L;

		public InvalidModificationException(String message) {
			super(message);
		}
	}

	private interface InnerChangeStrategy {
		public void activate();
	}

	private class ThrowException implements InnerChangeStrategy {
		@Override
		public void activate() {
			throw new InvalidModificationException(
					"Value of "
							+ inner.getName()
							+ " has been modified independent of encapsulating parameter "
							+ outer.getName() + ".");
		}
	};

	private class PrintWarning implements InnerChangeStrategy {
		@Override
		public void activate() {
			System.out.println("WARNING: wrapped parameter " + inner.getName()
					+ " has been changed to " + inner.current() + ".");
		}
	};

	private class PropagateToOuter implements InnerChangeStrategy {
		@Override
		public void activate() {
			outer.set(inner.current());
		}
	};

	private final Parameter<T> inner;

	private final Parameter<T> outer;

	private final ImmutableList<Parameter<?>> parameters;

	private final InnerChangeStrategy innerChangeStrategy;

	public SimpleParameterAdapter(Parameter<T> inner, Parameter<T> outer) {
		this(inner, outer, INNERCHANGEOPTION_ISSUE_WARNING);
	}

	public SimpleParameterAdapter(Parameter<T> inner, Parameter<T> outer,
			InnerChangeOption innerChangeOption) {
		this.inner = inner;
		this.outer = outer;
		this.parameters = ImmutableList.of(outer);
		this.innerChangeStrategy = innerChangeOption.apply(this);

		inner.set(outer.current());
		outer.addListener(this);
		inner.addListener(this);
	}

	@Override
	public void notifyValueChanged(Parameter<?> parameter) {
		checkArgument(parameter == inner || parameter == outer,
				"Adapter only accepts parameter updates from inner or outer parameter.");
		if (parameter == inner) {
			innerChangeStrategy.activate();
			// throw new InvalidModificationException(
			// "Inner parameter value has been modified independent of encapsulating adapater.");
		} else if (parameter == outer) {
			// try {
			inner.removeListener(this);
			inner.set(outer.current());
			inner.addListener(this);
			// } catch (InvalidModificationException exception) {
			// // authorized modification
			// ;
			// }
		}
	}

	@Override
	public List<Parameter<?>> getTopLevelParameters() {
		return parameters;
	}

	@Override
	public Parameter<?> getWrappedParameter() {
		return inner;
	}

}

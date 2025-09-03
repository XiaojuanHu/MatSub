/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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
package de.unibonn.realkd.lang.types;

import static com.google.common.base.Preconditions.checkArgument;
import static de.unibonn.realkd.common.base.DoubleValues.doubleValue;

import java.util.List;
import java.util.Optional;

/**
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.7.0
 *
 */
public class Types {

	public static NumericValue numericValue(double val) {
		return doubleValue(val);
	}

	public static StringValue stringValue(String val) {
		return new DefaultStringValue(val);
	}

	// public static <T> RsFunction<T> function() {
	// return new
	// }

	public interface RsFunctionComputation<T> {

		T perform(Object main, List<Object> mand, List<Optional<Object>> opt);

	}

	public static <T> FunctionDefinition<T> function(String name, Class<?> resultType, Class<?> mainArgType,
			List<String> mandArgNames, List<Class<?>> mandArgTypes, List<String> optArgNames,
			List<Class<?>> optArgTypes, RsFunctionComputation<T> computation) {
		checkArgument(mandArgTypes.size() == mandArgNames.size());
		checkArgument(optArgTypes.size() == optArgTypes.size());
		return new DefaultRsFunction<T>(name, resultType, mainArgType, mandArgNames, mandArgTypes, optArgNames,
				optArgTypes, computation);
	}

	private static final class DefaultRsFunction<T> implements FunctionDefinition<T> {

		private final String name;

		private final Class<?> resultType;

		private final Class<?> mainArgType;

		private final List<Class<?>> mandArgTypes;

		private final List<String> mandArgNames;

		private final List<Class<?>> optArgTypes;

		private final List<String> optArgNames;

		private final RsFunctionComputation<T> computation;

		private DefaultRsFunction(String name, Class<?> resultType, Class<?> mainArgType, List<String> mandArgNames,
				List<Class<?>> mandArgTypes, List<String> optArgNames, List<Class<?>> optArgTypes,
				RsFunctionComputation<T> computation) {
			this.name = name;
			this.resultType = resultType;
			this.mainArgType = mainArgType;
			this.mandArgNames = mandArgNames;
			this.mandArgTypes = mandArgTypes;
			this.optArgNames = optArgNames;
			this.optArgTypes = optArgTypes;
			this.computation = computation;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public Class<?> resultType() {
			return resultType;
		}

		@Override
		public Class<?> mainArgumentType() {
			return mainArgType;
		}

		@Override
		public List<Class<?>> otherMandatoryArgumentTypes() {
			return mandArgTypes;
		}

		@Override
		public List<Class<?>> otherOptionalArgumentTypes() {
			return optArgTypes;
		}

		@Override
		public List<String> mandatoryArgumentNames() {
			return mandArgNames;
		}

		@Override
		public List<String> optionalArguementNames() {
			return optArgNames;
		}

		@Override
		public T evaluate(Object main, List<Object> other, List<Optional<Object>> optOther) {
			return computation.perform(main, other, optOther);
		}

	}

	private final static class DefaultStringValue implements StringValue {

		private final String value;

		private DefaultStringValue(String val) {
			this.value = val;
		}

		@Override
		public String asString() {
			return value;
		}

		@Override
		public String toString() {
			return value;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof StringValue)) {
				return false;
			}
			return (this.value.equals(((StringValue) other).asString()));
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

	}

}

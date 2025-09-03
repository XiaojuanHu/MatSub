/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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
package de.unibonn.realkd.common.base;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.lang.types.NumericValue;

/**
 * <p>
 * Java types corresponding to the JSON type <i>number</i>. This implies that
 * all subtypes must have the corresponding shape.
 * </p>
 * <p>
 * Numeric values that cannot be represented in this shape (e.g., mathematical
 * constants, rational numbers) can be mapped to other subtypes of
 * {@link NumericValue}.
 * </p>
 * <p>
 * Potentially, this degree of freedom can also be used to correctly handle
 * values of the IEEE floating points standard that are not valid JSON number
 * values like NaN.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class DoubleValues {

	@KdonTypeName("number")
	@KdonDoc("Arbitrary JSON floating point literal -3.2, 0.0, 1, 12e23, and so on; corresponds to the standard JSON <strong>number</strong> type.")
	@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
	public interface DoubleValue extends NumericValue {
	}
	
	@KdonTypeName("posNum")
	@KdonDoc("Positive floating point literal 0.034, 12e23, and so on.")
	public interface PositiveDoubleValue extends DoubleValue {
	}

	public static final DoubleValue doubleValue(double val) {
		if (val>0) {
			return new PositiveDoubleValueCapture(val);
		}
		return new DefaultDoubleValue(val);
	}

	private final static class DefaultDoubleValueSerializer extends JsonSerializer<DefaultDoubleValue> {

		@Override
		public void serialize(DefaultDoubleValue value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			gen.writeNumber(value.asDouble());
		}

	}

	@JsonSerialize(using = DefaultDoubleValueSerializer.class)
	private static class DefaultDoubleValue implements DoubleValue {

		private final double value;

		private DefaultDoubleValue(double val) {
			this.value = val;
		}

		@Override
		public String asString() {
			return String.valueOf(value);
		}

		@Override
		public double asDouble() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof NumericValue)) {
				return false;
			}
			return (this.value == ((NumericValue) other).asDouble());
		}

		@Override
		public int hashCode() {
			return Double.valueOf(value).hashCode();
		}

	}

	private final static class PositiveDoubleValueCapture extends DefaultDoubleValue implements PositiveDoubleValue {

		private PositiveDoubleValueCapture(double val) {
			super(val);
		}

	}

	private DoubleValues() {
		;
	}

}

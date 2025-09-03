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
package de.unibonn.realkd.lang.types;

import java.io.IOException;
import java.io.StringReader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import de.unibonn.realkd.common.JsonSerialization;
import de.unibonn.realkd.common.base.IntegerValues;

public class GenericStringValueDeserializer extends JsonDeserializer<StringValue> {

	@Override
	public StringValue deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		if (p.currentToken().equals(JsonToken.VALUE_NUMBER_FLOAT)) {
			return Types.numericValue(p.getDoubleValue());
		} else if (p.currentToken().equals(JsonToken.VALUE_NUMBER_INT)) {
			return IntegerValues.intValue(p.getIntValue());
		}
		return Types.stringValue(p.getValueAsString());
	}

	public static void main(String[] args) throws IOException {
		StringValue test = IntegerValues.intValue(3);
		String serialString = JsonSerialization.serialString(test);
		System.out.println(serialString);

		StringValue readValue = JsonSerialization.deserialization(new StringReader(serialString), StringValue.class);
		System.out.println(readValue);
	}

}
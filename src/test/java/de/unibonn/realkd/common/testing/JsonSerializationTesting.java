/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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
package de.unibonn.realkd.common.testing;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;

import de.unibonn.realkd.common.JsonSerialization;

/**
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.1.2
 *
 */
public class JsonSerializationTesting {
	
	private static Logger LOGGGER=Logger.getLogger(JsonSerializationTesting.class.toString());

	private JsonSerializationTesting() {
		; // utility class not to be instantiated
	}

	/**
	 * 
	 * @param object
	 *            the object for which serialization/deserialization is tested
	 * @param serializationClass
	 *            the class that is used for serialization/deserialization
	 * @throws IOException 
	 * 
	 */
	public static <T> void testJsonSerialization(T object,
			Class<? super T> serializationClass) throws IOException {
		String json = JsonSerialization.serialString(object);
		LOGGGER.info("Serialized form of "+object+": "+json);
		Object clone = JsonSerialization.deserialization(new StringReader(json), serializationClass);
		assertEquals(
				"Could not reconstruct equivalent object from: "+json,
				object, clone);
	}

}

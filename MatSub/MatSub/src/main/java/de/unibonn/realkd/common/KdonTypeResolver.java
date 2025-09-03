/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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
package de.unibonn.realkd.common;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Resolves types for JSON serialization with Jackson based on KDON type names
 * (obtained from {@link KdonTypes}). In case no {@link KdonType} is registered
 * for a Java type it resolves class names based on fully qualified name.
 * 
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.7.0
 *
 */
public final class KdonTypeResolver implements TypeIdResolver {

	private static final Logger LOGGER = Logger.getLogger(KdonTypeResolver.class.getName());

	private final ClassNameIdResolver classBasedResolver = new ClassNameIdResolver(TypeFactory.unknownType(),
			TypeFactory.defaultInstance());

	@Override
	public String getDescForKnownTypeIds() {
		return "full class names plus KDON type names"; // + ID_TO_CLASSNAME.keySet();
	}

	@Override
	public Id getMechanism() {
		return Id.CUSTOM;
	}

	@Override
	public String idFromBaseType() {
		return classBasedResolver.idFromBaseType();
	}

	@Override
	public String idFromValue(Object value) {
		Optional<String> kdonTypeName = KdonTypes.kdonTypeByJavaTypeName(value.getClass().getName()).map(t -> t.name);
		return kdonTypeName.orElse(classBasedResolver.idFromValue(value));
	}

	@Override
	public String idFromValueAndType(Object value, Class<?> clazz) {
		Optional<String> kdonTypeName = KdonTypes.kdonTypeByJavaTypeName(value.getClass().getName()).map(t -> t.name);
		return kdonTypeName.orElse(classBasedResolver.idFromValueAndType(value, clazz));
	}

	@Override
	public void init(JavaType clazz) {
		classBasedResolver.init(clazz);
	}

	@Override
	public JavaType typeFromId(DatabindContext context, String id) throws IOException {
		Optional<String> className = KdonTypes.kdonTypeByName(id).map(t -> t.javaTypeName);
		if (className.isPresent()) {
			try {
				return TypeFactory.defaultInstance().constructSpecializedType(TypeFactory.unknownType(),
						Class.forName(className.get()));
			} catch (ClassNotFoundException e) {
				LOGGER.severe(String.format("Could not find class '%1$s' for id '%2$s'", className, id));
			}
		}
		LOGGER.info(String.format("Could not find alias id '%1$s'; defaulting to class based resolver", id));
		return classBasedResolver.typeFromId(context, id);
	}

}
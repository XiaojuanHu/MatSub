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
package de.unibonn.realkd.common;

/**
 * Documented type for JSON serialization.
 * 
 * @author Mario Boley
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class KdonType {

	public final String name;
	
	private final KdonTypeParameter[] parameters;

	public final String javaTypeName;

	public final String[] supertypes;

	public final String[] subtypes;

	public final String description;

	public final KdonField[] fields;

	public KdonType(String name, String javaTypeName, String[] superTypes, String[] subtypes, String description,
			KdonField[] fields, KdonTypeParameter... parameters) {
		this.name = name;
		this.javaTypeName = javaTypeName;
		this.supertypes = superTypes;
		this.subtypes = subtypes;
		this.description = description;
		this.fields = fields;
		this.parameters=parameters;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String[] getSupertypes() {
		return supertypes;
	}

	public String[] getSubtypes() {
		return subtypes;
	}

	public KdonField[] getFields() {
		return fields;
	}

	public KdonTypeParameter[] getParameters() {
		return parameters;
	}

}
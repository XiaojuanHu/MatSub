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
 * Field of a {@link KdonType}.
 * 
 * @author Mario Boley
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class KdonField {

	public static class KdonFieldParameter {

		private final String value;

		private final boolean isExternal;

		private KdonFieldParameter(String value, boolean isExternal) {
			this.value = value;
			this.isExternal = isExternal;
		}

		public String getValue() {
			return value;
		}

		public boolean isExternal() {
			return isExternal;
		}

	}

	public static KdonFieldParameter fieldParameter(String value, boolean isExternal) {
		return new KdonFieldParameter(value, isExternal);
	}

	public final String name;

	public final KdonFieldParameter[] parameters;

	public final boolean optional;

	public final boolean typeExternal;

	public final String description;

	public final String type;

	public KdonField(String name, String description, String type, boolean typeExternal, boolean optional,
			KdonFieldParameter... parameters) {
		this.name = name;
		this.description = description;
		this.type = type;
		this.typeExternal = typeExternal;
		this.optional = optional;
		this.parameters = parameters;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getType() {
		return type;
	}

	public boolean isTypeExternal() {
		return typeExternal;
	}

	public boolean isOptional() {
		return optional;
	}

	public KdonFieldParameter[] getParameters() {
		return parameters;
	}

}
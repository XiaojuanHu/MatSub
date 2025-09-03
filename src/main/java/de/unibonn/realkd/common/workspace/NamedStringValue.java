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
package de.unibonn.realkd.common.workspace;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.lang.types.StringValue;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public class NamedStringValue
		implements StringValue, Entity, IdentifiableSerialForm<NamedStringValue>, HasSerialForm<NamedStringValue> {

	public static NamedStringValue value(Identifier id, String value) {
		return new NamedStringValue(id, id.toString(), "", value);
	}

	public static NamedStringValue value(Identifier id, String name, String description, String value) {
		return new NamedStringValue(id, name, description, value);
	}

	@JsonProperty("id")
	private final Identifier id;

	@JsonProperty("name")
	private final String name;

	@JsonProperty("description")
	private final String description;

	@JsonProperty("value")
	private final String value;

	@JsonCreator
	private NamedStringValue(@JsonProperty("id") Identifier id, @JsonProperty("name") String name,
			@JsonProperty("description") String description, @JsonProperty("value") String value) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.value = value;
	}

	@Override
	public Identifier identifier() {
		return id;
	}

	@Override
	public String caption() {
		return name;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public String asString() {
		return value;
	}

	@Override
	public SerialForm<? extends NamedStringValue> serialForm() {
		return this;
	}

	@Override
	public NamedStringValue build(Workspace workspace) {
		return this;
	}

}

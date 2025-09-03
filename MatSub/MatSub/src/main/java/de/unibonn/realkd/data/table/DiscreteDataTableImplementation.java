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
package de.unibonn.realkd.data.table;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroup;

/**
 * @author Mario Boley
 * 
 * @since 0.4.1
 * 
 * @version 0.5.0
 *
 */
final class DiscreteDataTableImplementation extends AbstractDataTable implements DiscreteDataTable {

	private final List<? extends CategoricAttribute<?>> attributes;

	private final Map<Identifier, ? extends CategoricAttribute<?>> attributeFromId;

	DiscreteDataTableImplementation(Identifier id, String name, String description, Population population,
			List<? extends CategoricAttribute<?>> attributes, List<AttributeGroup> attributeGroups) {
		super(id, name, description, population, attributeGroups);
		this.attributes = attributes;
		this.attributeFromId = attributes.stream().collect(Collectors.toMap(Attribute::identifier, a -> a));
	}

	@Override
	public List<? extends CategoricAttribute<?>> attributes() {
		return attributes;
	}

	@Override
	public CategoricAttribute<?> attribute(int attributeIndex) {
		return attributes.get(attributeIndex);
	}

	@Override
	public Optional<? extends CategoricAttribute<?>> attribute(Identifier identifier) {
		return Optional.ofNullable(attributeFromId.get(identifier));
	}

}

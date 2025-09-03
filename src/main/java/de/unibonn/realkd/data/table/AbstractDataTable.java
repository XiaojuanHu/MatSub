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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroup;
import de.unibonn.realkd.data.table.attributegroups.FunctionalGroup;

/**
 * <p>
 * Abstract base class for datatables that holds id, name, description,
 * population, and attribute groups.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.4.1
 * 
 * @version 0.4.1
 *
 */
abstract class AbstractDataTable implements DataTable {

	protected static class AttributeGroupStore {

		private List<AttributeGroup> attributeGroups;

		private final Map<Attribute<?>, Collection<AttributeGroup>> attributeGroupsOf = new LinkedHashMap<Attribute<?>, Collection<AttributeGroup>>() {

			private static final long serialVersionUID = 4216235288457674519L;

			public Collection<AttributeGroup> get(Object key) {
				Collection<AttributeGroup> list = super.get(key);
				if (list == null && key instanceof Attribute)
					super.put((Attribute<?>) key, list = new ArrayList<AttributeGroup>());
				return list;
			}
		};

		public AttributeGroupStore(List<AttributeGroup> groups) {
			this.attributeGroups = new ArrayList<>();
			groups.forEach(this::addAttributeGroup);
		}

		public List<AttributeGroup> groups() {
			return attributeGroups;
		}

		public Collection<AttributeGroup> getAttributeGroupsOf(Attribute<?> attribute) {
			return this.attributeGroupsOf.get(attribute);
		}

		private void addAttributeGroup(AttributeGroup group) {
			this.attributeGroups.add(group);
			for (Object attribute : group.elements()) {
				Collection<AttributeGroup> groups = this.attributeGroupsOf.get(attribute);
				groups.add(group);
			}
		}

		public boolean isPartOfMacroAttributeWithAtLeastOneOf(Attribute<?> attribute,
				Collection<? extends Attribute<?>> otherAttributes) {
			for (Attribute<?> otherAttribute : otherAttributes) {
				if (isPartOfMacroAttributeWith(attribute, otherAttribute)) {
					return true;
				}
			}
			return false;
		}

		public boolean isPartOfMacroAttributeWith(Attribute<?> attribute, Attribute<?> otherAttribute) {
			for (AttributeGroup group : getAttributeGroupsOf(attribute)) {
				if (!(group instanceof FunctionalGroup)) {
					continue;
				}
				if (group.elements().contains(otherAttribute)) {
					return true;
				}
			}

			return false;
		}
	}

	private final String name;
	private final String description;
	private final AttributeGroupStore attributeGroupsStore;
	private final Population population;
	private final Identifier id;
	private final String stringRepr;

	public AbstractDataTable(Identifier id, String name, String description, Population population,
			List<AttributeGroup> attributeGroups) {
		this.id = id;
		this.population = population;
		this.name = name;
		this.description = description;
		this.attributeGroupsStore = new AttributeGroupStore(attributeGroups);
		this.stringRepr = name;
	}

	@Override
	public Identifier identifier() {
		return id;
	}

	@Override
	public boolean atLeastOneAttributeValueMissingFor(int objectId) {
		return atLeastOneAttributeValueMissingFor(objectId, attributes());
	}

	@Override
	public boolean atLeastOneAttributeValueMissingFor(int objectId, List<? extends Attribute<?>> testAttributes) {
		for (Attribute<?> attribute : testAttributes) {
			if (attribute.valueMissing(objectId)) {
				return true;
			}
		}
		return false;
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
	public List<AttributeGroup> attributeGroups() {
		return attributeGroupsStore.groups();
	}

	@Override
	public Collection<AttributeGroup> attributeGroupsOf(Attribute<?> attribute) {
		return this.attributeGroupsStore.getAttributeGroupsOf(attribute);
	}

	@Override
	public boolean containsDependencyBetweenAnyOf(Attribute<?> attribute,
			Collection<? extends Attribute<?>> otherAttributes) {
		return this.attributeGroupsStore.isPartOfMacroAttributeWithAtLeastOneOf(attribute, otherAttributes);
	}

	@Override
	public boolean containsDependencyBetween(Attribute<?> attribute, Attribute<?> otherAttribute) {
		return this.attributeGroupsStore.isPartOfMacroAttributeWith(attribute, otherAttribute);
	}

	@Override
	public String toString() {
		return stringRepr;
	}

	@Override
	public Population population() {
		return population;
	}

}
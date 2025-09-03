/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
 */

package de.unibonn.realkd.data.table.attributegroups;

import java.util.List;
import java.util.Objects;

import de.unibonn.realkd.data.table.attribute.Attribute;

public class DefaultAttributeGroup implements AttributeGroup {

	private List<? extends Attribute<?>> members;

	private String name;

	public DefaultAttributeGroup(String name, List<? extends Attribute<?>> members) {
		this.members = members;
		this.name = name;
	}

	@Override
	public String toString() {
		return members.toString();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public List<? extends Attribute<?>> elements() {
		return members;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof DefaultAttributeGroup)) {
			return false;
		}
		DefaultAttributeGroup otherGroup = (DefaultAttributeGroup) other;
		return members.equals(otherGroup.members) && name.equals(otherGroup.name);
	}
	
	public int hashCode() {
		return Objects.hash(members,name);
	}

}
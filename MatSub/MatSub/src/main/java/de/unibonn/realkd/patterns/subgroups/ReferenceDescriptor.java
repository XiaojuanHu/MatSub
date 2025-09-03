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
package de.unibonn.realkd.patterns.subgroups;

import java.util.Objects;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.common.base.Lazy;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;

public abstract class ReferenceDescriptor {

	private static final String GLOBAL = "global";

	private static final String COMPLEMENT = "complement";

	private final static class Global extends ReferenceDescriptor {

		public Global(IndexSet supportSet) {
			super(supportSet);
		}

		@Override
		public String toString() {
			return GLOBAL;
		}

		@Override
		public String caption() {
			return "global population";
		}

	}

	private final static class Complement extends ReferenceDescriptor {

		public Complement(IndexSet supportSet) {
			super(supportSet);
		}

		@Override
		public String caption() {
			return "complement";
		}

		@Override
		public String toString() {
			return COMPLEMENT;
		}

	}

	private final IndexSet supportSet;

	private final Lazy<Integer> hashCode;

	public abstract String caption();

	@Override
	public abstract String toString();

	private ReferenceDescriptor(IndexSet supportSet) {
		this.supportSet = supportSet;
		this.hashCode = Lazy.lazy(() -> Objects.hash(toString(), supportSet));
	}

	public IndexSet supportSet() {
		return supportSet;
	}

	@Override
	public int hashCode() {
		return hashCode.get();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!getClass().equals(other.getClass())) {
			return false;
		}
		return this.supportSet().equals(((ReferenceDescriptor) other).supportSet());
	}

	static ReferenceDescriptor fromString(String serial, Population population,
			LogicalDescriptor subgroupSelector) {
		if (serial.equals(COMPLEMENT)) {
			return new Complement(IndexSets.complement(subgroupSelector.supportSet()));
		}
		if (!serial.equals(GLOBAL)) {
			// warn
		}
		return new Global(population.objectIds());
	}
	
	public static ReferenceDescriptor complement(LogicalDescriptor subgroupSelector) {
		return new Complement(IndexSets.complement(subgroupSelector.supportSet()));
	}
	
	public static ReferenceDescriptor global(Population population) {
		return new Global(population.objectIds());
	}

}
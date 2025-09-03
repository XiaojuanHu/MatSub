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
package de.unibonn.realkd.common;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.testing.JsonSerializationTesting;

/**
 * Minimal example to reproduce generic serialization problem
 * 
 * @author Mario
 *
 */
public class GenericSerializationTest {

	public static interface Foo<T> {

	}

	public static interface Bah {

	}

	public static enum ConcreteFoos implements Foo<Object> {
		A, B;

	}

	public static enum ConcreteBahs implements Bah {

		ONE, TWO, THREE;

	}

	public static class FooBahContainer {

		private final ConcreteFoos concreteFoo;

		private final ConcreteBahs concreteBah;

		private final Foo<Object> abstractFoo;

		private final Bah abstractBah;

		private final List<ConcreteFoos> concreteFoos;
		//
		// private final List<ConcreteBahs> concreteBahs =
		// ImmutableList.of(ConcreteBahs.THREE);
		//
		// private final List<Foo<Object>> foos =
		// ImmutableList.of(ConcreteFoos.A);
		//
		private final List<Bah> bahs;

		@JsonCreator
		public FooBahContainer(@JsonProperty("concreteFoo") ConcreteFoos concreteFoo,
				@JsonProperty("concreteBah") ConcreteBahs concreteBah,
				@JsonProperty("abstractFoo") Foo<Object> abstractFoo, @JsonProperty("abstractBah") Bah abstractBah,
				@JsonProperty("concreteFoos") List<ConcreteFoos> concreteFoos, @JsonProperty("bahs") List<Bah> bahs) {
			this.concreteFoo = concreteFoo;
			this.concreteBah = concreteBah;
			this.abstractFoo = abstractFoo;
			this.abstractBah = abstractBah;
			this.concreteFoos = concreteFoos;
			this.bahs = bahs;
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof FooBahContainer)) {
				return false;
			}
			FooBahContainer fooContainer = (FooBahContainer) other;
			return (this.concreteFoo.equals(fooContainer.concreteFoo)
					&& this.concreteBah.equals(fooContainer.concreteBah)
					&& this.abstractBah.equals(fooContainer.abstractBah)
					&& this.abstractFoo.equals(fooContainer.abstractFoo)
					&& this.concreteFoos.equals(fooContainer.concreteFoos)
					// && this.foos.equals(fooContainer.foos)
					&& this.bahs.equals(fooContainer.bahs));
		}

		@JsonProperty("concreteFoo")
		public ConcreteFoos concreteFoo() {
			return concreteFoo;
		}

		@JsonProperty("abstractFoo")
		public Foo<Object> abstractFoo() {
			return abstractFoo;
		}

		@JsonProperty("abstractBah")
		public Bah abstratBah() {
			return abstractBah;
		}

		@JsonProperty("concreteBah")
		public ConcreteBahs concreteBah() {
			return concreteBah;
		}

		@JsonProperty("concreteFoos")
		public List<ConcreteFoos> concreteFoos() {
			return concreteFoos;
		}

		//
		// @JsonProperty("concreteBahs")
		// public List<ConcreteBahs> concreteBahs() {
		// return concreteBahs;
		// }
		//
		// @JsonProperty("foos")
		// public List<Foo<Object>> foos() {
		// return foos;
		// }
		//
		@JsonProperty("bahs")
		public List<Bah> bahs() {
			return bahs;
		}

	}

	@Test
	public void test() throws IOException {
		FooBahContainer container = new FooBahContainer(ConcreteFoos.A, ConcreteBahs.TWO, ConcreteFoos.A,
				ConcreteBahs.ONE, ImmutableList.of(ConcreteFoos.A),
				ImmutableList.of(ConcreteBahs.ONE, ConcreteBahs.THREE));
		JsonSerializationTesting.testJsonSerialization(container, FooBahContainer.class);
	}

}

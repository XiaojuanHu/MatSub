/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.graphs;

import static java.util.Objects.hash;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.Proposition;

/**
 *
 * @author Ali Doku
 * @author Sandy Moens
 * 
 * @since 0.7.1
 * 
 * @version 0.7.1
 */
public class Nodes {

	public static Node create(int id, Proposition proposition) {
		return new DefaultNode(id, proposition);
	}

	private static class DefaultNode implements Node {

		@KdonTypeName("defaultNode")
		private static class DefaultNodeSerialForm implements SerialForm<Node> {

			@JsonProperty("id")
			private int id;

			@JsonProperty("proposition")
			private SerialForm<? extends Proposition> proposition;

			private DefaultNodeSerialForm(@JsonProperty("id") int id,
					@JsonProperty("proposition") SerialForm<? extends Proposition> proposition) {
				this.id = id;
				this.proposition = proposition;
			}

			@Override
			public Node build(Workspace workspace) {
				return new DefaultNode(this.id, this.proposition.build(workspace));
			}

		}

		private final int id;

		private final Proposition proposition;

		private DefaultNode(int id, Proposition proposition) {
			this.id = id;
			this.proposition = proposition;
		}

		@Override
		public int id() {
			return this.id;
		}

		@Override
		public Proposition proposition() {
			return this.proposition;
		}

		@Override
		public String name() {
			return this.proposition.name();
		}

		@Override
		public SerialForm<Node> serialForm() {
			return new DefaultNodeSerialForm(this.id, this.proposition.serialForm());
		}

		@Override
		public int hashCode() {
			return hash(this.id, this.proposition);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Node))
				return false;

			Node other = (Node) o;

			return this.id() == other.id() && this.proposition.equals(other.proposition());
		}
	}

	// Suppress default constructor for non-instantiability
	private Nodes() {
		throw new AssertionError();
	}

}

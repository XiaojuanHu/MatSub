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
package de.unibonn.realkd.patterns;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.Entity;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.IdentifiableSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;

/**
 * Serializable collection of patterns.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public final class NamedPatternCollection implements Entity, HasSerialForm<NamedPatternCollection>, Iterable<String> {

	@KdonTypeName("patternCollection")
	public static class NamedPatternCollectionSerialForm implements IdentifiableSerialForm<NamedPatternCollection> {

		@JsonProperty("id")
		private final Identifier id;

		@JsonProperty("name")
		private final String name;

		@JsonProperty("description")
		private final String description;

		@JsonProperty("patterns")
		private final SerialForm<? extends Pattern<?>>[] patterns;

		@JsonCreator
		private NamedPatternCollectionSerialForm(@JsonProperty("id") Identifier id, @JsonProperty("name") String name,
				@JsonProperty("description") String description,
				@JsonProperty("patterns") SerialForm<? extends Pattern<?>>[] patterns) {
			this.id = id;
			this.name = name;
			this.description = description;
			this.patterns = patterns;
		}

		@Override
		public Identifier identifier() {
			return id;
		}

		@Override
		public NamedPatternCollection build(Workspace workspace) {
			return new NamedPatternCollection(id, name, description,
					Arrays.stream(patterns).map(b -> b.build(workspace)).collect(Collectors.toList()));
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			Set<Identifier> deps = Arrays.stream(patterns).flatMap(b -> b.dependencyIds().stream())
					.collect(Collectors.toSet());
			return deps;
		}
		
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof NamedPatternCollectionSerialForm)) {
				return false;
			}
			NamedPatternCollectionSerialForm otherNPC = (NamedPatternCollectionSerialForm) other;
			return (this.id.equals(otherNPC.id)
					&& this.name.equals(otherNPC.name) && this.description.equals(otherNPC.description) && Arrays.equals(this.patterns, otherNPC.patterns));
		}

	}

	private final Identifier id;

	private final String name;

	private final String description;

	private final Collection<? extends Pattern<?>> patterns;

	public NamedPatternCollection(Identifier id, String name, String description, Collection<? extends Pattern<?>> patterns) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.patterns = patterns;
	}

	public Collection<? extends Pattern<?>> patterns() {
		return patterns;
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

	@SuppressWarnings("unchecked")
	@Override
	public IdentifiableSerialForm<NamedPatternCollection> serialForm() {
		return new NamedPatternCollectionSerialForm(id, name, description,
				(SerialForm<? extends Pattern<?>>[]) patterns.stream().map(p -> p.serialForm()).toArray(i->new SerialForm<?>[i]));
	}

	@Override
	public String toString() {
		return patterns.toString();
	}

	@Override
	public Iterator<String> iterator() {
		return patterns.stream().map(p -> p.toString()).iterator();
	}

}

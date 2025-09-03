/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 * <p>
 * Pattern descriptor that explicitly refers to the values in some datatable
 * restricted to a specific set of attributes and rows.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.1.2.1
 *
 */
public interface ExplicitLocalPatternDescriptor extends
		TableSubspaceDescriptor, LocalPatternDescriptor {

	public static ExplicitLocalPatternDescriptor createExplicitLocalPatternDescriptor(
			DataTable table, Collection<Attribute<?>> attributes,
			IndexSet supportSet) {
		return new ExplicitLocalPatternDescriptorImplementation(table,
				attributes, supportSet);
	}

	public static class ExplicitLocalPatternDescriptorImplementation implements
			ExplicitLocalPatternDescriptor {

		private final List<Attribute<?>> attributes;

		private final IndexSet supportSet;

		private final DataTable datatable;

		private ExplicitLocalPatternDescriptorImplementation(
				DataTable dataArtifact, Collection<Attribute<?>> attributes,
				IndexSet supportSet) {
			this.attributes = new ArrayList<>(attributes);
			this.supportSet = supportSet;
			this.datatable = dataArtifact;
		}

		@Override
		public IndexSet supportSet() {
			return supportSet;
		}
		
		@Override
		public DataTable table() {
			return this.datatable;
		}

		@Override
		public List<Attribute<?>> getReferencedAttributes() {
			return attributes;
		}

		@Override
		public Population population() {
			return datatable.population();
		}

		private static class ExplicitLocalPatternDescriptorBuilderImplementation
				implements SerialForm<ExplicitLocalPatternDescriptor> {

			private final List<Integer> attributeIndices;

			private final IndexSet rowIndices;

			public ExplicitLocalPatternDescriptorBuilderImplementation(
					List<Integer> attributesIndices,
					IndexSet rowIndices) {
				this.attributeIndices = attributesIndices;
				this.rowIndices = rowIndices;
			}

			@Override
			public ExplicitLocalPatternDescriptor build(Workspace workspace) {
				DataTable dataTable = workspace.datatables().get(0);
				return ExplicitLocalPatternDescriptor
						.createExplicitLocalPatternDescriptor(
								dataTable,
								attributeIndices.stream()
										.map(i -> dataTable.attribute(i))
										.collect(Collectors.toList()),
								rowIndices);
			}

		}

		public SerialForm<ExplicitLocalPatternDescriptor> serialForm() {
			List<Integer> attributeIndices = attributes.stream()
					.map(a -> datatable.attributes().indexOf(a))
					.collect(Collectors.toList());
			return new ExplicitLocalPatternDescriptorBuilderImplementation(
					attributeIndices, supportSet());
		}

	}
}

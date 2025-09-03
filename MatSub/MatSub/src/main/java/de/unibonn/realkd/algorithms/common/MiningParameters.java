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
package de.unibonn.realkd.algorithms.common;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.base.Identifier.identifier;
import static de.unibonn.realkd.common.parameter.Parameters.rangeEnumerableParameter;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.DefaultRangeEnumerableParameter;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;
import de.unibonn.realkd.common.parameter.SubCollectionParameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 * Provides factory methods for standard mining parameters.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.5.0
 *
 */
public class MiningParameters {

	/**
	 * <p>
	 * Creates parameter for selecting a sub collection of attribute set of some
	 * currently selected data table. The value range of the parameter is sub
	 * collections of all attributes available filtered by an additional predicate.
	 * This can for instance be used to only allow metric attributes or similar.
	 * </p>
	 * 
	 * <p>
	 * WARNING: this is currently (0.1.1) a sub list parameter. Intended to change
	 * to ordered set in the future, because most often the order does not matter
	 * and it simplifies UI elements a lot.
	 * </p>
	 * 
	 * @param name
	 *            name of the parameter
	 * @param description
	 *            description of the parameter
	 * @param dataTableParameter
	 *            parameter that holds the data table selection with its attribute
	 *            collection
	 * @param filterPredicate
	 *            that is used to reduce the set of available attributes
	 * @return SubCollectionParameter for specifying a sub collection of attributes
	 * 
	 */
	public static SubCollectionParameter<Attribute<?>, List<Attribute<?>>> getAttributeSelectionParameter(Identifier id,
			String name, String description, Parameter<DataTable> dataTableParameter,
			Predicate<Attribute<?>> filterPredicate) {
		return Parameters
				.subListParameter(
						id, name, description, () -> dataTableParameter.current().attributes().stream()
								.filter(filterPredicate).collect(Collectors.toCollection(ArrayList::new)),
						dataTableParameter);
	}

	public static SubCollectionParameter<Attribute<? extends Object>, List<Attribute<? extends Object>>> getAttributeSelectionParameter(
			Identifier id, String name, String description, Parameter<DataTable> dataTableParameter,
			Predicate<Attribute<?>> filterPredicate, Predicate<List<Attribute<? extends Object>>> validator) {
		return Parameters
				.subListParameter(
						id, name, description, () -> dataTableParameter.current().attributes().stream()
								.filter(filterPredicate).collect(Collectors.toCollection(ArrayList::new)),
						validator, dataTableParameter);
	}

	/**
	 * Convenience method for creating an attribute selection attribute with no
	 * additional filter predicate.
	 * 
	 * @see #getAttributeSelectionParameter(String, String, Parameter, Predicate)
	 * 
	 */
	public static SubCollectionParameter<Attribute<?>, List<Attribute<?>>> getAttributeSelectionParameter(Identifier id, String name,
			String description, Parameter<DataTable> dataTableParameter) {
		return getAttributeSelectionParameter(id, name, description, dataTableParameter, attribute -> true);
	}

	private static class DataTableParameter extends DefaultRangeEnumerableParameter<DataTable> {

		private static final String DESCRIPTION = "The input data for the algorithm.";

		private static final String NAME = "Datatable";

		private final Workspace workspace;

		DataTableParameter(final Workspace workspace) {
			super(identifier("table"), NAME, DESCRIPTION, DataTable.class, () -> workspace.datatables());
			this.workspace = workspace;
		}

		public boolean hidden() {
			return workspace.datatables().size() == 1;
		}

	}

	/**
	 * Parameter that allows selection of a datatable in a given fixed workspace. Is
	 * hidden while only one datatable available in workspace.
	 * 
	 * @param workspace
	 *            the workspace in which to look for datatables
	 * @return range enumerable parameter with valid values all datatables in
	 *         workspace
	 */
	public static RangeEnumerableParameter<DataTable> dataTableParameter(Workspace workspace) {
		if (workspace == null) {
			throw new IllegalArgumentException("Data workspace must not be null");
		}
		return new DataTableParameter(workspace);
	}

	/**
	 * <p>
	 * Provides propositional logic parameter that accepts propositional logics as
	 * values that are linked to the same population as the datatable given by the
	 * current value of a specified datatable parameter.
	 * </p>
	 * 
	 * @see FreePropositionalLogicParameter
	 * 
	 * @author Mario Boley
	 * 
	 * @since 0.1.0
	 * 
	 * @version 0.6.0
	 * 
	 */
	public static RangeEnumerableParameter<PropositionalContext> matchingPropositionalLogicParameter(
			Workspace workspace, Parameter<DataTable> datatableParameter) {
		return rangeEnumerableParameter(id("props"),"Propositions",
				"The collections of basic statements available to the algorithm to construct patterns.",
				PropositionalContext.class,
				() -> workspace.propositionalContexts().stream()
						.filter(p -> p.population() == datatableParameter.current().population()).collect(toList()),
				datatableParameter);
	}

}

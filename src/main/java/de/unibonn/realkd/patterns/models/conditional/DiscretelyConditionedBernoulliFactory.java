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
package de.unibonn.realkd.patterns.models.conditional;

import static de.unibonn.realkd.patterns.models.table.ContingencyTables.contingencyTable;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.Attributes;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.patterns.models.ModelFactory;
import de.unibonn.realkd.patterns.models.table.Cell;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;
import de.unibonn.realkd.patterns.models.table.Dimension;

/**
 * @author Mario Boley
 * 
 * @author Kailash Budhathoki
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
@KdonTypeName("asDiscretlyConditionedBernoulli")
public class DiscretelyConditionedBernoulliFactory implements ModelFactory<DiscretelyConditionedBernoulli> {

	@JsonProperty("positiveCategory")
	private final Object positiveCategory;

	public DiscretelyConditionedBernoulliFactory(@JsonProperty("positiveCategory") Object positiveCategory) {
		this.positiveCategory = positiveCategory;
	}

	@Override
	public Class<? extends DiscretelyConditionedBernoulli> modelClass() {
		return DiscretelyConditionedBernoulli.class;
	}

	@Override
	public DiscretelyConditionedBernoulli getModel(DataTable dataTable, List<? extends Attribute<?>> attributes,
			IndexSet rows) {
		ContingencyTable controlTable, controlAndTargetTable;
		Dimension targetDim;
		Map<Cell, EmpiricalBernoulliDistribution> conditionalTables = new HashMap<Cell, EmpiricalBernoulliDistribution>();

		if (attributes.size() == 1) {
			List<Attribute<?>> attributesWithDummyControl = new ArrayList<>(attributes);
			CategoricAttribute<String> dummyControl = Attributes.categoricalAttribute(Identifier.identifier("dummy"),
					"dummy", "dummy", Collections.nCopies(attributes.get(0).maxIndex() + 1, "1"));
			attributesWithDummyControl.add(0, dummyControl);

			controlAndTargetTable = contingencyTable(dataTable, attributesWithDummyControl, rows);
			int[] controlDims = range(0, attributesWithDummyControl.size() - 1).toArray();
			controlTable = controlAndTargetTable.marginal(controlDims);
			targetDim = controlAndTargetTable.dimension(attributesWithDummyControl.size() - 1);
		} else {
			controlAndTargetTable = contingencyTable(dataTable, attributes, rows);
			int[] controlDims = range(0, attributes.size() - 1).toArray();
			controlTable = controlAndTargetTable.marginal(controlDims);
			targetDim = controlAndTargetTable.dimension(attributes.size() - 1);
		}

		int posTargetKey = targetDim.bin(positiveCategory);
		for (Cell ccell : controlTable.nonZeroCells()) {
			List<Integer> controlAndPositiveTargetKey = new ArrayList<Integer>(ccell.key());
			controlAndPositiveTargetKey.add(posTargetKey);

			int controlAndPosTargetCount = controlAndTargetTable.count(new Cell(controlAndPositiveTargetKey));
			int controlCount = controlTable.count(ccell);
			conditionalTables.put(ccell, new EmpiricalBernoulliDistribution(controlCount, controlAndPosTargetCount));
		}
		return new DiscretelyConditionedBernoulli(conditionalTables);
	}

	@Override
	public boolean isApplicable(List<? extends Attribute<?>> attributes) {
		return (attributes.size() >= 1) && (attributes.get(attributes.size() - 1) instanceof CategoricAttribute);
	}

	@Override
	public String symbol() {
		return "condBernoulli";
	}

}

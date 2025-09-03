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
package de.unibonn.realkd.patterns.pmm;

import static de.unibonn.realkd.patterns.pmm.PureModelMining.pureSubgroup;

import java.util.Arrays;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.testing.TestConstants;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptors;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistributionFactory;
import de.unibonn.realkd.patterns.subgroups.StandardDeviationReduction;
import de.unibonn.realkd.patterns.subgroups.Subgroups;

/**
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.2.2
 *
 */
public class PMMTestInputs {

	public static Iterable<Object[]> getPmmPatternTestInput() {
		Workspace workspace = TestConstants.getGermanyWorkspace();
		DataTable dataTable = workspace.datatables().get(0);
		PropositionalContext propLogic = workspace.propositionalContexts().get(0);
		LogicalDescriptor extensionDescriptor = LogicalDescriptors.create(propLogic.population(),
				ImmutableList.of(propLogic.propositions().get(4), propLogic.propositions().get(28)));
		Attribute<?> populationDensity = dataTable.attribute(15);
		Attribute<?> elderlyPopulation = dataTable.attribute(16);
		return Arrays.asList(new Object[][] { { workspace,
				pureSubgroup(
						Subgroups.subgroup(extensionDescriptor, dataTable,
								ImmutableList.of(populationDensity, elderlyPopulation),
								MetricEmpiricalDistributionFactory.INSTANCE),
						StandardDeviationReduction.STD_REDUCTION, ImmutableList.of()) } });
	}

}

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
package de.unibonn.realkd.patterns.emm;

import static de.unibonn.realkd.patterns.emm.ExceptionalModelMining.emmPattern;
import static de.unibonn.realkd.patterns.subgroups.Subgroups.subgroup;

import java.util.Arrays;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.JsonSerialization;
import de.unibonn.realkd.common.testing.TestConstants;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptors;
import de.unibonn.realkd.patterns.models.gaussian.GaussianModelFactory;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistributionFactory;
import de.unibonn.realkd.patterns.models.regression.TheilSenLinearRegressionModelFactory;
import de.unibonn.realkd.patterns.models.table.ContingencyTableModelFactory;

/**
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.2.2
 *
 */
public class EMMTestInputs {

	public static Iterable<Object[]> getEMMDescriptorTestInputs() {
		Workspace workspace = TestConstants.getGermanyWorkspace();
		DataTable dataTable = workspace.datatables().get(0);
		PropositionalContext propLogic = workspace.propositionalContexts().get(0);
		LogicalDescriptor extensionDescriptor = LogicalDescriptors.create(propLogic.population(),
				ImmutableList.of(propLogic.propositions().get(4), propLogic.propositions().get(28)));
		Attribute<?> populationDensity = dataTable.attribute(15);
		Attribute<?> elderlyPopulation = dataTable.attribute(16);
		return Arrays.asList(new Object[][] {
				{ workspace,
						subgroup(extensionDescriptor, dataTable, ImmutableList.of(populationDensity),
								GaussianModelFactory.INSTANCE) },
				{ workspace,
						subgroup(extensionDescriptor, dataTable, ImmutableList.of(populationDensity),
								ContingencyTableModelFactory.INSTANCE) },
				{ workspace,
						subgroup(extensionDescriptor, dataTable, ImmutableList.of(populationDensity),
								MetricEmpiricalDistributionFactory.INSTANCE) },
				{ workspace,
						subgroup(extensionDescriptor, dataTable, ImmutableList.of(populationDensity, elderlyPopulation),
								MetricEmpiricalDistributionFactory.INSTANCE) },
				{ workspace,
						subgroup(extensionDescriptor, dataTable, ImmutableList.of(populationDensity, elderlyPopulation),
								TheilSenLinearRegressionModelFactory.INSTANCE) } });
	}

	public static Iterable<Object[]> getEMMPatternTestInput() {
		Workspace workspace = TestConstants.getGermanyWorkspace();
		DataTable dataTable = workspace.datatables().get(0);
		PropositionalContext propLogic = workspace.propositionalContexts().get(0);
		LogicalDescriptor extensionDescriptor = LogicalDescriptors.create(propLogic.population(),
				ImmutableList.of(propLogic.propositions().get(4), propLogic.propositions().get(28)));
		Attribute<?> populationDensity = dataTable.attribute(15);
		Attribute<?> elderlyPopulation = dataTable.attribute(16);
		return Arrays.asList(new Object[][] {
				{ workspace,
						emmPattern(
								subgroup(extensionDescriptor, dataTable, ImmutableList.of(populationDensity),
										GaussianModelFactory.INSTANCE),
								TotalVariationDistance.TOTAL_VARIATION_DISTANCE, ImmutableList.of()) },
				{ workspace,
						emmPattern(
								subgroup(extensionDescriptor, dataTable, ImmutableList.of(populationDensity),
										ContingencyTableModelFactory.INSTANCE),
								TotalVariationDistance.TOTAL_VARIATION_DISTANCE, ImmutableList.of()) },
				{ workspace,
						emmPattern(
								subgroup(extensionDescriptor, dataTable, ImmutableList.of(populationDensity),
										MetricEmpiricalDistributionFactory.INSTANCE),
								CumulativeJensenShannonDivergence.CJS, ImmutableList.of()) },
				{ workspace, emmPattern(
						subgroup(extensionDescriptor, dataTable, ImmutableList.of(populationDensity),
								MetricEmpiricalDistributionFactory.INSTANCE),
						KolmogorovSmirnovStatistic.KOLMOGOROV_SMIRNOV_STATISTIC, ImmutableList.of()) },
				{ workspace,
						emmPattern(
								subgroup(extensionDescriptor, dataTable, ImmutableList.of(elderlyPopulation),
										MetricEmpiricalDistributionFactory.INSTANCE),
								ManhattenMeanDistance.MANHATTAN_MEAN_DISTANCE,
								ImmutableList.of(NormalizedPositiveMeanShift.NORMALIZED_POSITIVE_MEAN_SHIFT,
										NormalizedNegativeMeanShift.NORMALIZED_NEGATIVE_MEAN_SHIFT)) },
				{ workspace, emmPattern(
						subgroup(extensionDescriptor, dataTable, ImmutableList.of(populationDensity, elderlyPopulation),
								TheilSenLinearRegressionModelFactory.INSTANCE),
						AngularDistanceOfSlopes.ANGULAR_DISTANCE_OF_SLOPES, ImmutableList.of()) }

		});
	}

	public static void main(String[] args) {
		for (Object[] values : getEMMPatternTestInput()) {
			ExceptionalModelPattern object = (ExceptionalModelPattern) values[1];
			System.out.println(JsonSerialization.toPrettyJson(object.serialForm()));
		}
	}
}

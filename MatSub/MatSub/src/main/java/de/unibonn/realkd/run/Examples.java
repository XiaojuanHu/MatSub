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
package de.unibonn.realkd.run;

import static com.google.common.collect.Sets.newHashSet;
import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.patterns.functional.FunctionalPatterns.functionalPatternComputationSpec;
import static de.unibonn.realkd.patterns.functional.FunctionalPatterns.FunctionalDependencyDiscoverySpec.functionalDependencyDiscovery;
import static de.unibonn.realkd.patterns.logical.LogicalDescriptors.attributeBasedLogicalDescriptorSerialForm;
import static de.unibonn.realkd.patterns.subgroups.Subgroups.subgroupSerialForm;
import static de.unibonn.realkd.run.ExecutionContext.oneShotExecutionContext;
import static de.unibonn.realkd.run.LegacyComputationSpecification.legacyComputation;
import static de.unibonn.realkd.run.WorkspaceFromXarf.workspaceFromXarf;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.toList;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import de.unibonn.realkd.common.JsonSerialization;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.constraints.Constraint;
import de.unibonn.realkd.data.constraints.Constraints;
import de.unibonn.realkd.patterns.emm.ExceptionalModelMining.ExceptionalSubgroupComputationSpec;
import de.unibonn.realkd.patterns.emm.ReliableConditionalEffect;
import de.unibonn.realkd.patterns.functional.FunctionalPatterns.BinaryAttributeSetRelationSerialForm;
import de.unibonn.realkd.patterns.models.conditional.DiscretelyConditionedBernoulli;
import de.unibonn.realkd.patterns.models.conditional.DiscretelyConditionedBernoulliFactory;
import de.unibonn.realkd.patterns.subgroups.Subgroups.SubgroupSerialForm;

/**
 * @author Janis Kalafolias
 * 
 * @author Sandy Moens
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class Examples {

	private static final FileSystem FILE_SYSTEM = FileSystems.getDefault();
	private static final Logger LOGGER = Logger.getLogger(Examples.class.getName());

	private static class Example {

		private final Identifier identifier;

		private final List<String> dataFiles;

		private final ProductWorkScheme productWorkScheme;

		private final boolean testOnGeneration;

		private Example(Identifier identifier, List<String> dataFiles, ProductWorkScheme productWorkScheme,
				boolean testOnGeneration) {
			this.identifier = identifier;
			this.dataFiles = dataFiles;
			this.productWorkScheme = productWorkScheme;
			this.testOnGeneration = testOnGeneration;
		}

	}

	private static final Path RESOURCES_PATH = FILE_SYSTEM.getPath("src", "main", "resources");
	private static final Path DATA_PATH = FILE_SYSTEM.getPath(RESOURCES_PATH.toString(), "data");

	private static final Path EXTERNAL_RESOURCES_PATH = FILE_SYSTEM.getPath("target", "external-resources");
	private static final Path TEST_OUTPUT_PATH = FILE_SYSTEM.getPath("target", "example-test-ouput");

	private static final Path JOBS_PATH = EXTERNAL_RESOURCES_PATH.resolve("jobs");
	private static final Path DEST_DATA_PATH = EXTERNAL_RESOURCES_PATH.resolve("data");

	private static Path generateJobsPath(Identifier identifier) {
		return JOBS_PATH.resolve(identifier.toString() + ".json");
	}

	private static Path generateSourceDataPath(String dataName) {
		return FILE_SYSTEM.getPath(DATA_PATH.toString(), dataName);
	}

	private static Path generateDestinationDataPath(String dataName) {
		String[] split = dataName.split("/");
		return FILE_SYSTEM.getPath(DEST_DATA_PATH.toString(), split[split.length - 1]);
	}

	private static final List<Example> EXAMPLES = ImmutableList.of(octetBinariesFunctionalDependencyExample(),
			titanicSubgroupDiscoveryExample());

	private static Example octetBinariesFunctionalDependencyExample() {
		WorkspaceSpecification[] workspaceSpecifications = new WorkspaceSpecification[] {
				workspaceFromXarf(id("binaries"), "octet_binaries_2.1.1.xarf") };

		ComputationSpecification[] computationSpecifications = new ComputationSpecification[] {
				functionalDependencyDiscovery(id("titanic_functional_pattern_discovery"), Identifier.id("sign_delta_e"),
						3, 1) };

		ProductWorkScheme productWorkScheme = ProductWorkScheme.simpleExperiment(id("octet_binaries_fdd"),
				workspaceSpecifications, computationSpecifications, new Tracker[0], 3600);

		return new Example(id("octet_binaries_fdd"), ImmutableList.of("binaries/octet_binaries_2.1.1.xarf"),
				productWorkScheme, false);
	}

	private static Example titanicSubgroupDiscoveryExample() {
		WorkspaceSpecification[] workspaceSpecifications = new WorkspaceSpecification[] {
				workspaceFromXarf(id("titanic"), "titanic_1.0.0.xarf") };

		HashMap<String, String> subgroupAnalysisParameters = Maps.newHashMap();
		subgroupAnalysisParameters.put("targets", "[survived]");
		subgroupAnalysisParameters.put("modeling", "bernoulli");
		subgroupAnalysisParameters.put("pos_category", "1");
		subgroupAnalysisParameters.put("dev_measure", "pos_prob_shift");
		subgroupAnalysisParameters.put("attr_filter", "[]");
		subgroupAnalysisParameters.put("cov_weight", "0.5");
		subgroupAnalysisParameters.put("max_depth", "Optional[6]");
		subgroupAnalysisParameters.put("num_res", "3");

		HashMap<String, String> titanicFunctionalPatternDiscoveryParameters = Maps.newHashMap();
		titanicFunctionalPatternDiscoveryParameters.put("targetAttributeParameter", "[survived]");
		titanicFunctionalPatternDiscoveryParameters.put("apx_fac", "1");
		titanicFunctionalPatternDiscoveryParameters.put("num_res", "3");
		titanicFunctionalPatternDiscoveryParameters.put("oest", "CHAIN");
		titanicFunctionalPatternDiscoveryParameters.put("search_order", "BREADTHFSPOTENTIAL");

		SubgroupSerialForm<DiscretelyConditionedBernoulli> subgroupSerialForm = subgroupSerialForm(id("titanic"),
				attributeBasedLogicalDescriptorSerialForm(id("titanic"), new Identifier[] { id("sex") },
						new Constraint[] { Constraints.equalTo("female") }),
				null, new Identifier[] { id("class"), id("survived") }, new DiscretelyConditionedBernoulliFactory("1"),
				null, null);

		ComputationSpecification[] computationSpecifications = new ComputationSpecification[] {
				functionalPatternComputationSpec(id("survival_gender_dependency"),
						new BinaryAttributeSetRelationSerialForm(id("titanic"), new Identifier[] { id("sex") },
								new Identifier[] { id("survived") })),
				legacyComputation(id("subgroup_analysis"), id("EXCEPTIONAL_SUBGROUP_BESTFIRST_BRANCHANDBOUND"),
						subgroupAnalysisParameters),
				legacyComputation(id("titanic_functional_pattern_discovery"), id("FUNCTIONAL_PATTERN_DISCOVERY_OPUS"),
						titanicFunctionalPatternDiscoveryParameters),
				new ExceptionalSubgroupComputationSpec(id("class_conditioned_gender_effect"), subgroupSerialForm,
						ReliableConditionalEffect.RELIABLE_CONDITIONAL_EFFECT) };

		ProductWorkScheme productWorkScheme = ProductWorkScheme.simpleExperiment(id("titanic_survival_factors"),
				workspaceSpecifications, computationSpecifications, new Tracker[0], 3600);

		return new Example(id("titanic_survival_factors"), ImmutableList.of("titanic/titanic_1.0.0.xarf"),
				productWorkScheme, true);
	}

	/**
	 * Generates all examples given as arguments or all examples if no arguments
	 * given.
	 * 
	 * Runs all examples to be generated that require test in target environment.
	 * 
	 * @param args
	 *            identifiers of examples to be generated and tested
	 * 
	 * @throws IOException
	 *             if writing to output path fails
	 * 
	 */
	public static void main(String[] args) throws IOException {
		Files.createDirectories(JOBS_PATH);
		Files.createDirectories(DEST_DATA_PATH);

		Set<Identifier> examplesFilter = Arrays.stream(args).map(a -> id(a)).collect(Collectors.toSet());
		List<Example> relevantExamples = examplesFilter.isEmpty() ? EXAMPLES
				: EXAMPLES.stream().filter(e -> examplesFilter.contains(e.identifier)).collect(toList());

		LOGGER.info("copying datasets referenced in example jobs");
		Set<String> datasetsToCopy = newHashSet();
		for (Example example : relevantExamples) {
			datasetsToCopy.addAll(example.dataFiles);
		}
		datasetsToCopy.stream().forEach(s -> {
			try {
				Files.copy(generateSourceDataPath(s), generateDestinationDataPath(s), REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		LOGGER.info("writing example job files");
		for (Example example : relevantExamples) {
			Path filePath = generateJobsPath(example.identifier);

			try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
				if (example.testOnGeneration) {
					ExecutionContext context = oneShotExecutionContext(
							TEST_OUTPUT_PATH.resolve(example.productWorkScheme.identifier().toString()), DEST_DATA_PATH);
					example.productWorkScheme.run(context);
				}
				JsonSerialization.serialize(writer, example.productWorkScheme);
			} catch (IOException e) {
				LOGGER.severe(e.getMessage());
			}
		}

	}

}

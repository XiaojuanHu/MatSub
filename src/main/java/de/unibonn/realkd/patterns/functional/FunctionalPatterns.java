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
package de.unibonn.realkd.patterns.functional;

import static de.unibonn.realkd.patterns.functional.ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.algorithms.branchbound.OPUS.TraverseOrder;
import de.unibonn.realkd.algorithms.functional.OPUSFunctionalPatternSearch;
import de.unibonn.realkd.algorithms.functional.OPUSFunctionalPatternSearch.LanguageOption;
import de.unibonn.realkd.algorithms.functional.OPUSFunctionalPatternSearch.OptimisticEstimatorOption;
import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.computations.core.CallableWithStopInterface;
import de.unibonn.realkd.computations.core.Computation;
import de.unibonn.realkd.computations.core.Computations;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.DefaultPattern;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;
import de.unibonn.realkd.patterns.models.table.ContingencyTables;
import de.unibonn.realkd.run.ComputationSpecification;

/**
 * @author Mario Boley
 * @author Panagiotis Mandros
 * 
 * @since 0.3.1
 * 
 * @version 0.5.0
 *
 */
public class FunctionalPatterns {

	public static BinaryAttributeSetRelation binaryAttributeSetRelation(DataTable table, Set<Attribute<?>> domain,
			Set<Attribute<?>> coDomain) {
		List<Attribute<?>> attributes = new ArrayList<>();
		attributes.addAll(domain);
		attributes.addAll(coDomain);
		ContingencyTable contingencyTable = ContingencyTables.contingencyTable(table, attributes);
		return new BinaryAttributeSetRelationImplementation(table, domain, coDomain, contingencyTable);
	}

	public static FunctionalPattern functionalPattern(BinaryAttributeSetRelation descriptor) {
		return functionalPattern(descriptor, RELIABLE_FRACTION_OF_INFORMATION);
	}

	public static FunctionalPattern functionalPattern(BinaryAttributeSetRelation descriptor,
			FunctionalDependencyMeasure measure, Measurement... additionalMeasurements) {
		List<Measurement> measurements = new ArrayList<>();
		measurements.add(measure.perform(descriptor));
		stream(additionalMeasurements).forEach(m -> measurements.add(m));
		return new FunctionalPatternImplementation(descriptor.table().population(), descriptor, measurements);
	}

	public static FunctionalPattern functionalPattern(BinaryAttributeSetRelation descriptor,
			Measurement dependencyMeasurement, Measurement... additionalMeasurements) {
		List<Measurement> measurements = new ArrayList<>();
		measurements.add(dependencyMeasurement);
		stream(additionalMeasurements).forEach(m -> measurements.add(m));
		return new FunctionalPatternImplementation(descriptor.table().population(), descriptor, measurements);
	}

	private static class BinaryAttributeSetRelationImplementation implements BinaryAttributeSetRelation {

		private final DataTable table;

		private final Set<Attribute<?>> domain;

		private final Set<Attribute<?>> coDomain;

		private final List<Attribute<?>> allReferenced;

		private final ContingencyTable contingencyTable;

		private BinaryAttributeSetRelationImplementation(DataTable table, Set<Attribute<?>> domain,
				Set<Attribute<?>> coDomain, ContingencyTable contingencyTable) {
			this.domain = domain;
			this.coDomain = coDomain;
			this.allReferenced = ImmutableList.copyOf(Sets.union(domain, coDomain));
			this.table = table;
			this.contingencyTable = contingencyTable;
		}
		
		@Override
		public List<Attribute<?>> getReferencedAttributes() {
			return allReferenced;
		}

		@Override
		public Set<Attribute<?>> domain() {
			return domain;
		}

		@Override
		public Set<Attribute<?>> coDomain() {
			return coDomain;
		}

		@Override
		public SerialForm<BinaryAttributeSetRelation> serialForm() {
			return new BinaryAttributeSetRelationSerialForm(table.identifier(),
					domain.stream().map(a -> a.identifier()).toArray(n -> new Identifier[n]),
					coDomain.stream().map(a -> a.identifier()).toArray(n -> new Identifier[n]));
		}

		@Override
		public DataTable table() {
			return table;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof BinaryAttributeSetRelation)) {
				return false;
			}
			BinaryAttributeSetRelation that = (BinaryAttributeSetRelation) o;
			return this.domain().equals(that.domain()) && this.coDomain().equals(that.coDomain());
		}

		@Override
		public int hashCode() {
			return Objects.hash(domain, coDomain);
		}

		@Override
		public String toString() {
			return "(" + domain.toString() + "," + coDomain.toString() + ")";
		}

		@Override
		public ContingencyTable contingencyTable() {
			return contingencyTable;
		}
	}

	@KdonTypeName("binaryAttributeSetRelation")
	@KdonDoc("A binary relation R between two attribute sets X and Y.")
	public static class BinaryAttributeSetRelationSerialForm implements SerialForm<BinaryAttributeSetRelation> {

		@JsonProperty("table")
		@KdonDoc("The table containing all referenced attributes.")
		private final Identifier tableId;

		@JsonProperty("domain")
		@KdonDoc("The ids of the attributes in the left-hand side of the relation X.")
		private final Identifier[] domain;

		@JsonProperty("codomain")
		@KdonDoc("The ids of the attributes in the right-hand side of the relation Y.")
		private final Identifier[] coDomain;

		private final ImmutableList<Identifier> dependencies;

		@JsonCreator
		public BinaryAttributeSetRelationSerialForm(@JsonProperty("table") Identifier tableId,
				@JsonProperty("domain") Identifier[] domain, @JsonProperty("codomain") Identifier[] coDomain) {
			this.tableId = tableId;
			this.domain = domain;
			this.coDomain = coDomain;
			this.dependencies = ImmutableList.of(tableId);
		}

		@Override
		public BinaryAttributeSetRelation build(Workspace workspace) {
			DataTable table = workspace.get(tableId, DataTable.class).get();
			Function<Identifier, Attribute<?>> idToAttribute = id -> table.attribute(id).get();
			return binaryAttributeSetRelation(table, Arrays.stream(domain).map(idToAttribute).collect(toSet()),
					Arrays.stream(coDomain).map(idToAttribute).collect(toSet()));
		}

		public List<Identifier> dependencyIds() {
			return dependencies;
		}

	}

	private static class FunctionalPatternImplementation extends DefaultPattern<BinaryAttributeSetRelation>
			implements FunctionalPattern {

		private final FunctionalDependencyMeasure measure;

		/**
		 * 
		 * @param population
		 * @param descriptor
		 * @param measurements
		 *            non-empty list of measurements computed for this correlation
		 *            descriptor, the first of which has to be of a
		 *            {@link FunctionalDependencyMeasure}
		 */
		public FunctionalPatternImplementation(Population population, BinaryAttributeSetRelation descriptor,
				List<Measurement> measurements) {
			super(population, descriptor, measurements);
			measure = (FunctionalDependencyMeasure) measurements.get(0).measure();
		}
		
		@Override
		public FunctionalPattern add(Measurement measurement) {
			if(this.hasMeasure(measurement.measure())) {
				return this;
			}
			
			List<Measurement> newMeasurements = Lists.newArrayList(measurements());
			newMeasurements.add(measurement);
			
			return new FunctionalPatternImplementation(this.population(), this.descriptor(),
					ImmutableList.copyOf(newMeasurements));
		}

		@Override
		public BinaryAttributeSetRelation descriptor() {
			return (BinaryAttributeSetRelation) super.descriptor();
		}

		@Override
		public FunctionalDependencyMeasure functionalityMeasure() {
			return measure;
		}

		@Override
		public SerialForm<FunctionalPattern> serialForm() {
			return new FunctionPatternSerialFormImplementation(descriptor().serialForm(), measurements());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof FunctionalPattern)) {
				return false;
			}

			FunctionalPattern that = (FunctionalPattern) o;
			return this.descriptor().domain().equals(that.descriptor().domain())
					&& this.descriptor().coDomain().equals(that.descriptor().coDomain());
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.descriptor().domain(), this.descriptor().coDomain());
		}

	}

	@KdonTypeName("functionalDependencyPattern")
	public static class FunctionPatternSerialFormImplementation implements SerialForm<FunctionalPattern> {

		@JsonProperty("descriptor")
		private final SerialForm<BinaryAttributeSetRelation> descriptor;

		@JsonProperty("measurements")
		private final List<Measurement> measurements;

		@JsonCreator
		public FunctionPatternSerialFormImplementation(
				@JsonProperty("descriptor") SerialForm<BinaryAttributeSetRelation> descriptor,
				@JsonProperty("measurements") List<Measurement> measurements) {
			this.descriptor = descriptor;
			this.measurements = measurements;
		}

		@Override
		public FunctionalPattern build(Workspace workspace) {
			BinaryAttributeSetRelation attributeRelation = descriptor.build(workspace);
			return new FunctionalPatternImplementation(attributeRelation.table().population(), attributeRelation,
					measurements);
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			return descriptor.dependencyIds();
		}

	}

	public static ComputationSpecification functionalPatternComputationSpec(Identifier id,
			BinaryAttributeSetRelationSerialForm relation) {
		return new FunctionalPatternComputationSpec(id, relation);
	}

	@KdonTypeName("functionalDependencyComputation")
	@KdonDoc("Simple computation of reliable fraction of information for a given attribute relation.")
	public static class FunctionalPatternComputationSpec implements ComputationSpecification {

		@JsonProperty("id")
		private final Identifier id;

		@JsonProperty("relation")
		@KdonDoc("The attribute relation for which to compute the reliable fraction of information.")
		private final BinaryAttributeSetRelationSerialForm attributeSetRelation;

		@JsonCreator
		private FunctionalPatternComputationSpec(@JsonProperty("id") Identifier id,
				@JsonProperty("relation") BinaryAttributeSetRelationSerialForm relation) {
			this.id = id;
			this.attributeSetRelation = relation;
		}

		@Override
		public Computation<?> build(Workspace context) throws ValidationException {
			CallableWithStopInterface<FunctionalPattern> callable = () -> functionalPattern(
					attributeSetRelation.build(context));
			return Computations.computation(callable);
		}

		@Override
		public Identifier identifier() {
			return id;
		}

	}

	@KdonTypeName("functionalDependencyDiscovery")
	@KdonDoc("Performs functional dependency discovery with OPUS.")
	public static class FunctionalDependencyDiscoverySpec implements ComputationSpecification {

		public static FunctionalDependencyDiscoverySpec functionalDependencyDiscovery(Identifier id, Identifier target,
				int num_res, double alpha) {
			return new FunctionalDependencyDiscoverySpec(id, target, num_res, alpha);
		}

		@JsonProperty("id")
		private final Identifier id;

		@JsonProperty("target")
		private final Identifier target;

		@JsonProperty("num_res")
		private final int num_res;

		@JsonProperty("alpha")
		private final double alpha;

		@JsonCreator
		private FunctionalDependencyDiscoverySpec(@JsonProperty("id") Identifier id,
				@JsonProperty("target") Identifier target, @JsonProperty("num_res") int num_res,
				@JsonProperty("alpha") double alpha) {
			this.id = id;
			this.num_res = num_res;
			this.target = target;
			this.alpha = alpha;
		}

		@Override
		public MiningAlgorithm build(Workspace context) throws ValidationException {
			Optional<DataTable> maybeTable = context.datatables().stream().findFirst();
			if (!maybeTable.isPresent()) {
				throw new ValidationException("No table found", "add a table");
			}
			DataTable table = maybeTable.get();
			Optional<? extends Attribute<?>> maybeAttribute = table.attribute(target);
			if (!maybeAttribute.isPresent()) {
				throw new ValidationException("Attribute " + target + " not found in table " + table.identifier(),
						"Available attributes: " + table.attributes());
			}

			OPUSFunctionalPatternSearch opus = new OPUSFunctionalPatternSearch(context);
			opus.target(maybeAttribute.get());
			opus.topK(num_res);
			opus.alpha(alpha);
			opus.languageOption(LanguageOption.ALL);
			opus.optimisticOption(OptimisticEstimatorOption.CHAIN);
			opus.traverseOrderOption(TraverseOrder.BREADTHFSPOTENTIAL);
			return opus;
		}

		@Override
		public Identifier identifier() {
			return id;
		}

	}

}

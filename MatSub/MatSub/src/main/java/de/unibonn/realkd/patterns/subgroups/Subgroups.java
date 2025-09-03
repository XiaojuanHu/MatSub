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
package de.unibonn.realkd.patterns.subgroups;

import static de.unibonn.realkd.patterns.models.MeanAbsoluteMedianDeviation.MEAN_ABSOLUTE_MEDIAN_DEVIATION;
import static de.unibonn.realkd.patterns.subgroups.MedianDeviationReduction.AVERAGE_ABSOLUTE_MEDIAN_DEVIATION_REDUCTION;
import static de.unibonn.realkd.patterns.subgroups.NormalizedMeanShiftRepresentativeness.NORMALIZED_MEAN_SHIFT_REPRESENTATIVENESS;
import static de.unibonn.realkd.patterns.subgroups.TotalVariationRepresentativeness.TOTAL_VARIATION_REPRESENTATIVENESS;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import de.unibonn.realkd.common.HasExportableForm;
import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.JsonSerializable;
import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.TypeForDoc;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.TableSubspaceDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptors;
import de.unibonn.realkd.patterns.models.ErrorMeasure;
import de.unibonn.realkd.patterns.models.Model;
import de.unibonn.realkd.patterns.models.ModelFactory;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistribution;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;

/**
 * <p>
 * Provides static factory methods for the construction of subgroup descriptors.
 * Subgroups are a composition of a descriptor of a sub-population of some
 * global population, a local model of some target attributes, and another
 * (usually global) reference model.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.5.0
 *
 */
public class Subgroups {

	private Subgroups() {
		; // not to be instantiated
	}

	public static <M extends Model> Subgroup<M> subgroup(LogicalDescriptor extensionDescriptor,
			ReferenceDescriptor referenceDescriptor, DataTable targetTable, List<? extends Attribute<?>> targets,
			ModelFactory<? extends M> fittingAlgorithm, M referenceModel, M localModel) {

		return new SubgroupImplementation<M>(extensionDescriptor, referenceDescriptor, targetTable, targets,
				fittingAlgorithm, referenceModel, localModel);
	}

	public static <T extends Model, C extends Model> ControlledSubgroup<T, C> controlledSubgroup(
			LogicalDescriptor extensionDescriptor, DataTable targetTable, List<Attribute<?>> targets,
			ModelFactory<? extends T> targetFittingAlgorithm, T referenceModel, T localModel,
			List<Attribute<?>> controlAttributes, ModelFactory<? extends C> controlFittingAlgorithm,
			C referenceControlModel, C localControlModel) {
		return new ControlledSubgroupImplementation<T, C>(extensionDescriptor,
				ReferenceDescriptor.global(targetTable.population()), targetTable, targets,
				targetFittingAlgorithm, referenceModel, localModel, controlAttributes, controlFittingAlgorithm,
				referenceControlModel, localControlModel);
	}

	public static <M extends Model> Subgroup<M> subgroup(LogicalDescriptor extensionDescriptor, DataTable targetTable,
			List<? extends Attribute<?>> targets, ModelFactory<? extends M> fittingAlgorithm) {
		ReferenceDescriptor globalReferenceDescriptor = ReferenceDescriptor.global(targetTable.population());
		return subgroup(extensionDescriptor, globalReferenceDescriptor, targetTable, targets, fittingAlgorithm);
	}

	public static <M extends Model> Subgroup<M> subgroup(LogicalDescriptor extensionDescriptor,
			ReferenceDescriptor referenceDescriptor, DataTable targetTable, List<? extends Attribute<?>> targets,
			ModelFactory<? extends M> fittingAlgorithm) {

		M referenceModel = fittingAlgorithm.getModel(targetTable, targets, referenceDescriptor.supportSet());
		M localModel = fittingAlgorithm.getModel(targetTable, targets, extensionDescriptor.supportSet());

		return new SubgroupImplementation<>(extensionDescriptor, referenceDescriptor, targetTable, targets,
				fittingAlgorithm, referenceModel, localModel);
	}

	private static class SubgroupImplementation<M extends Model> implements Subgroup<M> {

		private final M referenceModel;
		private final M localModel;
		private final ModelFactory<? extends M> fittingAlgorithm;
		private final DataTable targetTable;
		private final List<? extends Attribute<?>> targets;
		private final LogicalDescriptor extensionDescriptor;
		private final ReferenceDescriptor referenceDescriptor;

		private SubgroupImplementation(LogicalDescriptor extensionDescriptor, ReferenceDescriptor referenceDescriptor,
				DataTable targetTable, List<? extends Attribute<?>> targets, ModelFactory<? extends M> fittingAlgorithm,
				M referenceModel, M localModel) {
			this.fittingAlgorithm = fittingAlgorithm;
			this.referenceModel = referenceModel;
			this.localModel = localModel;
			this.targets = targets;
			this.extensionDescriptor = extensionDescriptor;
			this.referenceDescriptor = referenceDescriptor;
			this.targetTable = targetTable;
		}

		@Override
		public Subgroup<M> greedySimplification() {
			LogicalDescriptor simplifiedDescriptor = LogicalDescriptors
					.approximateShortestGenerator(this.extensionDescriptor());
			return new SubgroupImplementation<M>(simplifiedDescriptor, this.referenceDescriptor(),
					this.getTargetTable(), this.targetAttributes(), this.fittingAlgorithm(), this.referenceModel(),
					this.localModel());
		}

		@Override
		public M referenceModel() {
			return referenceModel;
		}

		@Override
		public M localModel() {
			return localModel;
		}

		@Override
		public List<? extends Attribute<?>> targetAttributes() {
			return targets;
		}

		@Override
		public IndexSet supportSet() {
			return extensionDescriptor.supportSet();
		}

		@Override
		public LogicalDescriptor extensionDescriptor() {
			return extensionDescriptor;
		}

		@Override
		public DataTable table() {
			return this.targetTable;
		}

		@Override
		public List<Attribute<?>> getReferencedAttributes() {
			Set<Attribute<?>> temp = new HashSet<>();
			if (extensionDescriptor instanceof TableSubspaceDescriptor) {
				temp.addAll(((TableSubspaceDescriptor) extensionDescriptor).getReferencedAttributes());
			}
			temp.addAll(targets);
			return new ArrayList<>(temp);
		}

		@Override
		public Population population() {
			return extensionDescriptor.population();
		}

		@Override
		public DataTable getTargetTable() {
			return targetTable;
		}

		/**
		 * Checks equality based on extension descriptor, target list, and model fitting
		 * algorithm.
		 * 
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Subgroup)) {
				return false;
			}
			Subgroup<?> that = (Subgroup<?>) o;
			return this.extensionDescriptor.equals(that.extensionDescriptor())
					&& this.targets.equals(that.targetAttributes())
					&& this.fittingAlgorithm.equals(that.fittingAlgorithm());
		}

		@Override
		public int hashCode() {
			return Objects.hash(extensionDescriptor, targets, fittingAlgorithm);
		}

		@Override
		public String toString() {
			return "Subgroup(" + extensionDescriptor.toString() + ", " + localModel.toString() + ", "
					+ referenceModel.toString() + ")";
		}

		private JsonSerializable modelExport(Model model) {
			if (model instanceof HasSerialForm<?>) {
				return ((HasSerialForm<?>) model).serialForm();
			} else if (model instanceof HasExportableForm) {
				return ((HasExportableForm) model).exportableForm();
			} else {
				return null;
			}
		}

		@Override
		public SerialForm<? extends Subgroup<M>> serialForm() {
			JsonSerializable rm = modelExport(referenceModel);
			JsonSerializable lm = modelExport(localModel);
			return new SubgroupSerialForm<>(targetTable.identifier(), extensionDescriptor().serialForm(),
					referenceDescriptor.toString(), targets.stream().map(a -> a.identifier()).toArray(i -> new Identifier[i]),
					fittingAlgorithm, rm, lm);
		}

		@Override
		public List<Integer> targetAttributeIndices() {
			return targetAttributes().stream().map(a -> targetTable.attributes().indexOf(a))
					.collect(Collectors.toList());
		}

		@Override
		public ModelFactory<? extends M> fittingAlgorithm() {
			return fittingAlgorithm;
		}

		@Override
		public ReferenceDescriptor referenceDescriptor() {
			return referenceDescriptor;
		}

	}

	private static class ControlledSubgroupImplementation<T extends Model, C extends Model>
			extends SubgroupImplementation<T> implements ControlledSubgroup<T, C> {

		private final List<Attribute<?>> controlAttributes;
		private final C referenceControlModel;
		private final C localControlModel;
		private final ModelFactory<? extends C> controlFittingAlgorithm;

		private ControlledSubgroupImplementation(LogicalDescriptor extensionDescriptor,
				ReferenceDescriptor referenceDescriptor, DataTable targetTable, List<? extends Attribute<?>> targets,
				ModelFactory<? extends T> targetFittingAlgorithm, T referenceModel, T localModel,
				List<Attribute<?>> controlAttributes, ModelFactory<? extends C> controlFittingAlgorithm,
				C referenceControlModel, C localControlModel) {
			super(extensionDescriptor, referenceDescriptor, targetTable, targets, targetFittingAlgorithm,
					referenceModel, localModel);
			this.controlAttributes = controlAttributes;
			this.controlFittingAlgorithm = controlFittingAlgorithm;
			this.referenceControlModel = referenceControlModel;
			this.localControlModel = localControlModel;
		}

		@Override
		public ControlledSubgroup<T, C> greedySimplification() {
			LogicalDescriptor simplifiedDescriptor = LogicalDescriptors
					.approximateShortestGenerator(this.extensionDescriptor());
			return new ControlledSubgroupImplementation<T, C>(simplifiedDescriptor, this.referenceDescriptor(),
					this.getTargetTable(), this.targetAttributes(), this.fittingAlgorithm(), this.referenceModel(),
					this.localModel(), controlAttributes, controlFittingAlgorithm, referenceControlModel,
					localControlModel);
		}

		@Override
		public List<Attribute<?>> controlAttributes() {
			return controlAttributes;
		}

		@Override
		public C localControlModel() {
			return localControlModel;
		}

		@Override
		public C referenceControlModel() {
			return referenceControlModel;
		}

		public SerialForm<ControlledSubgroup<T, C>> serialForm() {
			List<Identifier> targets = targetAttributes().stream().map(a -> a.identifier()).collect(Collectors.toList());
			List<Identifier> controlVars = controlAttributes.stream().map(a -> a.identifier()).collect(Collectors.toList());
			return new ControlledSubgroupSerialFormImplementation<>(getTargetTable().identifier(),
					extensionDescriptor().serialForm(), targets, fittingAlgorithm(), controlVars,
					controlFittingAlgorithm);
		}

		@Override
		public ModelFactory<? extends C> controlfittingAlgorithm() {
			return controlFittingAlgorithm;
		}

	}
	
	public static <M extends Model> SubgroupSerialForm<M> subgroupSerialForm(Identifier tableId,
			SerialForm<LogicalDescriptor> selector,
			String reference,
			Identifier[] targetAttributes,
			ModelFactory<? extends M> modellingMethod,
			JsonSerializable referenceModel,
			JsonSerializable localModel) {
		return new SubgroupSerialForm<>(tableId, selector, reference, targetAttributes, modellingMethod, referenceModel, localModel);
	}

	@KdonTypeName("subgroup")
	public static class SubgroupSerialForm<M extends Model> implements SerialForm<Subgroup<M>> {

		@JsonProperty("table")
		@KdonDoc("The id of a table in the workspace that contains the target attributes.")
		private final Identifier tableId;

		@JsonProperty("selector")
		@TypeForDoc("logicalDescriptor")
		private final SerialForm<LogicalDescriptor> selector;

		@JsonInclude(JsonInclude.Include.NON_NULL)
		@JsonProperty("reference")
		@KdonDoc("Whether subgroup complement or global sample population is modelled by reference model. Possible values 'global' (default) or 'complement'.")
		private final String reference;

		@JsonProperty("targetAttributes")
		private final Identifier[] targetAttributes;

		@JsonProperty("targetModellingMethod")
		@TypeForDoc("modellingMethod")
		private final ModelFactory<? extends M> targetModellingMethod;

		@JsonInclude(JsonInclude.Include.NON_NULL)
		@JsonProperty("targetLocalModel")
		private final JsonSerializable localModel;

		@JsonInclude(JsonInclude.Include.NON_NULL)
		@JsonProperty("targetReferenceModel")
		private final JsonSerializable referenceModel;

		@JsonCreator
		private SubgroupSerialForm(@JsonProperty("table") Identifier tableId,
				@JsonProperty("selector") SerialForm<LogicalDescriptor> selector,
				@JsonProperty("reference") String reference,
				@JsonProperty("targetAttributes") Identifier[] targetAttributes,
				@JsonProperty("targetModellingMethod") ModelFactory<? extends M> modellingMethod,
				@JsonProperty("targetReferenceModel") JsonSerializable referenceModel,
				@JsonProperty("targetLocalModel") JsonSerializable localModel) {
			this.tableId = tableId;
			this.selector = selector;
			this.reference = reference == null ? "global" : reference;
			this.targetAttributes = targetAttributes;
			this.targetModellingMethod = modellingMethod;
			this.referenceModel = referenceModel;
			this.localModel = localModel;
		}

		@Override
		public Subgroup<M> build(Workspace dataWorkspace) {
			DataTable table = dataWorkspace.get(tableId, DataTable.class).get();
			LogicalDescriptor extensionDescriptor = selector.build(dataWorkspace);
			ReferenceDescriptor referenceDescriptor = ReferenceDescriptor.fromString(reference, table.population(),
					extensionDescriptor);
			Function<Identifier, Attribute<?>> idToAttribute = a -> table.attribute(a).get();
			List<Attribute<?>> targetAttributeList = stream(targetAttributes)
					.map((Function<Identifier, Attribute<?>>) idToAttribute).collect(Collectors.toList());

			if (localModel instanceof SerialForm && referenceModel instanceof SerialForm) {
				@SuppressWarnings("unchecked")
				M lm = (M) ((SerialForm<?>) localModel).build(dataWorkspace);
				@SuppressWarnings("unchecked")
				M rm = (M) ((SerialForm<?>) referenceModel).build(dataWorkspace);
				Subgroups.subgroup(extensionDescriptor, referenceDescriptor, table, targetAttributeList,
						targetModellingMethod, rm, lm);
			}

			return Subgroups.subgroup(extensionDescriptor, referenceDescriptor, table, targetAttributeList, targetModellingMethod);
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof SubgroupSerialForm)) {
				return false;
			}
			SubgroupSerialForm<?> otherSerialForm = (SubgroupSerialForm<?>) other;
			return (this.selector.equals(otherSerialForm.selector)
					&& Arrays.equals(targetAttributes, otherSerialForm.targetAttributes)
					&& this.targetModellingMethod.equals(otherSerialForm.targetModellingMethod)
					&& this.tableId.equals(otherSerialForm.tableId));
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			// TODO model deps
			return Sets.union(ImmutableSet.copyOf(selector.dependencyIds()), ImmutableSet.of(tableId));

		}
	}

	private static class ControlledSubgroupSerialFormImplementation<T extends Model, C extends Model>
			implements SerialForm<ControlledSubgroup<T, C>> {

		@JsonProperty("tableId")
		private final Identifier tableId;

		@JsonProperty("selector")
		private final SerialForm<LogicalDescriptor> selector;

		@JsonProperty("targetAttributes")
		private final List<Identifier> targetAttributes;

		@JsonProperty("targetModellingMethod")
		private final ModelFactory<? extends T> targetModellingAlgorithm;

		@JsonProperty("controlAttributes")
		private final List<Identifier> controlAttributes;

		@JsonProperty("controlModellingMethod")
		private final ModelFactory<? extends C> controlFittingAlgorithm;

		@JsonCreator
		public ControlledSubgroupSerialFormImplementation(@JsonProperty("tableId") Identifier tableId,
				@JsonProperty("selector") SerialForm<LogicalDescriptor> selector,
				@JsonProperty("targetAttributes") List<Identifier> targetAttributes,
				@JsonProperty("targetModellingMethod") ModelFactory<? extends T> targetModellingMethod,
				@JsonProperty("controlAttributes") List<Identifier> controlAttributes,
				@JsonProperty("controlModellingMethod") ModelFactory<? extends C> controlModellingMethod) {
			this.tableId = tableId;
			this.selector = selector;
			this.targetAttributes = targetAttributes;
			this.targetModellingAlgorithm = targetModellingMethod;
			this.controlAttributes = controlAttributes;
			this.controlFittingAlgorithm = controlModellingMethod;

		}

		@Override
		public ControlledSubgroup<T, C> build(Workspace workspace) {
			DataTable table = workspace.get(tableId, DataTable.class).get();
			Function<Identifier, Attribute<?>> idToAttribute = a -> table.attribute(a).get();
			List<Attribute<?>> targetAttributeList = this.targetAttributes.stream()
					.map((Function<Identifier, Attribute<?>>) idToAttribute).collect(Collectors.toList());
			List<Attribute<?>> controlAttributeList = this.controlAttributes.stream()
					.map((Function<Identifier, Attribute<?>>) idToAttribute).collect(Collectors.toList());

			LogicalDescriptor extensionDescriptor = selector.build(workspace);
			T localTargetModel = targetModellingAlgorithm.getModel(table, targetAttributeList,
					extensionDescriptor.supportSet());
			T referenceTargetModel = targetModellingAlgorithm.getModel(table, targetAttributeList);
			C localControlModel = controlFittingAlgorithm.getModel(table, controlAttributeList,
					extensionDescriptor.supportSet());
			C referenceControlModel = controlFittingAlgorithm.getModel(table, controlAttributeList);

			return controlledSubgroup(extensionDescriptor, table, targetAttributeList, targetModellingAlgorithm,
					referenceTargetModel, localTargetModel, controlAttributeList, controlFittingAlgorithm,
					referenceControlModel, localControlModel);
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			return Sets.union(ImmutableSet.copyOf(selector.dependencyIds()), ImmutableSet.of(tableId));

		}

	}

	/**
	 * Generates a representative measurement for a controlled subgroup using a
	 * default measure for the given control model class.
	 * 
	 * @param subgroup
	 *            a subgroup with control variable
	 * @return representativeness measurement of subgroup
	 */
	public static Optional<Measurement> representativenessMeasurement(ControlledSubgroup<?, ?> subgroup) {
		if (subgroup.localControlModel() instanceof MetricEmpiricalDistribution
				&& subgroup.referenceControlModel() instanceof MetricEmpiricalDistribution) {
			@SuppressWarnings("unchecked")
			ControlledSubgroup<?, MetricEmpiricalDistribution> subgroupWithMetricModel = (ControlledSubgroup<?, MetricEmpiricalDistribution>) subgroup;
			return Optional.of(NORMALIZED_MEAN_SHIFT_REPRESENTATIVENESS.measurement(subgroupWithMetricModel));
		} else if (subgroup.localControlModel() instanceof ContingencyTable
				&& subgroup.referenceControlModel() instanceof ContingencyTable) {
			@SuppressWarnings("unchecked")
			ControlledSubgroup<?, ContingencyTable> subgroupWithTableModel = (ControlledSubgroup<?, ContingencyTable>) subgroup;
			return Optional.of(TOTAL_VARIATION_REPRESENTATIVENESS.measurement(subgroupWithTableModel));
		}
		return empty();
	}

	private static final Map<ErrorMeasure, ErrorReductionMeasure> ERROR_TO_REDUCTION_MEASURE = new HashMap<>();
	static {
		ERROR_TO_REDUCTION_MEASURE.put(MEAN_ABSOLUTE_MEDIAN_DEVIATION, AVERAGE_ABSOLUTE_MEDIAN_DEVIATION_REDUCTION);
	}

	public static Optional<Measurement> accuracyGainMeasurement(Measurement m1, Measurement m2) {
		ErrorReductionMeasure gainMeasure = ERROR_TO_REDUCTION_MEASURE.get(m1.measure());
		if (gainMeasure != null && m1.measure() == m2.measure()) {
			double gain = Math.max(0, (m2.value() - m1.value()) / m2.value());
			return Optional.of(Measures.measurement(gainMeasure, gain));
		}
		return empty();
	}

	public static List<Measurement> accuracyGainMeasurements(Subgroup<?> subgroup) {
		List<Measurement> localAccuracies = subgroup.localModel().measurements(ErrorMeasure.class);
		Stream<Measurement> map = localAccuracies.stream().map(m1 -> subgroup.referenceModel().measurement(m1.measure())
				.flatMap(m2 -> accuracyGainMeasurement(m1, m2))).filter(o -> o.isPresent()).map(o -> o.get());
		return map.collect(toList());
	}

}

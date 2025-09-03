/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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
package de.unibonn.realkd.common.measures;

import static de.unibonn.realkd.computations.core.ComputationTime.COMPUTATION_TIME;
import static de.unibonn.realkd.computations.dag.MaxAttainedBoundarySize.MAX_ATTAINED_BOUNDARY_SIZE;
import static de.unibonn.realkd.computations.dag.MaxAttainedDepth.MAX_ATTAINED_DEPTH;
import static de.unibonn.realkd.computations.dag.NodesCreated.NODES_CREATED;
import static de.unibonn.realkd.computations.dag.NodesDiscarded.NODES_DISCARDED;
import static de.unibonn.realkd.computations.dag.SolutionDepth.SOLUTION_DEPTH;
import static de.unibonn.realkd.patterns.Frequency.FREQUENCY;
import static de.unibonn.realkd.patterns.QualityMeasureId.ABSOLUTE_LIFT;
import static de.unibonn.realkd.patterns.QualityMeasureId.EXPECTED_FREQUENCY;
import static de.unibonn.realkd.patterns.QualityMeasureId.FREQUENCY_DEVIATION;
import static de.unibonn.realkd.patterns.QualityMeasureId.LOCAL_ENTROPY;
import static de.unibonn.realkd.patterns.QualityMeasureId.LOCAL_MODE_PROBABILITY;
import static de.unibonn.realkd.patterns.QualityMeasureId.LOCAL_PEARSON;
import static de.unibonn.realkd.patterns.QualityMeasureId.LOCAL_RMSE;
import static de.unibonn.realkd.patterns.QualityMeasureId.LOCAL_STD;
import static de.unibonn.realkd.patterns.QualityMeasureId.LOCAL_WEIBULL_SCALE;
import static de.unibonn.realkd.patterns.QualityMeasureId.NEGATIVE_LIFT;
import static de.unibonn.realkd.patterns.QualityMeasureId.OUTLIER_SCORE;
import static de.unibonn.realkd.patterns.QualityMeasureId.REFERENCE_PEARSON;
import static de.unibonn.realkd.patterns.QualityMeasureId.REFERENCE_RMSE;
import static de.unibonn.realkd.patterns.QualityMeasureId.REFERENCE_STD;
import static de.unibonn.realkd.patterns.QualityMeasureId.REFERENCE_WEIBULL_SCALE;
import static de.unibonn.realkd.patterns.QualityMeasureId.REF_ENTROPY;
import static de.unibonn.realkd.patterns.QualityMeasureId.WEIBULL_SCALE;
import static de.unibonn.realkd.patterns.Support.SUPPORT;
import static de.unibonn.realkd.patterns.emm.AngularDistanceOfSlopes.ANGULAR_DISTANCE_OF_SLOPES;
import static de.unibonn.realkd.patterns.emm.CumulativeJensenShannonDivergence.CJS;
import static de.unibonn.realkd.patterns.emm.HellingerDistance.HELLINGER_DISTANCE;
import static de.unibonn.realkd.patterns.emm.KolmogorovSmirnovStatistic.KOLMOGOROV_SMIRNOV_STATISTIC;
import static de.unibonn.realkd.patterns.emm.KullbackLeiblerDivergence.KL_DIVERGENCE;
import static de.unibonn.realkd.patterns.emm.ManhattenMeanDistance.MANHATTAN_MEAN_DISTANCE;
import static de.unibonn.realkd.patterns.emm.NormalizedAbsoluteMeanShift.NORMALIZED_ABSOLUTE_MEAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedAbsoluteMedianShift.NORMALIZED_ABSOLUTE_MEDIAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedNegativeMeanShift.NORMALIZED_NEGATIVE_MEAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedNegativeMedianShift.NORMALIZED_NEGATIVE_MEDIAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedMaxWithConstatntRef.NORMALIZED_MAX_CONSTANT_REF;
import static de.unibonn.realkd.patterns.emm.NormalizedMax.NORMALIZED_MAX;
import static de.unibonn.realkd.patterns.emm.NormalizedMin.NORMALIZED_MIN;
import static de.unibonn.realkd.patterns.emm.NormalizedPositiveMeanShift.NORMALIZED_POSITIVE_MEAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedPositiveMedianShift.NORMALIZED_POSITIVE_MEDIAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.ReliableConditionalEffect.RELIABLE_CONDITIONAL_EFFECT;
import static de.unibonn.realkd.patterns.emm.TotalVariationDistance.TOTAL_VARIATION_DISTANCE;
import static de.unibonn.realkd.patterns.episodes.EpisodeRuleConfidence.EPISODE_RULE_CONDIFENCE;
import static de.unibonn.realkd.patterns.episodes.EpisodeSupport.EPISODE_SUPPORT;
import static de.unibonn.realkd.patterns.functional.CoDomainAmbiguityCount.CODOMAIN_AMBIGUITY_COUNT;
import static de.unibonn.realkd.patterns.functional.CoDomainEntropy.CODOMAIN_ENTROPY;
import static de.unibonn.realkd.patterns.functional.ExpectedMutualInformation.EXPECTED_MUTUAL_INFORMATION;
import static de.unibonn.realkd.patterns.functional.FractionOfInformation.FRACTION_OF_INFORMATION;
import static de.unibonn.realkd.patterns.functional.ReliableFractionOfInformation.RELIABLE_FRACTION_OF_INFORMATION;
import static de.unibonn.realkd.patterns.logical.Area.AREA;
import static de.unibonn.realkd.patterns.logical.ItemsetLeverage.ITEMSET_LEVERAGE;
import static de.unibonn.realkd.patterns.logical.Lift.LIFT;
import static de.unibonn.realkd.patterns.models.MeanAbsoluteMedianDeviation.MEAN_ABSOLUTE_MEDIAN_DEVIATION;
import static de.unibonn.realkd.patterns.models.table.MutualInformation.MUTUAL_INFORMATION;
import static de.unibonn.realkd.patterns.models.table.ShannonEntropy.ENTROPY;
import static de.unibonn.realkd.patterns.rules.Confidence.CONFIDENCE;
import static de.unibonn.realkd.patterns.rules.ConfidenceLift.CONFIDENCE_LIFT;
import static de.unibonn.realkd.patterns.rules.MinimalAllConfidenceLift.MINIMAL_ALL_CONFIDENCE_LIFT;
import static de.unibonn.realkd.patterns.rules.MinimalConfidenceLift.MINIMAL_CONFIDENCE_LIFT;
import static de.unibonn.realkd.patterns.rules.OddsRatio.ODDS_RATIO;
import static de.unibonn.realkd.patterns.rules.PhiCoefficient.PHI_COEFFICIENT;
import static de.unibonn.realkd.patterns.rules.RuleLift.RULE_LIFT;
import static de.unibonn.realkd.patterns.rules.WeightedRelativeAccuracy.WRACC;
import static de.unibonn.realkd.patterns.sequence.Cohesion.COHESION;
import static de.unibonn.realkd.patterns.sequence.Interestingness.INTERESTINGNESS;
import static de.unibonn.realkd.patterns.subgroups.AbsolutePearsonCorrelationGain.ABSOLUTE_PEARSON_GAIN;
import static de.unibonn.realkd.patterns.subgroups.EntropyReduction.ENTROPY_REDUCTION;
import static de.unibonn.realkd.patterns.subgroups.HellingerRepresentativeness.HELLINGER_REPRESENTATIVENESS;
import static de.unibonn.realkd.patterns.subgroups.MedianDeviationReduction.AVERAGE_ABSOLUTE_MEDIAN_DEVIATION_REDUCTION;
import static de.unibonn.realkd.patterns.subgroups.MutualInformationGain.MUTUAL_INFORMATION_GAIN;
import static de.unibonn.realkd.patterns.subgroups.NormalizedMeanShiftRepresentativeness.NORMALIZED_MEAN_SHIFT_REPRESENTATIVENESS;
import static de.unibonn.realkd.patterns.subgroups.RootMeanSquaredErrorReduction.RMSE_REDUCTION;
import static de.unibonn.realkd.patterns.subgroups.StandardDeviationReduction.STD_REDUCTION;
import static de.unibonn.realkd.patterns.subgroups.TotalVariationRepresentativeness.TOTAL_VARIATION_REPRESENTATIVENESS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.unibonn.realkd.algorithms.ComputationMeasure;
import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.patterns.emm.ModelDeviationMeasure;
import de.unibonn.realkd.patterns.episodes.EpisodeRuleConfidence;
import de.unibonn.realkd.patterns.episodes.EpisodeSupport;
import de.unibonn.realkd.patterns.episodes.Episodes;
import de.unibonn.realkd.patterns.functional.FunctionalDependencyMeasure;
import de.unibonn.realkd.patterns.models.ErrorMeasure;
import de.unibonn.realkd.patterns.models.GoodnessOfFitMeasure;
import de.unibonn.realkd.patterns.rules.ConfidenceLift;
import de.unibonn.realkd.patterns.rules.MinimalAllConfidenceLift;
import de.unibonn.realkd.patterns.rules.MinimalConfidenceLift;
import de.unibonn.realkd.patterns.rules.OddsRatio;
import de.unibonn.realkd.patterns.rules.PhiCoefficient;
import de.unibonn.realkd.patterns.rules.WeightedRelativeAccuracy;
import de.unibonn.realkd.patterns.subgroups.ErrorReductionMeasure;

/**
 * Provides centralized access to measures.
 * 
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.5.0
 * 
 * @version 0.7.1
 *
 */
public class Measures {

	private Measures() {
		;
	}

	private static final Logger LOGGER = Logger.getLogger(Measures.class.getName());

	private static final Map<Identifier, Measure> MEASURES = new HashMap<>();

	public static final void registerMeasure(Measure measure) {
		Identifier id = (measure instanceof Identifiable) ? ((Identifiable) measure).identifier()
				: Identifier.id(measure.toString());
		if (MEASURES.containsKey(id)) {
			LOGGER.warning("Measure " + id + " already registered; skipping");
			return;
		}
		MEASURES.put(id, measure);
	}

	public static final void registerMeasures(Measure... measures) {
		for (Measure measure : measures)
			registerMeasure(measure);
	}

	static {
		registerMeasures(ABSOLUTE_LIFT, ABSOLUTE_PEARSON_GAIN, ANGULAR_DISTANCE_OF_SLOPES, ITEMSET_LEVERAGE, AREA,
				AVERAGE_ABSOLUTE_MEDIAN_DEVIATION_REDUCTION, CJS, CODOMAIN_AMBIGUITY_COUNT, CODOMAIN_ENTROPY,
				RELIABLE_CONDITIONAL_EFFECT, COMPUTATION_TIME, CONFIDENCE, ENTROPY, ENTROPY_REDUCTION,
				EXPECTED_FREQUENCY, EXPECTED_MUTUAL_INFORMATION, FRACTION_OF_INFORMATION, FREQUENCY,
				FREQUENCY_DEVIATION, HELLINGER_DISTANCE, HELLINGER_REPRESENTATIVENESS, KL_DIVERGENCE,
				KOLMOGOROV_SMIRNOV_STATISTIC, LIFT, LOCAL_ENTROPY, LOCAL_MODE_PROBABILITY, LOCAL_PEARSON, LOCAL_RMSE,
				LOCAL_STD, LOCAL_WEIBULL_SCALE, MANHATTAN_MEAN_DISTANCE, MAX_ATTAINED_DEPTH, MAX_ATTAINED_BOUNDARY_SIZE,
				MEAN_ABSOLUTE_MEDIAN_DEVIATION, MUTUAL_INFORMATION, MUTUAL_INFORMATION_GAIN, NEGATIVE_LIFT,
				NODES_CREATED, NODES_DISCARDED, NORMALIZED_ABSOLUTE_MEAN_SHIFT, NORMALIZED_ABSOLUTE_MEDIAN_SHIFT,
				NORMALIZED_MEAN_SHIFT_REPRESENTATIVENESS, NORMALIZED_NEGATIVE_MEAN_SHIFT,
				 NORMALIZED_NEGATIVE_MEDIAN_SHIFT, NORMALIZED_MAX_CONSTANT_REF, NORMALIZED_MAX, NORMALIZED_MIN,
				 NORMALIZED_POSITIVE_MEAN_SHIFT, NORMALIZED_POSITIVE_MEDIAN_SHIFT,
				OUTLIER_SCORE, REF_ENTROPY, REFERENCE_PEARSON, REFERENCE_RMSE, REFERENCE_STD, REFERENCE_WEIBULL_SCALE,
				RELIABLE_FRACTION_OF_INFORMATION, RULE_LIFT, RMSE_REDUCTION, COHESION,
				INTERESTINGNESS, SOLUTION_DEPTH, STD_REDUCTION, SUPPORT, TOTAL_VARIATION_DISTANCE,
				TOTAL_VARIATION_REPRESENTATIVENESS, WEIBULL_SCALE, EPISODE_SUPPORT, EPISODE_RULE_CONDIFENCE);
	}

	public static Measure measure(Identifier id) {
		Measure entry = MEASURES.get(id);
		return (entry != null) ? entry : new UnknownMeasure(id);
	}

	public static Measurement measurement(Measure measure, double value) {
		return new Measurement(measure, value);
	}

	public static Measurement measurement(Measure measure, double value, List<Measurement> auxiliaryMeasurements) {
		return new Measurement(measure, value, auxiliaryMeasurements);
	}

	private static class UnknownMeasure implements Measure, Identifiable, ModelDeviationMeasure, ComputationMeasure,
			ErrorMeasure, ErrorReductionMeasure, FunctionalDependencyMeasure, GoodnessOfFitMeasure {

		private final Identifier id;

		private final String caption;

		private UnknownMeasure(Identifier id) {
			this.id = id;
			this.caption = "?" + id.toString() + "?";
		}

		@Override
		public String caption() {
			return caption;
		}

		@Override
		public String description() {
			return "No measure with id " + id + " was found in measure register";
		}

		@Override
		public Identifier identifier() {
			return id;
		}

		@Override
		public boolean isApplicable(Object descriptor) {
			return false;
		}

		@Override
		public ModelDeviationMeasure getMeasure() {
			return this;
		}

		@Override
		public Measurement perform(Object descriptor) {
			return measurement(this, Double.NaN);
		}

	}

}

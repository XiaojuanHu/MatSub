package de.unibonn.realkd.visualization.pattern;

import java.util.function.Function;

import org.jfree.chart.JFreeChart;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.models.UnivariateOrdinalProbabilisticModel;
import de.unibonn.realkd.patterns.subgroups.Subgroup;
import de.unibonn.realkd.visualization.JFChartPainter;

public class UnivariateCumulativeDensityPlot implements PatternVisualization {

	/**
	 * constructor only to be invoked from Visualization Register
	 */
	UnivariateCumulativeDensityPlot() {
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		return (pattern.descriptor() instanceof Subgroup
				&& ((Subgroup<?>) pattern.descriptor()).referenceModel() instanceof UnivariateOrdinalProbabilisticModel
				&& ((Subgroup<?>) pattern.descriptor()).targetAttributes().get(0) instanceof MetricAttribute
				&& ((Subgroup<?>) pattern.descriptor()).supportSet().size() > 1);
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		final Subgroup<?> subgroup = (Subgroup<?>) pattern.descriptor();
		MetricAttribute target = (MetricAttribute) subgroup.targetAttributes().get(0);
		return JFChartPainter.PREVIEW.functionsPlot("", cumulativeDistributionFunctions(subgroup), target.min(),
				target.max(), target.caption());
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		final Subgroup<?> subgroup = (Subgroup<?>) pattern.descriptor();
		MetricAttribute target = (MetricAttribute) subgroup.targetAttributes().get(0);
		return JFChartPainter.DETAILED.functionsPlot("cumulative distribution", cumulativeDistributionFunctions(subgroup), target.min(),
				target.max(), target.caption());
	}

	private ImmutableList<Function<Double, Double>> cumulativeDistributionFunctions(final Subgroup<?> subgroup) {
		// TODO how to make the following type safe?
		@SuppressWarnings("unchecked")
		UnivariateOrdinalProbabilisticModel<Double> globalModel = (UnivariateOrdinalProbabilisticModel<Double>) subgroup
				.referenceModel();
		@SuppressWarnings("unchecked")
		UnivariateOrdinalProbabilisticModel<Double> localModel = (UnivariateOrdinalProbabilisticModel<Double>) subgroup
				.localModel();
		ImmutableList<Function<Double, Double>> functions = ImmutableList
				.of(globalModel.cumulativeDistributionFunction(), localModel.cumulativeDistributionFunction());
		return functions;
	}

}

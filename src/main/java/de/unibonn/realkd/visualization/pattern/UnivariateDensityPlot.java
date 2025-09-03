package de.unibonn.realkd.visualization.pattern;

import org.jfree.chart.JFreeChart;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.models.UnivariateContinuousProbabilisticModel;
import de.unibonn.realkd.patterns.subgroups.Subgroup;
import de.unibonn.realkd.visualization.JFChartPainter;

/**
 * Visualization of EMM patterns with univariate probabilistic models. Draws
 * density function of local and global model by sampling it on a regular grid
 * with 50 points from the empirical min to the empirical max value of the
 * target attribute.
 * 
 * @author Ruofang Xu
 * 
 * @since 0.0.1
 * 
 * @version 0.2.2
 *
 */
public class UnivariateDensityPlot implements PatternVisualization {
	//
	// private static final String SUBGROUP_LABEL = "subgroup";
	//
	// private static final String GLOBAL_LABEL = "global";

	/**
	 * constructor only to be invoked from Visualization Register
	 */
	UnivariateDensityPlot() {
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		return (pattern.descriptor() instanceof Subgroup
				&& ((Subgroup<?>) pattern.descriptor())
						.referenceModel() instanceof UnivariateContinuousProbabilisticModel
				&& ((Subgroup<?>) pattern.descriptor()).supportSet().size() > 1);
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		final Subgroup<?> subgroup = (Subgroup<?>) pattern.descriptor();
		UnivariateContinuousProbabilisticModel globalModel = (UnivariateContinuousProbabilisticModel) subgroup
				.referenceModel();
		UnivariateContinuousProbabilisticModel localModel = (UnivariateContinuousProbabilisticModel) subgroup
				.localModel();
		MetricAttribute target = (MetricAttribute) subgroup.targetAttributes().get(0);
		return JFChartPainter.PREVIEW.functionsPlot("",
				ImmutableList.of(globalModel.densityFunction(), localModel.densityFunction()), target.min(),
				target.max(), subgroup.targetAttributes().get(0).caption());
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		return draw(pattern);
	}

}

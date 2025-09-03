package de.unibonn.realkd.visualization.pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.jfree.chart.JFreeChart;

import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;
import de.unibonn.realkd.visualization.JFChartPainter;

public class TargetPointCloud implements PatternVisualization {

	TargetPointCloud() {
		;
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		return pattern instanceof ExceptionalModelPattern
				&& checkExactlyTwoMetricTargets(((ExceptionalModelPattern) pattern)
						.descriptor().targetAttributes());
	}

	protected boolean checkExactlyTwoMetricTargets(
			List<? extends Attribute<?>> targetAttributes) {
		if (targetAttributes.size() != 2) {
			return false;
		}
		for (Attribute<?> attribute : targetAttributes) {
			if (!(attribute instanceof MetricAttribute)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		checkArgument(isApplicable(pattern));
		ExceptionalModelPattern exceptionalModelPattern = (ExceptionalModelPattern) pattern;

		JFChartPainter painter = JFChartPainter.PREVIEW;

		List<List<JFChartPainter.Point>> pointLists = newArrayList();
		pointLists.add(JFChartPainter.createPoints(exceptionalModelPattern
				.population().objectIds(),
				(MetricAttribute) exceptionalModelPattern.descriptor()
						.targetAttributes().get(0),
				(MetricAttribute) exceptionalModelPattern.descriptor()
						.targetAttributes().get(1)));

		pointLists.add(JFChartPainter.createPoints(exceptionalModelPattern
				.descriptor().supportSet(),
				(MetricAttribute) exceptionalModelPattern.descriptor()
						.targetAttributes().get(0),
				(MetricAttribute) exceptionalModelPattern.descriptor()
						.targetAttributes().get(1)));

		return painter.createPointCloud("", pointLists, exceptionalModelPattern
				.descriptor().targetAttributes().get(0).caption(),
				exceptionalModelPattern.descriptor().targetAttributes()
						.get(1).caption());

	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		return draw(pattern);
	}
}

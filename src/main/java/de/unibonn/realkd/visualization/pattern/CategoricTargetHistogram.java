package de.unibonn.realkd.visualization.pattern;

import static de.unibonn.realkd.visualization.JFChartPainter.DETAILED;
import static de.unibonn.realkd.visualization.JFChartPainter.PREVIEW;
import static de.unibonn.realkd.visualization.JFChartPainter.series;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;

import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.subgroups.Subgroup;
import de.unibonn.realkd.visualization.JFChartPainter;
import de.unibonn.realkd.visualization.JFChartPainter.Series;

public class CategoricTargetHistogram implements PatternVisualization {

	private static final String SUBGROUP_LABEL = "local";

	private static final String GLOBAL_LABEL = "global";

	CategoricTargetHistogram() {
		;
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		return (pattern.descriptor() instanceof Subgroup
				&& ((Subgroup<?>) pattern.descriptor()).targetAttributes().size() == 1
				&& ((Subgroup<?>) pattern.descriptor()).targetAttributes().get(0) instanceof CategoricAttribute);
	}

	private CategoryDataset dataset(Subgroup<?> subgroup) {
		CategoricAttribute<?> targetAttribute = (CategoricAttribute<?>) subgroup.targetAttributes().get(0);
		Series localSeries = series(SUBGROUP_LABEL, targetAttribute.categories(),
				targetAttribute.getCategoryFrequenciesOnRows(subgroup.supportSet()));
		Series globalSeries = series(GLOBAL_LABEL, targetAttribute.categories(), targetAttribute.categoryFrequencies());
		return JFChartPainter.categoryDataset(new Series[] { localSeries, globalSeries });
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		Subgroup<?> subgroup = (Subgroup<?>) pattern.descriptor();
		return PREVIEW.createLayeredBarChart("", dataset(subgroup));
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		Subgroup<?> subgroup = (Subgroup<?>) pattern.descriptor();
		return DETAILED.createLayeredBarChart("Target category frequencies", dataset(subgroup));
	}

}

package de.unibonn.realkd.visualization.pattern;

import static de.unibonn.realkd.visualization.JFChartPainter.series;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;
import de.unibonn.realkd.patterns.subgroups.ControlledSubgroup;
import de.unibonn.realkd.visualization.JFChartPainter;
import de.unibonn.realkd.visualization.JFChartPainter.Series;

/**
 * Layered bar chart for controlled subgroups with categorical control variable
 * (showing local and reference category frequencies).
 * 
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public class CategoricControlHistogram implements PatternVisualization {

	private static final String SUBGROUP_LABEL = "local";

	private static final String GLOBAL_LABEL = "global";

	CategoricControlHistogram() {
		;
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		return (pattern.descriptor() instanceof ControlledSubgroup
				&& ((ControlledSubgroup<?, ?>) pattern.descriptor()).localControlModel() instanceof ContingencyTable				
				&& ((ControlledSubgroup<?, ?>) pattern.descriptor()).referenceControlModel() instanceof ContingencyTable
				&& ((ContingencyTable)((ControlledSubgroup<?, ?>) pattern.descriptor()).localControlModel()).dimensions().size()==1
				&& ((ContingencyTable)((ControlledSubgroup<?, ?>) pattern.descriptor()).referenceControlModel()).dimensions().size()==1);
	}

	private CategoryDataset dataset(ControlledSubgroup<?, ?> subgroup) {
		ContingencyTable localTable = (ContingencyTable) subgroup.localControlModel();
		ContingencyTable globalTable = (ContingencyTable) subgroup.referenceControlModel();
		Series localSeries = series(SUBGROUP_LABEL, localTable.dimension(0).binCaptions(), probabilities(localTable));
		Series globalSeries = series(GLOBAL_LABEL, globalTable.dimension(0).binCaptions(), probabilities(globalTable));
		return JFChartPainter.categoryDataset(new Series[] { localSeries, globalSeries });
	}

	private List<Double> probabilities(ContingencyTable table) {
		List<Double> localProbs = IntStream.range(0, table.dimension(0).numberOfBins())
				.mapToObj(i -> ImmutableList.of(i)).map(key -> table.probabilityOfBins(key))
				.collect(Collectors.toList());
		return localProbs;
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		ControlledSubgroup<?, ?> subgroup = (ControlledSubgroup<?, ?>) pattern.descriptor();
		return JFChartPainter.PREVIEW.createLayeredBarChart("", dataset(subgroup));
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		ControlledSubgroup<?, ?> subgroup = (ControlledSubgroup<?, ?>) pattern.descriptor();
		return JFChartPainter.DETAILED.createLayeredBarChart("Control category frequencies", dataset(subgroup));
	}

}

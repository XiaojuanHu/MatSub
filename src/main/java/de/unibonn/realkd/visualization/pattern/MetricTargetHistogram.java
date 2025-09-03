package de.unibonn.realkd.visualization.pattern;

import static de.unibonn.realkd.visualization.JFChartPainter.DETAILED;
import static de.unibonn.realkd.visualization.JFChartPainter.PREVIEW;
import static de.unibonn.realkd.visualization.JFChartPainter.categoryDataset;

import java.util.List;

import org.jfree.chart.JFreeChart;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.subgroups.Subgroup;
import de.unibonn.realkd.visualization.ReducedEnglishDecimalFormat;

public class MetricTargetHistogram implements PatternVisualization {

	private static final String SUBGROUP_LABEL = "subgroup";

	private static final String GLOBAL_LABEL = "global";

	MetricTargetHistogram() {
		;
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		return (pattern.descriptor() instanceof Subgroup
				&& ((Subgroup<?>) pattern.descriptor()).targetAttributes().size() == 1
				&& ((Subgroup<?>) pattern.descriptor()).targetAttributes().get(0) instanceof MetricAttribute
				&& !(((Subgroup<?>) pattern.descriptor()).targetAttributes().get(0) instanceof CategoricAttribute));
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		MetricAttribute targetAttribute = (MetricAttribute) ((Subgroup<?>) pattern.descriptor()).targetAttributes().get(0);
		Data data = getNumericHistogramSpecs((MetricAttribute) targetAttribute, pattern);
		return PREVIEW.createLayeredBarChart("", categoryDataset(data.series, data.column, data.values));
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		MetricAttribute targetAttribute = (MetricAttribute) ((Subgroup<?>) pattern.descriptor()).targetAttributes().get(0);
		Data data = getNumericHistogramSpecs((MetricAttribute) targetAttribute, pattern);
		return DETAILED.createLayeredBarChart("Target histograms", categoryDataset(data.series, data.column, data.values));
	}

	private static double[] getNumericHistogramValues(Subgroup<?> descriptor, int numOfBins) {
		double[] res = new double[2 * numOfBins];

		MetricAttribute attr = ((MetricAttribute) descriptor.targetAttributes().get(0));
		double range = attr.max() - attr.min();

		IndexSet supportSet = descriptor.supportSet();

		List<Integer> rowIndices = attr.sortedNonMissingRowIndices();
		int pos = 0;
		for (int i = 0; i < numOfBins; i++) {
			double bucketBound = attr.min() + (i + 1) * range / (double) numOfBins;
			res[i + numOfBins] = 0.0;
			while (pos < rowIndices.size() && ((MetricAttribute) attr).value(rowIndices.get(pos)) <= bucketBound) {
				res[i + numOfBins] += 1.0;
				if (supportSet.contains(rowIndices.get(pos))) {
					res[i] += 1.0;
				}
				pos += 1;
			}
			res[i + numOfBins] /= rowIndices.size();
			res[i] /= supportSet.size();
		}

		return res;
	}

	private static int getNumberOfBinsForNumeric(MetricAttribute attribute) {
		return (int) Math.round(Math.ceil(Math.log(attribute.numberOfNonMissingValues()) / Math.log(2)));
	}

	private static class Data {
		String[] series;
		String[] column;
		double[] values;
	}

	private static Data getNumericHistogramSpecs(MetricAttribute targetAttribute, Pattern<?> pattern) {
		int numOfBins = getNumberOfBinsForNumeric(targetAttribute);
		Data data = new Data();
		data.series = new String[numOfBins * 2];
		data.column = new String[numOfBins * 2];

		ReducedEnglishDecimalFormat formatter = new ReducedEnglishDecimalFormat();
		double range = targetAttribute.max() - targetAttribute.min();
		for (int i = 0; i < numOfBins; i++) {
			data.series[i] = SUBGROUP_LABEL;
			data.series[numOfBins + i] = GLOBAL_LABEL;

			data.column[i] = formatter.format(targetAttribute.min() + (i + 1) * range / (double) numOfBins);
			data.column[numOfBins + i] = formatter.format(targetAttribute.min() + (i + 1) * range / (double) numOfBins);
		}

		data.values = getNumericHistogramValues((Subgroup<?>) pattern.descriptor(), numOfBins);
		return data;
	}

}

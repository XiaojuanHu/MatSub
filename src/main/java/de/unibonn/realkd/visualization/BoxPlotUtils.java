package de.unibonn.realkd.visualization;

import java.util.List;

import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;

public class BoxPlotUtils {

	public static Number getMinOutlier(BoxAndWhiskerCategoryDataset bawDataset,
			int row, int column) {
		List<Double> outliers = bawDataset.getOutliers(row, column);
		if (outliers.isEmpty()) {
			return null;
		}
		double min = outliers.get(0);
		for (double outlier : outliers) {
			min = Math.min(min, outlier);
		}
		return min < bawDataset.getMinRegularValue(row, column).doubleValue() ? min
				: null;
	}

	public static double getMinRange(BoxAndWhiskerCategoryDataset bawDataset,
			int row, int column) {
		Number min = getMinOutlier(bawDataset, row, column);
		if (min == null) {
			return bawDataset.getMinOutlier(row, column).doubleValue();
		}
		return Math.min(min.doubleValue(), bawDataset
				.getMinOutlier(row, column).doubleValue());
	}

	public static Number getMaxOutlier(BoxAndWhiskerCategoryDataset bawDataset,
			int row, int column) {
		@SuppressWarnings("unchecked")
		List<Double> outliers = bawDataset.getOutliers(row, column);
		if (outliers.isEmpty()) {
			return null;
		}
		double max = outliers.get(0);
		for (double outlier : outliers) {
			max = Math.max(max, outlier);
		}
		return max > bawDataset.getMaxRegularValue(row, column).doubleValue() ? max
				: null;
	}

	public static double getMaxRange(BoxAndWhiskerCategoryDataset bawDataset,
			int row, int column) {
		Number max = getMaxOutlier(bawDataset, row, column);
		if (max == null) {
			return bawDataset.getMaxOutlier(row, column).doubleValue();
		}
		return Math.max(max.doubleValue(), bawDataset
				.getMaxOutlier(row, column).doubleValue());
	}
}

package edu.uab.cftp.sampling.distribution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uab.cftp.sampling.distribution.GeneralDistribution.SupportMeasure;
import edu.uab.cftp.sampling.distribution.tool.StarOperation;
import edu.uab.consapt.sampling.DistributionFactor;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;

public class GeneralDistributionTesterBPI extends DistributionTester {

	private final StarOperation starOperation;
	private final List<DistributionFactor> qFunctions;
	private final double defaultBias;
	private Map<Integer, Double> itemBiases = new HashMap<Integer, Double>();

	public GeneralDistributionTesterBPI(PosNegDbInterface posNegDb,
			List<DistributionFactor> qFunctions, StarOperation starOperation,
			double defaultBias, Map<Integer, Double> itemBiases) {
		super(posNegDb);
		this.qFunctions = qFunctions;
		this.starOperation = starOperation;
		this.defaultBias = defaultBias;
		this.itemBiases.putAll(itemBiases);
	}

	public void addBias(int item, double bias) {
		itemBiases.put(item, bias);
	}

	public double bias(int id) {
		Double bias = itemBiases.get(id);
		if (bias != null) {
			return bias;
		}
		return defaultBias;
	}

	@Override
	public double computeMeasureValue(PlainItemSet itemSet) {
		double value = 0;
		if (this.starOperation.equals(StarOperation.MULTIPLICATIVE)) {
			double patternBias = 1;
			for (PlainItem item : itemSet) {
				patternBias *= bias(item.getId());
			}
			value = patternBias;
		} else {
			double patternBias = 0;
			for (PlainItem item : itemSet) {
				patternBias += bias(item.getId());
			}
			value = patternBias;
		}
		for (DistributionFactor qFunction : this.qFunctions) {
			PlainItemSet is = qFunction.getDataPortion().getItemSetInPortion(
					itemSet);
			if (qFunction.getSupportMeasure().equals(SupportMeasure.POSITIVE)) {
				value *= is.getTIDs().cardinality();
			} else {
				value *= (qFunction.getDataPortion().size() - is.getTIDs()
						.cardinality());
			}
		}
		return value;
	}
}

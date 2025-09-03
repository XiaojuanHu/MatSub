package edu.uab.cftp.sampling.distribution;

import java.util.List;

import edu.uab.cftp.sampling.distribution.GeneralDistribution.SupportMeasure;
import edu.uab.cftp.sampling.distribution.tool.StarOperation;
import edu.uab.consapt.sampling.DistributionFactor;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;

public class GeneralDistributionTester extends DistributionTester {

	private final StarOperation starOperation;
	private final double biasWeight;
	private final List<DistributionFactor> qFunctions;

	public GeneralDistributionTester(PosNegDbInterface db,
			List<DistributionFactor> qFunctions, StarOperation starOperation,
			double biasWeight) {
		super(db);
		this.qFunctions = qFunctions;
		this.starOperation = starOperation;
		this.biasWeight = biasWeight;
	}

	@Override
	public double computeMeasureValue(PlainItemSet itemSet) {
		double value = 0;
		if (this.starOperation.equals(StarOperation.MULTIPLICATIVE)) {
			double patternBias = Math.pow(this.biasWeight, itemSet.size());
			value = patternBias;

		} else {
			double patternBias = itemSet.size() * this.biasWeight;
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

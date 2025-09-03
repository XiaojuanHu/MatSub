package edu.uab.consapt.sampling;

import edu.uab.cftp.sampling.distribution.GeneralDistribution.SupportMeasure;

public class DistributionFactor {
	final DataPortion dataPortion;
	private final SupportMeasure supportMeasure;

	public DistributionFactor(DataPortion dataPortion,
			SupportMeasure supportMeasure) {
		this.dataPortion = dataPortion;
		this.supportMeasure = supportMeasure;
	}

	public DataPortion getDataPortion() {
		return this.dataPortion;
	}

	public SupportMeasure getSupportMeasure() {
		return this.supportMeasure;
	}

	@Override
	public String toString() {
		return "QFunction[" + dataPortion + ", " + supportMeasure + "]";
	}
}
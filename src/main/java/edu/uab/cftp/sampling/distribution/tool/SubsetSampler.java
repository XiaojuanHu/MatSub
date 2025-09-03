package edu.uab.cftp.sampling.distribution.tool;

import ua.ac.be.mime.mining.TidList;

public interface SubsetSampler {

	public int[] drawSubset(TidList posIntersection,
			TidList[] negativeTransactions);
}

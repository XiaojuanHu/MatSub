package edu.uab.consapt.sampling.cftp;

import java.util.List;

import ua.ac.be.mime.plain.PlainTransaction;

public interface PotentialsComputer {

	public double[] getPotentials(List<PlainTransaction> transactions);

}

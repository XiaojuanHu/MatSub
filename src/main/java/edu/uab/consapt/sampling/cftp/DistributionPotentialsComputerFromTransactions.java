package edu.uab.consapt.sampling.cftp;

import java.util.Collection;
import java.util.List;

import edu.uab.consapt.sampling.PotentialFunction;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainTransaction;

public class DistributionPotentialsComputerFromTransactions implements
		PotentialsComputer {

	private final PotentialFunction<Collection<PlainItem>> potentialFunction;

	public DistributionPotentialsComputerFromTransactions(
			PotentialFunction<Collection<PlainItem>> potentialFunction) {
		this.potentialFunction = potentialFunction;
	}

	@Override
	public double[] getPotentials(List<PlainTransaction> transactions) {
		double[] potentials = new double[transactions.size()];

		int i = 0;
		for (PlainTransaction transaction : transactions) {
			potentials[i++] = potentialFunction.getPotential(transaction
					.getItems());
		}

		return potentials;
	}
}

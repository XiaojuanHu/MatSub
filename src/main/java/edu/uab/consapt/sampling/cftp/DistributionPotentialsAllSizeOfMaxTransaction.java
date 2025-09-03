package edu.uab.consapt.sampling.cftp;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import edu.uab.consapt.sampling.PotentialFunction;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainTransaction;

public class DistributionPotentialsAllSizeOfMaxTransaction implements
		PotentialsComputer {

	private final PotentialFunction<Collection<PlainItem>> potentialFunction;
	private final double potentialAllItems;
	private final double max;
	private double maxT;

	public DistributionPotentialsAllSizeOfMaxTransaction(
			PotentialFunction<Collection<PlainItem>> potentialFunction,
			Iterable<PlainItem> allItems, List<PlainTransaction> transactions) {
		this.potentialFunction = potentialFunction;
		this.potentialAllItems = potentialFunction
				.getPotential(newArrayList(allItems));
		this.max = getMax(transactions);
	}

	private double getMax(List<PlainTransaction> transactions) {
		double max = -1;
		for (PlainTransaction transaction : transactions) {
			max = Math.max(max,
					potentialFunction.getPotential(transaction.getItems()));
			maxT = Math.max(maxT, transaction.size());
		}
		return max;
	}

	@Override
	public double[] getPotentials(List<PlainTransaction> transactions) {
		double[] potentials = new double[transactions.size()];

		int i = 0;
		for (PlainTransaction transaction : transactions) {
			potentials[i++] = maxT;
		}

		return potentials;
	}
}

package ua.ac.be.mime.plain.weighting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uab.cftp.sampling.distribution.TwoStepSamplingDistribution;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemDB;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;

/**
 * Some utility functions for sampling procedures
 * 
 * @author Sandy Moens
 */

public class Utils {

	/**
	 * Draws a uniform subset of over an iterable of plain items. Each item is
	 * accepted with a probability 1/2
	 * 
	 * @param iterable
	 *            an iterable object of plain items
	 * @return a uniform subset of the plain items
	 */
	public static PlainItemSet drawUniformSubSet(Iterable<PlainItem> iterable) {
		PlainItemSet itemSet = new PlainItemSet();

		for (PlainItem item : iterable) {
			if (TwoStepSamplingDistribution.random.nextBoolean()) {
				itemSet.add(item);
			}
		}

		return itemSet;
	}

	public static PlainItemSet drawUniformSubSet(PlainItemDB itemDB,
			TidList transaction) {
		PlainItemSet itemSet = new PlainItemSet();

		int index = -1;
		while ((index = transaction.nextSetBit(index + 1)) != -1) {
			if (TwoStepSamplingDistribution.random.nextBoolean()) {
				itemSet.add(itemDB.get(index));
			}
		}

		return itemSet;
	}

	/**
	 * Draws a non-empty uniform subset over an iterable of plain items.
	 * 
	 * @param iterable
	 *            an iterable object of plain items
	 * @return a uniform subset of the plain items
	 */
	public static PlainItemSet drawUniformSubSetNoSingletons(
			Iterable<PlainItem> iterable) {
		PlainItemSet itemSet;

		do {
			itemSet = drawUniformSubSet(iterable);
		} while (itemSet.size() <= 1);

		return itemSet;
	}

	public static PlainItemSet drawUniformSubSetNoSingletons(
			PlainItemDB itemDB, TidList transaction) {
		PlainItemSet itemSet;

		if (transaction.cardinality() == 2) {
			itemSet = new PlainItemSet();
			int i;
			itemSet.add(itemDB.get(i = transaction.nextSetBit(0)));
			itemSet.add(itemDB.get(transaction.nextSetBit(i + 1)));
			return itemSet;
		}

		do {
			itemSet = drawUniformSubSet(itemDB, transaction);
		} while (itemSet.size() <= 1);

		return itemSet;
	}

	public static PlainItemSet drawUniformSubSetNoEmpty(PlainItemDB itemDB,
			TidList transaction) {
		PlainItemSet itemSet;

		do {
			itemSet = drawUniformSubSet(itemDB, transaction);
		} while (itemSet.size() == 0);

		return itemSet;
	}

	public static PlainItemSet drawDiscriminativeSubSetNoSingletons(
			PlainItemDB itemDB, TidList intersection, TidList difference) {
		PlainItemSet itemSet;

		do {
			PlainItemSet drawUniformSubSetNoEmpty = Utils
					.drawUniformSubSetNoEmpty(itemDB, difference);
			PlainItemSet drawUniformSubSet = Utils.drawUniformSubSet(itemDB,
					intersection);

			itemSet = drawUniformSubSetNoEmpty.union(drawUniformSubSet);
		} while (itemSet.size() <= 1);
		return itemSet;
	}

	public static PlainItemSet drawMultWeightedSubSet(
			Iterable<PlainItem> iterable) {
		PlainItemSet itemSet = new PlainItemSet();
		double alfa;

		for (PlainItem item : iterable) {
			alfa = 1.0 / 3.0;
			if (TwoStepSamplingDistribution.random.nextDouble() < alfa) {
				itemSet.add(item);
			}
		}

		return itemSet;
	}

	/**
	 * Draws a subset over an iterable of plain items using multiplicative item
	 * weights. Each item is accepted with probability weight(e)/(1+weight(e))
	 * 
	 * @param iterable
	 *            an iterable object of plain items
	 * @param weights
	 *            the weights of the individual items
	 * @return a weighted subset of the plain items
	 */
	public static PlainItemSet drawMultWeightedSubSet(
			Iterable<PlainItem> iterable, double[] weights) {
		PlainItemSet itemSet = new PlainItemSet();
		double alfa;

		int i = 0;
		for (PlainItem item : iterable) {
			alfa = weights[i] / (1.0 + weights[i]);
			if (TwoStepSamplingDistribution.random.nextDouble() < alfa) {
				itemSet.add(item);
			}
			i++;
		}

		return itemSet;
	}

	public static PlainItemSet drawMultWeightedSubSetNoSingletons(
			Iterable<PlainItem> iterable) {
		PlainItemSet itemSet;

		do {
			itemSet = drawMultWeightedSubSet(iterable);
		} while (itemSet.size() <= 1);

		return itemSet;
	}

	public static PlainItemSet drawMultWeightedSubSet(PlainItemDB itemDB,
			TidList tids, double[] weights) {
		PlainItemSet itemSet = new PlainItemSet();
		double alfa;

		int i = 0, index = -1;
		while ((index = tids.nextSetBit(index + 1)) != -1) {
			alfa = weights[i] / (1.0 + weights[i]);
			if (TwoStepSamplingDistribution.random.nextDouble() < alfa) {
				itemSet.add(itemDB.get(index));
			}
			i++;
		}

		return itemSet;
	}

	public static PlainItemSet drawMultWeightedSubSetNoSingletons(
			PlainItemDB itemDB, TidList tids, double[] weights) {
		PlainItemSet itemSet;

		if (tids.cardinality() == 2) {
			itemSet = new PlainItemSet();
			int i;
			itemSet.add(itemDB.get(i = tids.nextSetBit(0)));
			itemSet.add(itemDB.get(tids.nextSetBit(i + 1)));
			return itemSet;
		}

		do {
			itemSet = drawMultWeightedSubSet(itemDB, tids, weights);
		} while (itemSet.size() <= 1);

		return itemSet;
	}

	/**
	 * Draws a non-empty subset over an iterable of plain items using
	 * multiplicative item weights. Each item is accepted with probability
	 * weight(e)/(1+weight(e))
	 * 
	 * @param iterable
	 *            an iterable object of plain items
	 * @param weights
	 *            the weights of the individual items
	 * @return a weighted subset of the plain items
	 */
	public static PlainItemSet drawMultWeightedSubSetNoSingletons(
			Iterable<PlainItem> iterable, double[] weights) {
		PlainItemSet itemSet;

		do {
			itemSet = drawMultWeightedSubSet(iterable, weights);
		} while (itemSet.size() <= 1);

		return itemSet;
	}

	public static float getWeight(Iterable<PlainItem> iterable) {
		float weight = 0;
		for (PlainItem item : iterable) {
			weight += ((WeightedItem) item).getWeight();
		}
		return weight;
	}

	public static double getMultSubSetWeight(Iterable<PlainItem> iterable) {
		double weight = 1;
		for (PlainItem item : iterable) {
			weight *= (1 + 1 / item.getTIDs().cardinality());
		}
		return weight;
	}

	/**
	 * Computes the multiplicative item set weight for a number of items
	 * 
	 * @param iterable
	 *            an iterable object of plain items
	 * @param weights
	 *            the weights of the individual items
	 * @return the multiplicative weight of a number of items
	 */
	public static double getMultSubSetWeight(Iterable<PlainItem> iterable,
			double[] weights) {
		double weight = 1;
		for (double w : weights) {
			weight *= (1.0 + w);
		}
		return weight;
	}

	/**
	 * Computes the multiplicative item set weight for a number of items without
	 * counting singletons and the empty set
	 * 
	 * @param iterable
	 *            an iterable object of plain items
	 * @param weights
	 *            the weights of the individual items
	 * @return the multiplicative weight of a number of items
	 */
	public static double getMultSubSetWeightNoSingletons(
			Iterable<PlainItem> iterable, double[] weights) {
		double weight = 1;
		double singleTonsSum = 0;
		for (double w : weights) {
			weight *= (1.0 + w);
			singleTonsSum += w;
		}
		return weight - singleTonsSum - 1;
	}

	public static double getMultSubSetWeightNoSingletons(TidList t,
			double[] weights) {
		double weight = 1, w;
		double singleTonsSum = 0;
		int index = -1;
		while ((index = t.nextSetBit(index + 1)) != -1) {
			weight *= (1.0 + (w = weights[index - 1]));
			singleTonsSum += w;
		}
		return weight - singleTonsSum - 1;
	}

	public static double getMultSubSetWeight(double[] weights) {
		double weight = 1;
		for (double w : weights) {
			weight *= (1.0 + w);
		}
		return weight;
	}

	public static double getMultSubSetWeightNoSingletons(double[] weights) {
		double weight = 1;
		double singleTonsSum = 0;
		for (double w : weights) {
			weight *= (1.0 + w);
			singleTonsSum += w;
		}
		return weight - singleTonsSum - 1;
	}

	public static TidList intersectAll(List<PlainTransaction> transactions) {
		if (transactions.size() == 1) {
			return transactions.get(0).getItemsAsBitSet();
		}
		Iterator<PlainTransaction> it = transactions.iterator();
		TidList intersection = TidList.intersect(it.next().getItemsAsBitSet(),
				it.next().getItemsAsBitSet());
		while (it.hasNext()) {
			intersection.and(it.next().getItemsAsBitSet());
		}
		return intersection;
	}

	public static List<PlainTransaction> getTransactions(
			List<PlainTransaction> transactions, int[] indices) {
		List<PlainTransaction> uniqueTransactions = new ArrayList<PlainTransaction>(
				indices.length);
		for (Integer i : indices) {
			uniqueTransactions.add(transactions.get(i));
		}
		return uniqueTransactions;
	}

	public static PlainItemSet convertToPlainItemSet(
			Iterable<PlainItem> iterable) {
		PlainItemSet itemSet = new PlainItemSet();
		for (PlainItem item : iterable) {
			itemSet.add(item);
		}
		return itemSet;
	}

	public static double getWeight(TidList tids, int i) {
		return (tids.cardinality() + 1) * 0.5;
	}

	public static double getWeight(TidList tids) {
		return tids.cardinality() * 0.5;
	}

	private static Random random = new Random(System.currentTimeMillis());

	public static PlainItemSet drawSequentialSubsetNoSingletons(
			PlainItemDB itemDB, TidList tids) {
		PlainItemSet itemSet;

		do {
			itemSet = drawSequentialSubset(itemDB, tids);
		} while (itemSet.size() <= 1);

		return itemSet;
	}

	public static PlainItemSet drawSequentialSubset(PlainItemDB itemDB,
			TidList tids) {
		TidList P = new TidList();
		TidList N = new TidList();

		double alfa, tmp;
		int index = -1;
		while ((index = tids.nextSetBit(index + 1)) != -1) {
			tmp = getWeight(TidList.difference(tids, N));
			alfa = tmp + getWeight(P, index);
			alfa /= 2 * (tmp + getWeight(P));
			if (random.nextDouble() < alfa) {
				P.set(index);
			} else {
				N.set(index);
			}
		}

		PlainItemSet itemSet = new PlainItemSet();
		index = -1;
		while ((index = P.nextSetBit(index + 1)) != -1) {
			itemSet.add(itemDB.get(index));
		}

		return itemSet;
	}

	public static PlainItemSet drawSubSetOfSizeK(PlainItemDB itemDB,
			TidList tids, int size) {
		TidList P = new TidList();
		TidList N = new TidList();

		Random random = new Random(System.currentTimeMillis());

		double alfa;
		int itemsLeft = tids.cardinality();
		int index = -1;
		while ((index = tids.nextSetBit(index + 1)) != -1) {
			if (P.cardinality() == size) {
				break;
			}
			if (P.cardinality() + itemsLeft == size) {
				P.set(index);
				continue;
			}
			alfa = getWeight(P, index)
					+ (((double) (size - P.cardinality() - 1) / (double) (itemsLeft - 1)) * getWeight(
							TidList.union(tids, N), index));
			alfa /= ((itemsLeft) / (double) (size - P.cardinality()) * getWeight(P))
					+ getWeight(TidList.difference(tids, TidList.union(P, N)));
			if (random.nextDouble() < alfa) {
				P.set(index);
			} else {
				N.set(index);
			}
			itemsLeft--;
		}

		PlainItemSet itemSet = new PlainItemSet();
		index = -1;
		while ((index = P.nextSetBit(index + 1)) != -1) {
			itemSet.add(itemDB.get(index));
		}

		return itemSet;
	}
}

package edu.uab.cftp.sampling;

import edu.uab.cftp.sampling.distribution.DistributionFamilyArea;
import edu.uab.cftp.sampling.distribution.DistributionFamilyDiscriminativity;
import edu.uab.cftp.sampling.distribution.DistributionFamilyFrequency;
import edu.uab.cftp.sampling.distribution.DistributionFamilyRare;
import edu.uab.cftp.sampling.distribution.DistributionFamilyWeightedFrequency;
import edu.uab.cftp.sampling.distribution.StateSpaceSamplingDistribution;
import edu.uab.cftp.sampling.distribution.old.NegFrequencyDiscriminativityDistribution;
import ua.ac.be.mime.errorhandling.IncorrectParameterException;
import ua.ac.be.mime.plain.TransactionDBInterface;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;
import ua.ac.be.mime.plain.weighting.WeightedTransactionDBInterface;

public class DistributionFactory {

	private static NextStateProposer newNextStateProposer(int d,
			TransactionDBInterface db) {
		NextStateProposer nextStateProposer;
		if (db.isRectangular() && (isFrequencyFamily(d) || isAreaFamily(d))) {
			nextStateProposer = new UniformNextStateProposer();
		} else if (isDiscriminavityFamily(d)) {
			nextStateProposer = new WeightBasedLabeledNextStateProposer();
		} else {
			nextStateProposer = new WeightBasedNextStateProposer();
		}
		return nextStateProposer;
	}

	public static StateSpaceSamplingDistribution newFamilyDistribution(int d,
			TransactionDBInterface db) throws IncorrectParameterException {

		NextStateProposer nsp = newNextStateProposer(d, db);

		StateSpaceSamplingDistribution dist;
		if (isFrequencyFamily(d)) {
			dist = new DistributionFamilyFrequency(db, nsp, (d - 1) % 10);
		} else if (isWeightedFrequencyFamily(d)) {
			dist = new DistributionFamilyWeightedFrequency(
					(WeightedTransactionDBInterface) db, nsp, (d - 1) % 10);
		} else if (isAreaFamily(d)) {
			dist = new DistributionFamilyArea(db, nsp, (d - 1) % 10);
		} else if (isRareFamily(d)) {
			dist = new DistributionFamilyRare(db, nsp, (d - 1) % 10);
		} else if (isDiscriminavityFamily(d)) {
			dist = getDiscriminativityFamilyDistribuion(d, db, nsp);
		} else {
			throw new IncorrectParameterException(d + "", "distribution", "-m");
		}
		return dist;
	}

	private static StateSpaceSamplingDistribution getDiscriminativityFamilyDistribuion(
			int d, TransactionDBInterface db, NextStateProposer nsp)
			throws IncorrectParameterException {
		if (d == 42) {
			return new DistributionFamilyDiscriminativity(
					(PosNegDbInterface) db, (LabeledNextStateProposer) nsp, 0,
					0);
		} else if (d == 431) {
			return new DistributionFamilyDiscriminativity(
					(PosNegDbInterface) db, (LabeledNextStateProposer) nsp, 1,
					0);
		} else if (d == 432) {
			return new DistributionFamilyDiscriminativity(
					(PosNegDbInterface) db, (LabeledNextStateProposer) nsp, 0,
					1);
		} else if (d == 433) {
			return new NegFrequencyDiscriminativityDistribution(
					(PosNegDbInterface) db, (LabeledNextStateProposer) nsp);
		} else if (d == 441) {
			return new DistributionFamilyDiscriminativity(
					(PosNegDbInterface) db, (LabeledNextStateProposer) nsp, 2,
					0);
		} else if (d == 442) {
			return new DistributionFamilyDiscriminativity(
					(PosNegDbInterface) db, (LabeledNextStateProposer) nsp, 0,
					2);
		}
		throw new IncorrectParameterException(d + "", "distribution", "-m");
	}

	public static boolean isFrequencyFamily(int d) {
		return d > 10 && d < 20;
	}

	public static boolean isWeightedFrequencyFamily(int d) {
		return d > 100 && d < 110;
	}

	public static boolean isAreaFamily(int d) {
		return d > 20 && d < 30;
	}

	public static boolean isRareFamily(int d) {
		return d > 30 && d < 40;
	}

	public static boolean isDiscriminavityFamily(int d) {
		String dString = String.valueOf(d);
		return dString.length() >= 2 && dString.charAt(0) == '4';
	}

}

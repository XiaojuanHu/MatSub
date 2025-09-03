package edu.uab.consapt.sampling.cftp;

import static com.google.common.collect.Lists.newLinkedList;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import edu.uab.consapt.sampling.AbstractDistribution;
import edu.uab.consapt.sampling.PotentialFunction;
import edu.uab.consapt.sampling.StoppableSampler;
import edu.uab.consapt.sampling.cftp.tool.BlockSizeComputer;
import edu.uab.consapt.sampling.cftp.tool.DoublingBlockSizeComputer;

public class CFTPSampler<T> implements StoppableSampler<T> {

	public static double randomMultiplier = 10;
	public static double randomStart = 2012;

	public static long nextRandomSeed() {
		return (long) ((randomMultiplier++ * System.currentTimeMillis()) % Long.MAX_VALUE);
	}

	private static class RunSpecification {
		long randomSeed;
		long stepsCount;

		public RunSpecification(long randomSeed, long stepsCount) {
			this.randomSeed = randomSeed;
			this.stepsCount = stepsCount;
		}
	}

	private final BlockSizeComputer sizeComputer;

	private final AbstractDistribution<T> proposalDistribution;
	private final PotentialFunction<T> targetPotential;

	private T curState;
	private double curValue;
	private boolean isAccepted;

	private boolean isStop;

	public CFTPSampler(AbstractDistribution<T> proposalDistribution,
			PotentialFunction<T> potentialFunction) {
		this.sizeComputer = new DoublingBlockSizeComputer();

		this.proposalDistribution = proposalDistribution;
		this.targetPotential = potentialFunction;

		reset();
	}

	@Override
	public T getNext() {
		reset();
		int t = 2;
		int s;
		long randomSeed;

		LinkedList<RunSpecification> randomSeeds = newLinkedList();
		randomSeeds.push(new RunSpecification(nextRandomSeed(), 2));
		do {
			t += s = this.sizeComputer.getBlockSizeIncrease(t == 0 ? 2 : t);
			randomSeed = nextRandomSeed();
			randomSeeds.addFirst(new RunSpecification(randomSeed, s));
			runForTimeWithRandomSeed(s, randomSeed);
		} while (!isAccepted && !isStop);
		if (isStop) {
			return null;
		}
		runWithRandomSeeds(1, randomSeeds);
		return curState;
	}

	private void reset() {
		this.curState = null;
		this.curValue = 1;
		this.isAccepted = false;
	}

	private double getPotential(T t) {
		return targetPotential.getPotential(t)
				/ proposalDistribution.getPotential(t);
	}

	private void runForTimeWithRandomSeed(long time, long randomSeed) {
		T newState;
		Random random = new Random(randomSeed);
		for (int i = 0; i < time; i++) {
			if (isStop) {
				return;
			}
			newState = proposalDistribution.getNext(random);
			double newValue = getPotential(newState);

			if (random.nextDouble() < newValue / this.curValue) {
				this.curState = newState;
				this.curValue = newValue;
				this.isAccepted = true;
			}
		}
	}

	private void runWithRandomSeeds(int beg,
			List<RunSpecification> runSpecifications) {
		Iterator<RunSpecification> it = runSpecifications.listIterator(beg);
		RunSpecification runSpecification;
		while (it.hasNext()) {
			if (isStop) {
				return;
			}
			runSpecification = it.next();
			runForTimeWithRandomSeed(runSpecification.stepsCount,
					runSpecification.randomSeed);
		}
	}

	@Override
	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}
}

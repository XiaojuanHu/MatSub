package edu.uab.cftp.sampling;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;

import edu.uab.cftp.sampling.SamplingAlgorithmFactory.MetropolisFactory;
import edu.uab.cftp.sampling.SamplingAlgorithmFactory.MetropolisHastingsFactory;
import edu.uab.cftp.sampling.distribution.OrderedBaseObject;
import edu.uab.cftp.sampling.distribution.State;
import edu.uab.cftp.sampling.distribution.StateSpaceSamplingDistribution;
import edu.uab.cftp.sampling.distribution.TwoStepSamplingDistribution;
import edu.uab.consapt.sampling.cftp.tool.BlockSizeComputer;
import edu.uab.consapt.sampling.cftp.tool.DoublingBlockSizeComputer;
import ua.ac.be.mime.errorhandling.SupportRemovedException;
import ua.ac.be.mime.mining.ResultDataStructure;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.tool.DebugPrinter;
import ua.ac.be.mime.tool.ProgressPrinter;
import ua.ac.be.mime.tool.ProgressPrinter.AllStepsProgressPrinter;
import ua.ac.be.mime.tool.ProgressPrinter.NoProgressPrinter;
import ua.ac.be.mime.tool.ProgressPrinter.StepsProgressPrinter;

/**
 * Implements the coupling from the past algorithm using an underlying
 * interruptible perfect sampling algorithm and computer that gives new block
 * sizes
 * 
 * @author Sandy Moens
 */

public class CouplingFromThePast {

	public static double randomMultiplier = 10;
	public static double randomStart = 2012;

	private static boolean printAllSteps = false;

	public static long nextRandomSeed() {
		// return (long) ((randomMultiplier++ * randomStart++) %
		// Long.MAX_VALUE);
		return (long) ((randomMultiplier++ * System.currentTimeMillis()) % Long.MAX_VALUE);
	}

	private boolean stop;

	private final InterruptiblePerfectSamplingAlgorithm algorithm;
	private final StateSpaceSamplingDistribution distribution;
	private final StateSpaceSamplingDistribution testDistribution;
	private final BlockSizeComputer blockSizeComputer;
	private Logger logger;

	public CouplingFromThePast(InterruptiblePerfectSamplingAlgorithm algorithm,
			BlockSizeComputer blockSizeComputer,
			StateSpaceSamplingDistribution distribution,
			StateSpaceSamplingDistribution testDistribution) {
		this.algorithm = algorithm;
		this.blockSizeComputer = blockSizeComputer;
		this.distribution = distribution;
		this.testDistribution = testDistribution;
	}

	public CouplingFromThePast(InterruptiblePerfectSamplingAlgorithm algorithm,
			BlockSizeComputer blockSizeComputer,
			StateSpaceSamplingDistribution distribution) {
		this(algorithm, blockSizeComputer, distribution, null);
	}

	public void stop() {
		this.stop = true;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public State runOnce(Logger logger) throws InterruptedException {
		algorithm.reset();
		int t = 2;
		int s;
		long randomSeed;
		LinkedList<long[]> randomSeeds = new LinkedList<long[]>();
		randomSeeds.push(new long[] { 2, nextRandomSeed() });
		do {
			s = this.blockSizeComputer.getBlockSizeIncrease(t == 0 ? 2 : t);
			t += s;
			randomSeeds
					.addFirst(new long[] { s, randomSeed = nextRandomSeed() });
			Thread.sleep(0);
		} while (algorithm.run(s, randomSeed) == null);
		if (logger != null) {
			logger.addTimeStep((3 * t / 2) + 2);
		}
		Thread.sleep(0);
		return algorithm.run(1, randomSeeds);
	}

	public State runOnce() throws InterruptedException {
		algorithm.reset();
		int t = 2;
		int s;
		long randomSeed;
		LinkedList<long[]> randomSeeds = new LinkedList<long[]>();
		randomSeeds.push(new long[] { 2, nextRandomSeed() });
		do {
			s = this.blockSizeComputer.getBlockSizeIncrease(t == 0 ? 2 : t);
			t += s;
			randomSeeds
					.addFirst(new long[] { s, randomSeed = nextRandomSeed() });
			Thread.sleep(0);
		} while (algorithm.run(s, randomSeed) == null);
		Thread.sleep(0);
		return algorithm.run(1, randomSeeds);
	}

	public void runIndependentTrials(String fileName, int numberOfSamples) {
		try {
			PrintStream p = new PrintStream(fileName);
			ProgressPrinter pp = createProgressPrinter(numberOfSamples);
			pp.restart();
			for (int i = 0; i < numberOfSamples; i++) {
				PlainItemSet itemSet = ((TwoStepSamplingDistribution) this.distribution)
						.drawSubSet(runOnce().getBaseObject());
				for (PlainItem item : itemSet) {
					String name = item.getName();
					if (name != null) {
						p.print(item.getName() + " ");
					} else {
						p.print(item.getId() + " ");
					}
				}
				p.println();
				p.flush();
				pp.addStepsAndPrint(1);
			}
			pp.printCompletion();
			p.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private ProgressPrinter createProgressPrinter(int numberOfSamples) {
		if (DebugPrinter.verbose) {
			return new StepsProgressPrinter(numberOfSamples);
		}
		return new NoProgressPrinter();
	}

	public void runIndependentTrials(ResultDataStructure data,
			int numberOfSamples) {
		stop = false;
		try {
			ProgressPrinter pp = createProgressPrinter(numberOfSamples);
			pp.restart();
			for (int i = 0; i < numberOfSamples; i++) {
				if (stop) {
					return;
				}
				OrderedBaseObject baseObject = runOnce().getBaseObject();
				if (stop) {
					return;
				}
				((TwoStepSamplingDistribution) this.distribution).drawSubSet(
						baseObject, data);
				pp.addStepsAndPrint(1);
				Thread.sleep(0);
			}
			pp.printCompletion();
		} catch (InterruptedException e) {
			return;
		}
	}

	public void runIndependentTrials(ResultDataStructure data) {
		stop = false;
		while (true) {
			try {
				if (stop) {
					return;
				}
				OrderedBaseObject baseObject = runOnce().getBaseObject();
				if (stop) {
					return;
				}
				((TwoStepSamplingDistribution) this.distribution).drawSubSet(
						baseObject, data);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	public void testSeparateNoLog(int numberOfSamples) {
		try {
			ProgressPrinter pp = printAllSteps ? new AllStepsProgressPrinter()
					: new StepsProgressPrinter(numberOfSamples);

			for (int i = 0; i < numberOfSamples; i++) {
				runOnce(null);
				pp.addStepsAndPrint(1);
			}
			pp.printCompletion();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Logger testSeparate(String fileName, int numberOfSamples,
			boolean drawDistribution, boolean... add) {
		Logger logger;
		if (this.logger == null) {
			logger = new Logger(this.distribution, this.testDistribution,
					numberOfSamples);
		} else {
			logger = this.logger;
		}
		logger.setDrawDistribution(drawDistribution);
		logger.start();

		try {
			ProgressPrinter pp = printAllSteps ? new AllStepsProgressPrinter()
					: new StepsProgressPrinter(numberOfSamples);

			for (int i = 0; i < numberOfSamples; i++) {
				if (add.length != 0 && add[0]) {
					logger.increment(runOnce(logger).getBaseObject());
				} else {
					runOnce(logger);
				}
				pp.addStepsAndPrint(1);
			}
			pp.printCompletion();

			logger.stop();
			logger.printInfo(fileName + "-separate-n" + numberOfSamples
					+ ".txt");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return logger;
	}

	public void testTogether(String filename, int numberOfSamples,
			boolean drawDistribution) throws SupportRemovedException {
		throw new SupportRemovedException("CouplingFromThePast::testTogether()");
	}

	public static void runCouplingFromThePast(String outputfile,
			int numberOfSamples, StateSpaceSamplingDistribution distribution,
			int nextStateProposer, int chainMethod, int approximation,
			int verbose) {
		// throws SupportRemovedException {
		DebugPrinter.verbose = verbose == 1 ? false : true;

		NextStateProposer n = null;
		if (nextStateProposer == 1) {
			// throw new
			// SupportRemovedException("UniformSparseNextStateProposer.class");
		} else if (nextStateProposer == 2) {
			// throw new
			// SupportRemovedException("UniformFullNextStateProposer.class");
		} else if (nextStateProposer == 3) {
			// throw new
			// SupportRemovedException("SizeBasedNextStateProposer.class");
		}
		distribution.setNextStateProposer(n);

		SamplingAlgorithmFactory factory = null;
		if (chainMethod == 1) {
			factory = new MetropolisFactory();
		} else if (chainMethod == 2) {
			factory = new MetropolisHastingsFactory();
		} else if (chainMethod == 3) {
			// throw new
			// SupportRemovedException("SmartMetropolisAlgorithm.class");
		} else if (chainMethod == 4) {
			// throw new
			// SupportRemovedException("MetropolisTestAlgorithm.class");
		}

		CouplingFromThePast cftp = new CouplingFromThePast(
				factory.createAlgorithm(distribution),
				new DoublingBlockSizeComputer(), distribution);
		if (approximation == 1) {
			cftp.runIndependentTrials(outputfile, numberOfSamples);
		} else if (approximation == 2) {
			// throw new
			// SupportRemovedException("CouplingFromThePast::runTogether()");
		}
	}

}

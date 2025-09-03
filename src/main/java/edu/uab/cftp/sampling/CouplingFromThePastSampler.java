package edu.uab.cftp.sampling;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.uab.cftp.sampling.SamplingAlgorithmFactory.MetropolisHastingsFactory;
import edu.uab.cftp.sampling.distribution.StateSpaceSamplingDistribution;
import edu.uab.consapt.sampling.cftp.tool.DoublingBlockSizeComputer;
import ua.ac.be.mime.mining.ResultDataStructure;
import ua.ac.be.mime.mining.ResultDataStructure.DataStructureFactory;
import ua.ac.be.mime.tool.DebugPrinter;

public class CouplingFromThePastSampler {

	private final class CFTPCallable implements Callable<ResultDataStructure> {

		private CouplingFromThePast cftp;
		private ResultDataStructure result;
		private int numberOfSamples;

		public CFTPCallable(
				InterruptiblePerfectSamplingAlgorithm createAlgorithm,
				StateSpaceSamplingDistribution distribution,
				ResultDataStructure result, int numberOfSamples) {
			cftp = new CouplingFromThePast(
					mcmcFactory.createAlgorithm(distribution),
					new DoublingBlockSizeComputer(), distribution);
			this.result = result;
			this.numberOfSamples = numberOfSamples;
		}

		public void stop() {
			cftp.stop();
		}

		@Override
		public ResultDataStructure call() {
			if (numberOfSamples == -1) {
				cftp.runIndependentTrials(result);
			} else {
				cftp.runIndependentTrials(result, numberOfSamples);
			}
			return result;
		}
	}

	private DataStructureFactory dataFactory = new ResultDataStructure.PlainListDataFactory();
	private SamplingAlgorithmFactory mcmcFactory = new MetropolisHastingsFactory();
	private StateSpaceSamplingDistribution distribution;

	private ResultDataStructure lastMineResults;

	private CouplingFromThePast cftp = null;

	public CouplingFromThePastSampler(
			StateSpaceSamplingDistribution distribution) {
		this.distribution = distribution;
		this.cftp = null;
	}

	public void setVerbose(boolean verbose) {
		DebugPrinter.verbose = verbose;
	}

	public void setResultDataStructureFactory(DataStructureFactory factory) {
		this.dataFactory = factory;
	}

	public void setMcmcFactory(SamplingAlgorithmFactory mcmcFactory) {
		this.mcmcFactory = mcmcFactory;
		this.cftp = null;
	}

	public ResultDataStructure runOnce() {
		if (cftp == null) {
			cftp = new CouplingFromThePast(
					mcmcFactory.createAlgorithm(distribution),
					new DoublingBlockSizeComputer(), distribution);
		}
		lastMineResults = dataFactory.newDataStructure();
		cftp.runIndependentTrials(lastMineResults, 1);
		return lastMineResults;
	}

	public ResultDataStructure run(int numberOfSamples) {
		return run(numberOfSamples, -1, TimeUnit.SECONDS);
	}

	public ResultDataStructure run(int period, TimeUnit timeUnit) {
		return run(-1, period, timeUnit);
	}

	public ResultDataStructure run(int numberOfSamples, int period,
			TimeUnit timeUnit) {
		lastMineResults = dataFactory.newDataStructure();
		ExecutorService newSingleThreadExecutor = Executors
				.newSingleThreadExecutor();
		CFTPCallable task = new CFTPCallable(
				mcmcFactory.createAlgorithm(distribution), distribution,
				lastMineResults, numberOfSamples);
		Future<ResultDataStructure> fut = newSingleThreadExecutor.submit(task);
		try {
			if (period == -1) {
				fut.get();
			} else {
				fut.get(period, timeUnit);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			task.stop();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			fut.cancel(true);
		}
		newSingleThreadExecutor.shutdownNow();
		return lastMineResults;
	}

	public void run(int numberOfSamples, String outputFileName) {
		run(numberOfSamples);
		lastMineResults.writeToFile(outputFileName);
	}

	public ResultDataStructure getLastMineResults() {
		return lastMineResults;
	}
}
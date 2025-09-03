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
import ua.ac.be.mime.errorhandling.IncorrectParameterException;
import ua.ac.be.mime.mining.ResultDataStructure;
import ua.ac.be.mime.mining.ResultDataStructure.DataStructureFactory;
import ua.ac.be.mime.plain.TransactionDBInterface;
import ua.ac.be.mime.tool.DebugPrinter;

public class CouplingFromThePastSampler2 {

	private final class CFTPCallable implements Callable<ResultDataStructure> {

		private CouplingFromThePast cftp;
		private final ResultDataStructure result;
		private final TransactionDBInterface db;
		private final int distributionId;
		private final int nrSamples;
		private boolean stop;

		public CFTPCallable(SamplingAlgorithmFactory mcmcFactory,
				TransactionDBInterface db, int distributionId, int nrSamples,
				ResultDataStructure result) {
			this.db = db;
			this.distributionId = distributionId;
			this.nrSamples = nrSamples;
			this.result = result;
			this.stop = false;
		}

		public void stop() {
			if (cftp != null) {
				cftp.stop();
			}
			this.stop = true;
		}

		@Override
		public ResultDataStructure call() {
			if (stop) {
				return result;
			}
			try {
				StateSpaceSamplingDistribution distribution = DistributionFactory
						.newFamilyDistribution(distributionId, db);

				cftp = new CouplingFromThePast(
						mcmcFactory.createAlgorithm(distribution),
						new DoublingBlockSizeComputer(), distribution);
				cftp.runIndependentTrials(result, nrSamples);
			} catch (IncorrectParameterException e) {
				e.printStackTrace();
			}

			return result;
		}
	}

	private DataStructureFactory dataFactory = new ResultDataStructure.PlainListWithTimesDataFactory();
	private SamplingAlgorithmFactory mcmcFactory = new MetropolisHastingsFactory();

	private ResultDataStructure lastMineResults;

	private final TransactionDBInterface db;
	private final int distributionId;
	private final int nrSamples;

	public CouplingFromThePastSampler2(TransactionDBInterface db,
			int distributionId, int nrSamples) {
		this.db = db;
		this.distributionId = distributionId;
		this.nrSamples = nrSamples;
	}

	public void setVerbose(boolean verbose) {
		DebugPrinter.verbose = verbose;
	}

	public void setResultDataStructureFactory(DataStructureFactory factory) {
		this.dataFactory = factory;
	}

	public void setMcmcFactory(SamplingAlgorithmFactory mcmcFactory) {
		this.mcmcFactory = mcmcFactory;
	}

	public ResultDataStructure run(int period, TimeUnit timeUnit) {
		lastMineResults = dataFactory.newDataStructure();
		ExecutorService newSingleThreadExecutor = Executors
				.newSingleThreadExecutor();
		CFTPCallable task = new CFTPCallable(mcmcFactory, db, distributionId,
				nrSamples, lastMineResults);
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

	public ResultDataStructure getLastMineResults() {
		return lastMineResults;
	}
}
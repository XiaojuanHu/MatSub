package edu.uab.cftp.sampling;

import java.util.BitSet;
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
import ua.ac.be.mime.mining.ResultDataStructure.PlainList;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.PlainTransactionDB;
import ua.ac.be.mime.plain.TransactionDBInterface;
import ua.ac.be.mime.tool.DatabaseConditioner;
import ua.ac.be.mime.tool.DebugPrinter;

public class AdaptiveCouplingFromThePastSampler {

	private final class CFTPCallable implements Callable<ResultDataStructure> {

		private final ResultDataStructure result;
		private final int numberOfSamples;
		private final SamplingAlgorithmFactory mcmcFactory;
		private final TransactionDBInterface db;
		private boolean stop;

		public CFTPCallable(int d, TransactionDBInterface db,
				SamplingAlgorithmFactory mcmcFactory,
				ResultDataStructure result, int numberOfSamples) {
			this.db = db;
			this.mcmcFactory = mcmcFactory;
			this.result = result;
			this.numberOfSamples = numberOfSamples;
			stop = false;
		}

		public void stop() {
			if (cftp != null) {
				cftp.stop();
			}
			stop = true;
		}

		@Override
		public ResultDataStructure call() {
			if (stop) {
				return result;
			}
			PlainTransactionDB pdb = DatabaseConditioner.conditionDB(
					(PlainTransactionDB) db, new PlainItemSet(), new BitSet());
			do {
				try {
					StateSpaceSamplingDistribution distribution = DistributionFactory
							.newFamilyDistribution(d, pdb);
					cftp = new CouplingFromThePast(
							mcmcFactory.createAlgorithm(distribution),
							new DoublingBlockSizeComputer(), distribution);
					PlainList list = new ResultDataStructure.PlainList();
					cftp.runIndependentTrials(list, 1);
					if (stop) {
						return result;
					}
					PlainItemSet next = list.iterator().next();
					result.add(next);
					pdb = DatabaseConditioner.conditionDB(pdb, next,
							next.getTIDs());
					if (allTransOneOrLower(pdb)) {
						pdb = DatabaseConditioner.conditionDB(
								(PlainTransactionDB) db, new PlainItemSet(),
								new BitSet());
					}
				} catch (IncorrectParameterException e) {
					e.printStackTrace();
				}

			} while (numberOfSamples == -1 || result.size() < numberOfSamples);
			return result;
		}

		private boolean allTransOneOrLower(PlainTransactionDB pdb) {
			for (PlainTransaction t : pdb.getTransactions()) {
				if (t.size() > 1) {
					return false;
				}
			}
			return true;
		}
	}

	private DataStructureFactory dataFactory = new ResultDataStructure.PlainListDataFactory();
	private SamplingAlgorithmFactory mcmcFactory = new MetropolisHastingsFactory();

	private ResultDataStructure lastMineResults;

	private CouplingFromThePast cftp = null;

	private final int d;
	private final TransactionDBInterface db;

	public AdaptiveCouplingFromThePastSampler(int d, TransactionDBInterface db) {
		this.d = d;
		this.db = db;
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
		CFTPCallable task = new CFTPCallable(d, db, mcmcFactory,
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

	public static void main(String[] args) {
		PlainTransactionDB db = new PlainTransactionDB(
				"../../experiments/data/transactions/accidents/accidents.dat");
		System.out.println("Loading finished");
		AdaptiveCouplingFromThePastSampler a = new AdaptiveCouplingFromThePastSampler(
				12, db);
		a.run(25, 1, TimeUnit.SECONDS);
	}
}
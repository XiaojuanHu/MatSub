package edu.uab.cftp.sampling;

import static com.google.common.collect.Maps.newHashMap;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import edu.uab.cftp.sampling.distribution.OrderedBaseObject;
import edu.uab.cftp.sampling.distribution.StateSpaceSamplingDistribution;
import edu.uab.cftp.sampling.distribution.old.FrequencyDiscriminativityDistribution;
import edu.uab.cftp.sampling.distribution.old.PosFrequencyDiscriminativityDistribution;
import ua.ac.be.mime.plain.weighting.old.PosNegWeightedTransactionDb;
import ua.ac.be.mime.tool.DebugPrinter;
import ua.ac.be.mime.tool.Utilities;
import ua.ac.be.mime.tool.threading.StreamReader;
import ua.ac.be.statistics.Mean;
import ua.ac.be.statistics.StandardDeviation;
import ua.ac.be.viz.PlotterHeaderTypes;

public class Logger {

	private final StateSpaceSamplingDistribution distribution;
	private final StateSpaceSamplingDistribution testDistribution;
	public int timeStepCounter;
	public double[] timeSteps;
	private final Map<OrderedBaseObject, AtomicInteger> map;
	public long start;
	public long stop;

	private boolean drawDistribution;
	private boolean writeDistribution;

	public Logger(StateSpaceSamplingDistribution distribution,
			StateSpaceSamplingDistribution testDistribution,
			int numberOfTimeSteps) {
		this.distribution = distribution;
		this.testDistribution = testDistribution;
		this.timeSteps = new double[numberOfTimeSteps];
		this.map = newHashMap();
		this.drawDistribution = false;
		this.writeDistribution = false;
		restart();
	}

	public void setDrawDistribution(boolean drawDistribution) {
		this.drawDistribution = drawDistribution;
	}

	public void setWriteDistribution(boolean writeDistribution) {
		this.writeDistribution = writeDistribution;
	}

	public void restart() {
		this.timeStepCounter = 0;
		this.map.clear();
	}

	public void addTimeStep(int timeStep) {
		this.timeSteps[this.timeStepCounter++] = timeStep;
	}

	public void increment(OrderedBaseObject baseObject) {
		Object o = this.map.get(baseObject);
		if (o != null) {
			((AtomicInteger) o).incrementAndGet();
		} else {
			this.map.put(baseObject, new AtomicInteger(1));
		}
	}

	public Map<OrderedBaseObject, AtomicInteger> getMap() {
		return this.map;
	}

	public void start() {
		this.start = System.currentTimeMillis();
	}

	public void stop() {
		this.stop = System.currentTimeMillis();
	}

	private void drawDistribution(String filename) {
		try {
			writeToFile(filename);
			String cpSeparator = Utilities.isWindows() ? ";" : ":";
			String options = Utilities.isMacOSX() ? "-Xdock:name=viz " : "";
			Process proc = Runtime.getRuntime().exec(
					"java -Xmx1024m " + options + "-cp mime_lib/*"
							+ cpSeparator + "./mime_lib/Plotter.jar "
							+ "ua.ac.be.viz.Plotter " + filename);
			DebugPrinter.println(this, "Draw: " + filename);

			if (proc != null) {
				Thread ouputThread = new Thread(new StreamReader(
						proc.getInputStream()));
				ouputThread.start();

				Thread errorThread = new Thread(new StreamReader(
						proc.getErrorStream()));
				errorThread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private double[] minMax(double[] values) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_NORMAL;
		int i = 0;
		for (double value : values) {
			if (value < min && value != 0) {
				min = value;
			}
			if (value > max) {
				max = value;
			}
			i++;
			if (i == this.timeStepCounter) {
				break;
			}
		}
		return new double[] { min, max };
	}

	@SuppressWarnings("unused")
	private double totalVariationDistance() {
		double totalVariationDistance = 0.0;
		// OrderedBaseObject[] baseObjects =
		// this.testDistribution.getBaseObjects();
		// if (baseObjects == null) {
		int end = this.distribution.getDB().getTransactions().size();
		OrderedBaseObject baseObject;
		AtomicInteger ai;
		if (this.distribution.cardinality() == 2) {
			for (int i = 0; i < end; i++) {
				for (int j = i; j < end; j++) {
					baseObject = this.distribution.getNextStateProposer()
							.createBaseObject(new int[] { i, j });
					if ((ai = this.map.get(baseObject)) != null) {
						totalVariationDistance += Math
								.abs(((this.testDistribution
										.baseObjectWeight(baseObject) / this.testDistribution
										.getNormalizationFactor()))
										- ai.doubleValue()
										/ this.timeSteps.length) / 2.0;
					} else {
						totalVariationDistance += ((this.testDistribution
								.baseObjectWeight(baseObject) / this.testDistribution
								.getNormalizationFactor())) / 2.0;
					}
				}
			}
		} else if (this.distribution.cardinality() == 3) {
			int endI, endJ, endK;
			if (this.distribution instanceof PosFrequencyDiscriminativityDistribution) {
				endI = endJ = ((PosNegWeightedTransactionDb) this.distribution
						.getDB()).getTransactionsPos().size();
				endK = ((PosNegWeightedTransactionDb) this.distribution.getDB())
						.getTransactionsNeg().size();
			} else if (this.distribution instanceof FrequencyDiscriminativityDistribution) {
				endI = this.distribution.getDB().getTransactions().size();
				endJ = ((PosNegWeightedTransactionDb) this.distribution.getDB())
						.getTransactionsPos().size();
				endK = ((PosNegWeightedTransactionDb) this.distribution.getDB())
						.getTransactionsNeg().size();
			} else {
				endI = endJ = endK = end;
			}
			for (int i = 0; i < endI; i++) {
				for (int j = i; j < endJ; j++) {
					for (int k = j; k < endK; k++) {
						baseObject = new OrderedBaseObject(
								new int[] { i, j, k });
						if ((ai = this.map.get(baseObject)) != null) {
							totalVariationDistance += Math
									.abs(((this.testDistribution
											.baseObjectWeight(this.testDistribution
													.getNextStateProposer()
													.createBaseObject(
															baseObject
																	.getIndices())) / this.testDistribution
											.getNormalizationFactor()))
											- ai.doubleValue()
											/ this.timeSteps.length) / 2.0;
						} else {
							totalVariationDistance += ((this.testDistribution
									.baseObjectWeight(baseObject) / this.testDistribution
									.getNormalizationFactor())) / 2.0;
						}
					}
				}
			}
		} else {
			for (int i = 0; i < end; i++) {
				for (int j = i; j < end; j++) {
					for (int k = j; k < end; k++) {
						for (int l = k; l < end; l++) {
							baseObject = new OrderedBaseObject(new int[] { i,
									j, k, l });
							if ((ai = this.map.get(baseObject)) != null) {
								totalVariationDistance += Math
										.abs(((this.testDistribution
												.baseObjectWeight(this.testDistribution
														.getNextStateProposer()
														.createBaseObject(
																baseObject
																		.getIndices())) / this.testDistribution
												.getNormalizationFactor()))
												- ai.doubleValue()
												/ this.timeSteps.length) / 2.0;
							} else {
								totalVariationDistance += ((this.testDistribution
										.baseObjectWeight(baseObject) / this.testDistribution
										.getNormalizationFactor())) / 2.0;
							}
						}
					}
				}
			}
		}
		// } else {
		// AtomicInteger i;
		// for (OrderedBaseObject b : baseObjects) {
		// if ((i = this.map.get(b)) != null) {
		// totalVariationDistance += Math.abs(((this.testDistribution
		// .baseObjectWeight(b) / this.testDistribution
		// .normalizationFactor()))
		// - i.doubleValue() / this.timeSteps.length) / 2.0;
		// } else {
		// totalVariationDistance += ((this.testDistribution.baseObjectWeight(b)
		// /
		// this.testDistribution
		// .normalizationFactor())) / 2.0;
		// }
		// }
		// }
		return totalVariationDistance;
	}

	@SuppressWarnings("unused")
	private double totalVariationDistanceNoNulls() {
		double totalVariationDistance = 0.0;
		for (Entry<OrderedBaseObject, AtomicInteger> entry : this.map
				.entrySet()) {
			totalVariationDistance += Math.abs(((this.testDistribution
					.baseObjectWeight(entry.getKey()) / this.testDistribution
					.getNormalizationFactor()))
					- entry.getValue().doubleValue() / this.timeSteps.length) / 2;
		}
		return totalVariationDistance;
	}

	public void writeToFile(String filename) {
		BufferedWriter output;
		try {
			output = new BufferedWriter(new FileWriter(filename));
			output.write(PlotterHeaderTypes.TITLE.toString()
					+ "=Probability Function" + "\n");
			output.write(PlotterHeaderTypes.XAXISNAME.toString()
					+ "=Base Object\n");
			output.write(PlotterHeaderTypes.YAXISNAME.toString() + "=Weight\n");
			output.write(PlotterHeaderTypes.TYPE.toString() + "=line\n\n");
			// output.write(PlotterHeaderTypes.YRANGE.toString() + "=0.05\n\n");

			output.write("computed: ");

			Set<OrderedBaseObject> baseObjects = new TreeSet<OrderedBaseObject>(
					this.map.keySet());

			for (OrderedBaseObject b : baseObjects) {
				DebugPrinter.println(
						this,
						b
								+ " "
								+ this.testDistribution
										.baseObjectWeight(this.testDistribution
												.getNextStateProposer()
												.createBaseObject(
														b.getIndices())));
			}

			double d;
			double dd = 0;
			AtomicInteger i;
			// int c = 0;
			for (OrderedBaseObject b : baseObjects) {
				if ((i = this.map.get(b)) != null) {
					output.write((d = i.doubleValue() / this.timeSteps.length)
							+ " ");
					dd += d;
					// DebugPrinter
					// .println(this, "(" + c++ + ") " + b + "\t\t" +
					// b.getWeight() +
					// "\t\t" + i.doubleValue());
				} else {
					// output.write("0 ");
				}

			}

			DebugPrinter.println(this, "dd " + dd + "");

			output.write("\n");
			output.write("expected: ");

			dd = 0;
			for (OrderedBaseObject b : baseObjects) {
				if (this.map.get(b) != null) {
					output.write((d = this.testDistribution
							.baseObjectWeight(this.testDistribution
									.getNextStateProposer().createBaseObject(
											b.getIndices()))
							/ this.testDistribution.getNormalizationFactor())
							+ " ");
					dd += d;
				}
			}
			DebugPrinter.println(this, "dd " + dd);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printInfo(String filename) {
		try {
			PrintStream p = new PrintStream(filename + ".txt");
			DebugPrinter.println(this, "");
			DebugPrinter.println(this,
					"===============================================");
			printToFile(p, "Time taken: " + (this.stop - this.start));
			printToFile(
					p,
					"Number of Base Objects: "
							+ this.distribution.numberOfBaseObjects());
			printToFile(p, "Samples drawn: " + this.timeStepCounter);
			double[] minMax = minMax(this.timeSteps);
			printToFile(
					p,
					"Average number of steps: "
							+ Mean.evaluate(this.timeSteps, 0,
									this.timeStepCounter) + " minmax: ["
							+ minMax[0] + ";" + minMax[1] + "]");
			printToFile(
					p,
					"Standard deviation steps: "
							+ StandardDeviation.evaluate(this.timeSteps, 0,
									this.timeStepCounter));
			// double[] errors = halvedDifference();
			// minMax = minMax(errors);
			// System.out.println("Halved errors: " + sum(errors) + " minmax: ["
			// + minMax[0] + ";" + minMax[1] + "]");
			// System.out.println("Average halved error rate: " +
			// Mean.evaluate(errors));
			if (this.testDistribution != null) {
				// printToFile(p, "Total variation distance: " +
				// totalVariationDistance());
				// printToFile(p, "Total variation distance (no nulls): "
				// + totalVariationDistanceNoNulls());
			}
			DebugPrinter.println(this,
					"===============================================");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (this.drawDistribution) {
			drawDistribution(filename);
		} else if (this.writeDistribution) {
			writeToFile(filename);
		}
	}

	private static void printToFile(PrintStream p, String string) {
		p.println(string);
		DebugPrinter.println("Logger", string);
	}

	public double getAverageNumberOfTimeSteps() {
		return Mean.evaluate(this.timeSteps, 0, this.timeStepCounter);
	}

	public double[] getTimeSteps() {
		return this.timeSteps;
	}
}

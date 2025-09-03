package edu.uab.cftp.sampling;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static ua.ac.be.mime.tool.Utils.checkFileExists;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import edu.uab.cftp.sampling.SamplingAlgorithmFactory.MetropolisHastingsFactory;
import edu.uab.cftp.sampling.distribution.GeneralDistribution;
import edu.uab.cftp.sampling.distribution.GeneralDistribution.SupportMeasure;
import edu.uab.cftp.sampling.distribution.tool.StarOperation;
import edu.uab.consapt.sampling.DataPortion;
import ua.ac.be.mime.mining.ResultDataStructure;
import ua.ac.be.mime.plain.DatabaseFactory;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;
import ua.ac.be.mime.tool.DebugPrinter;

/**
 * This class runs the General Distribution using information from the config
 * file to build up the distribution, draw X samples from this distribution and
 * store them to an output file.
 * 
 * @author Sandy Moens
 */
public class GeneralDistributionRunner {
	private static String KEY_FILENAME = "filename";
	private static String KEY_SAMPLES = "samples";
	private static String KEY_BIAS = "bias";
	private static String KEY_STAR = "star";
	private static String KEY_LABELS = "labels";
	private static String KEY_QFUNCTION = "qfunction";
	private static String KEY_OUTPUT = "output";
	private static String KEY_LABELATTRIBUTE = "label";
	private static String KEY_POSLABELS = "pos";

	private static String VALUE_ADDITIVE = "additive";
	private static String VALUE_MULTIPLICATIVE = "multiplicative";
	private static String VALUE_POSITIVE = "positive";
	private static String VALUE_NEGATIVE = "negative";
	private static String VALUE_ALL = "all";
	private static String VALUE_TRUE = "true";
	private static String VALUE_FALSE = "false";

	private static String configFilename = "cftp.config";
	private static String weightsFilename = "biases.txt";

	private final Map<String, String> runProperties = newHashMap();
	private final List<String> dataPortions = newArrayList();
	private final List<String> supportFunctions = newArrayList();

	private String fileName = "";
	private int numberOfSamples = -1;
	private double bias = 1;
	private StarOperation star = StarOperation.MULTIPLICATIVE;
	private Map<Integer, Double> biases;
	private boolean biasesAreSet;
	private boolean keepLabels = false;
	private String outputFilename = "result.txt";

	private String labelAttribute;
	private String[] posLabels;

	private GeneralDistribution distribution;

	/**
	 * Performs the complete sampling operation, starting by reading in the
	 * config file and building the distribution. If everything is okay, the
	 * specified number of samples is drawn from the distribution and written to
	 * an output file.
	 * 
	 * @throws ImpossibleToSampleException
	 */
	public void perfomSamplingTask() throws FileNotFoundException,
			ImpossibleToSampleException {
		readConfigFile();
		if (!checkRunProperties()) {
			return;
		}
		readBiasesIfFileExists();

		initializeFields();
		printInfo();
		createDistribution();
		runDistribution();
	}

	private void readBiasesIfFileExists() {
		if (!checkFileExists(weightsFilename)) {
			DebugPrinter
					.println("No weights file, setting default weights only!");
			biasesAreSet = false;
			return;
		}
		biases = getBiasesFromFile();
		biasesAreSet = true;
	}

	private Map<Integer, Double> getBiasesFromFile() {
		return new WeightReader(weightsFilename, "=").getWeightMap();
	}

	/**
	 * Reads the contents from the config file if it exists. Lines that begin
	 * with a hash-tag are skipped. A config file contains key-value pairs
	 * separated by an equality sign. Key-value pairs that are not qfunctions
	 * are stored in map. QFunctions are stored separately as a list of
	 * dataportions and a list of support functions.
	 */
	private void readConfigFile() {
		if (!checkFileExists(configFilename)) {
			System.out.println("\"" + configFilename + "\" does not exists");
			return;
		}

		BufferedReader r;
		try {
			r = new BufferedReader(new FileReader(configFilename));

			String line;
			while ((line = r.readLine()) != null) {
				if (line.length() > 0 && line.charAt(0) == '#') {
					continue;
				}
				String[] split = line.split("=");
				if (split[0].contains(KEY_QFUNCTION)) {
					String[] qFunction = split[1].split("/");
					this.dataPortions.add(qFunction[0]);
					this.supportFunctions.add(qFunction[1]);
				} else {
					this.runProperties.put(split[0], split[1]);
				}
			}
			r.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the star is either 'additive' or 'multiplicative' ignoring
	 * caps.
	 * 
	 * @param star
	 *            string that needs to be checked
	 * @return true if the value is one of the expected
	 */
	private boolean checkStar(String star) {
		if (star.equalsIgnoreCase(VALUE_ADDITIVE)
				|| star.equalsIgnoreCase(VALUE_MULTIPLICATIVE)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if labels is either 'true' or 'false' ignoring caps
	 * 
	 * @param star
	 *            string that needs to be checked
	 * @return true if the value is one of the expected
	 */
	private boolean checkLabels(String labels) {
		if (labels.equalsIgnoreCase(VALUE_TRUE)
				|| labels.equalsIgnoreCase(VALUE_FALSE)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the data portion is either 'positive', 'negative' or 'all'
	 * ignoring caps.
	 * 
	 * @param dataPortion
	 *            string that needs to be checked
	 * @return true if the value is one of the expected
	 */
	private boolean checkDataPortion(String dataPortion) {
		if (dataPortion.equalsIgnoreCase(VALUE_POSITIVE)
				|| dataPortion.equalsIgnoreCase(VALUE_NEGATIVE)
				|| dataPortion.equalsIgnoreCase(VALUE_ALL)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the supportFunction is either 'positive' or 'negative' ignoring
	 * caps.
	 * 
	 * @param supportFunction
	 *            string that needs to be checked
	 * @return true if the value is one of the expected
	 */
	private boolean checkSupportFunction(String supportFunction) {
		if (supportFunction.equalsIgnoreCase(VALUE_POSITIVE)
				|| supportFunction.equalsIgnoreCase(VALUE_NEGATIVE)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if all necessary run properties are present and if the given
	 * values are conform to what is expected.
	 * 
	 * @return true if all run properties are okay and a distribution can be
	 *         built and sampled
	 */
	private boolean checkRunProperties() {
		if (this.runProperties.size() == 0) {
			System.out.println("No run properties specified!");
			return false;
		}
		boolean ok = true;
		if (!this.runProperties.containsKey(KEY_FILENAME)) {
			System.out.println("Filename of dataset is not specified!");
			ok &= false;
		}
		if (!this.runProperties.containsKey(KEY_SAMPLES)) {
			System.out.println("Number of samples is not specified!");
			ok &= false;
		}
		if (this.runProperties.containsKey(KEY_STAR)) {
			if (!checkStar(this.runProperties.get(KEY_STAR))) {
				System.out.println("'" + this.runProperties.get(KEY_STAR) + "'"
						+ "is an invalid star operation. Accepted values are '"
						+ VALUE_MULTIPLICATIVE + "' and '" + VALUE_ADDITIVE
						+ "'.");
				ok &= false;
			}
		}
		if (this.runProperties.containsKey(KEY_LABELS)) {
			if (!checkLabels(this.runProperties.get(KEY_LABELS))) {
				System.out.println("'" + this.runProperties.get(KEY_LABELS)
						+ "'"
						+ "is an invalid labels value. Accepted values are '"
						+ VALUE_TRUE + "' and '" + VALUE_FALSE + "'.");
				ok &= false;
			}
		}
		if (this.dataPortions.size() == 0) {
			System.out.println("No dataportions specified!");
			ok &= false;
		}
		for (String dataPortion : this.dataPortions) {
			if (!checkDataPortion(dataPortion)) {
				System.out.println("'" + dataPortion + "'"
						+ "is an invalid data portion. Accepted values are '"
						+ VALUE_POSITIVE + "', '" + VALUE_NEGATIVE + "' and '"
						+ VALUE_ALL + "'.");
				ok &= false;
			}
		}
		if (this.supportFunctions.size() == 0) {
			System.out.println("No support functions specified!");
			ok &= false;
		}
		for (String supportFunction : this.supportFunctions) {
			if (!checkSupportFunction(supportFunction)) {
				System.out
						.println("\""
								+ supportFunction
								+ "\""
								+ "is an invalid support function. Accepted values are '"
								+ VALUE_POSITIVE + "' and '" + VALUE_NEGATIVE
								+ "'.");
				ok &= false;
			}
		}
		if (this.runProperties.get(KEY_FILENAME).endsWith(".arff")) {
			if (!this.runProperties.containsKey(KEY_LABELATTRIBUTE)) {
				System.out
						.println("No class attribute specified for ARFF-dataset");
				ok &= false;
			}

			if (!this.runProperties.containsKey(KEY_POSLABELS)) {
				System.out
						.println("No positive attribute instances specified for ARFF-dataset");
				ok &= false;
			}

		}

		return ok;
	}

	/**
	 * Initializes the fields using the runProperties that are read from the
	 * config file.
	 */
	private void initializeFields() {
		this.fileName = this.runProperties.get(KEY_FILENAME);
		if (this.runProperties.containsKey(KEY_BIAS)) {
			this.bias = Integer.parseInt(this.runProperties.get(KEY_BIAS));
		}
		if (this.runProperties.containsKey(KEY_STAR)
				&& this.runProperties.get(KEY_STAR).equalsIgnoreCase(
						VALUE_ADDITIVE)) {
			this.star = StarOperation.ADDITIVE;
		}
		if (this.runProperties.containsKey(KEY_LABELS)) {
			this.keepLabels = this.runProperties.get(KEY_LABELS)
					.equalsIgnoreCase(VALUE_FALSE);
		}

		this.numberOfSamples = Integer.parseInt(this.runProperties
				.get("samples"));
		if (this.runProperties.containsKey(KEY_OUTPUT)) {
			this.outputFilename = this.runProperties.get(KEY_OUTPUT);
		}

		if (this.runProperties.containsKey(KEY_LABELATTRIBUTE)) {
			this.labelAttribute = this.runProperties.get(KEY_LABELATTRIBUTE);
		}
		if (this.runProperties.containsKey(KEY_POSLABELS)) {
			this.posLabels = this.runProperties.get(KEY_POSLABELS).split(",");
		}
	}

	/**
	 * Prints a key-value pair to the output stream separated by a colon.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	private void print(Object key, Object value) {
		DebugPrinter.println(key + ": " + value);
	}

	private void print(Object key, Object[] values) {
		StringBuffer buf = new StringBuffer();
		for (Object value : values) {
			buf.append(value + " ");
		}
		DebugPrinter.println(key + ": " + buf.toString());
	}

	/**
	 * Prints all distribution info to the output stream.
	 */
	private void printInfo() {
		if (DebugPrinter.verbose) {
			System.out.println("============");
			print(KEY_FILENAME, this.fileName);
			print(KEY_BIAS, this.bias);
			print(KEY_STAR, this.star);
			print(KEY_LABELS, this.keepLabels);
			for (int i = 0; i < this.dataPortions.size(); i++) {
				print(KEY_QFUNCTION, this.dataPortions.get(i) + "/"
						+ this.supportFunctions.get(i));
			}
			print(KEY_SAMPLES, this.numberOfSamples);
			print(KEY_OUTPUT, this.outputFilename);
			if (labelAttribute != null) {
				print(KEY_LABELATTRIBUTE, labelAttribute);
			}
			if (posLabels != null) {
				print(KEY_POSLABELS, posLabels);
			}
			System.out.println("============");
		}
	}

	/**
	 * Creates the sampling distribution using the properties from the config
	 * file.
	 * 
	 * @throws ImpossibleToSampleException
	 */
	private void createDistribution() throws FileNotFoundException,
			ImpossibleToSampleException {
		PosNegDbInterface db = DatabaseFactory.loadLabeledDB(fileName,
				labelAttribute, posLabels, keepLabels);

		DebugPrinter.print(db.toString());
		DebugPrinter.println("============");

		this.distribution = new GeneralDistribution(db);

		for (int i = 0; i < this.dataPortions.size(); i++) {
			List<PlainTransaction> transactions = newArrayList();
			if (this.dataPortions.get(i).equalsIgnoreCase("all")) {
				transactions = db.getTransactions();
			} else if (this.dataPortions.get(i).equalsIgnoreCase("positive")) {
				transactions = db.getTransactionsPos();
			} else if (this.dataPortions.get(i).equalsIgnoreCase("negative")) {
				transactions = db.getTransactionsNeg();
			}
			this.distribution.addQFunction(new DataPortion(transactions),
					SupportMeasure.getSupportMeasure(this.supportFunctions
							.get(i)));
		}
		this.distribution.setDefaultBias(this.bias);
		this.distribution.setStarOperation(this.star);

		if (biasesAreSet) {
			this.distribution.addBiases(biases);
		}

		this.distribution.initializeForCFTP();
	}

	/**
	 * Runs the distribution and samples X samples from it.
	 */
	private void runDistribution() {
		CouplingFromThePastSampler sampler = new CouplingFromThePastSampler(
				this.distribution);
		sampler.setResultDataStructureFactory(new ResultDataStructure.IdsListDataFactory());
		sampler.setMcmcFactory(new MetropolisHastingsFactory());
		sampler.run(this.numberOfSamples, this.outputFilename);
	}
}

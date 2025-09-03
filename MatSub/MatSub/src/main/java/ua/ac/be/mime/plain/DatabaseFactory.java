package ua.ac.be.mime.plain;

import java.io.File;
import java.io.FileNotFoundException;

import ua.ac.be.mime.plain.ArffConverter.OnlyCategoricalConverter;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;
import ua.ac.be.mime.plain.weighting.PosNegTransactionDb;
import ua.ac.be.mime.plain.weighting.WeightedTransactionDB;

public class DatabaseFactory {

	public static TransactionDBInterface loadTransactionsDB(String fileName)
			throws FileNotFoundException {
		checkFileExists(fileName);
		
		if (fileName.endsWith(".arff")) {
			return new OnlyCategoricalConverter().convert(fileName);
		}
		return new PlainTransactionDB(fileName);
	}

	public static PosNegDbInterface loadLabeledDB(String fileName,
			String attributeLabel, String[] posLabels, boolean keepLabels)
			throws FileNotFoundException {
		checkFileExists(fileName);
		if (fileName.endsWith(".arff")) {
			return new OnlyCategoricalConverter().convertLabeled(fileName, attributeLabel, posLabels, keepLabels);
		}
		return new PosNegTransactionDb(fileName);
	}

	public static PosNegDbInterface loadLabeledDB(String fileNamePos,
			String fileNameNeg) throws FileNotFoundException {
		checkFileExists(fileNamePos);
		checkFileExists(fileNameNeg);
		if (fileNamePos.endsWith(".arff")) {
			return new OnlyCategoricalConverter().convertLabeled(fileNamePos, fileNameNeg);
		}
		return new PosNegTransactionDb(fileNamePos, fileNameNeg);
	}

	private static void checkFileExists(String fileName)
			throws FileNotFoundException {
		if (!new File(fileName).exists()) {
			throw new FileNotFoundException("Database file '" + fileName
					+ "' does not exist!");
		}
	}

	public static PosNegDbInterface loadLabeledDB(String fileName)
			throws FileNotFoundException {
		return loadLabeledDB(fileName, false);
	}

	public static PosNegDbInterface loadLabeledDB(String fileName,
			boolean keepLabels) throws FileNotFoundException {
		checkFileExists(fileName);
		return new PosNegTransactionDb(fileName, keepLabels);
	}

	public static TransactionDBInterface loadWeightedTransactionsDb(
			String fileName, String biasFileName) throws FileNotFoundException {
		checkFileExists(fileName);
		if (fileName.endsWith(".arff")) {
			return new OnlyCategoricalConverter().convertWeighted(fileName, biasFileName);
		}
		if (biasFileName.isEmpty()) {
			return new WeightedTransactionDB(fileName);
		}
		return new WeightedTransactionDB(fileName, biasFileName);
	}
}

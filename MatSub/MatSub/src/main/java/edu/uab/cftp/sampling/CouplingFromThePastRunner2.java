package edu.uab.cftp.sampling;

import static com.google.common.collect.Lists.newArrayList;
import static edu.uab.cftp.sampling.DistributionFactory.isDiscriminavityFamily;
import static edu.uab.cftp.sampling.DistributionFactory.isWeightedFrequencyFamily;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ua.ac.be.mime.errorhandling.IncorrectParameterException;
import ua.ac.be.mime.errorhandling.NotYetImplementedException;
import ua.ac.be.mime.mining.ResultDataStructure;
import ua.ac.be.mime.mining.ResultDataStructure.IdsListWithTimes;
import ua.ac.be.mime.mining.ResultDataStructure.IdsListWithTimesDataFactory;
import ua.ac.be.mime.plain.DatabaseFactory;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.TransactionDBInterface;
import ua.ac.be.mime.tool.DebugPrinter;

public class CouplingFromThePastRunner2 {

	private static final String SOFTWARENAME = "Direct Sampling CFTP";
	private static final String DEVELOPERNAME = "Sandy Moens";

	public static final String VERSION = "0.11.0";
	private static final String DATE = "12.12.2013";

	public static void main(String[] args) throws NotYetImplementedException,
			IncorrectParameterException {
		if (args.length < 1) {
			System.out.println(SOFTWARENAME + " version " + VERSION + " (c) ("
					+ DATE + ")" + "\t(" + DEVELOPERNAME + ")");
			System.out
					.println("This software is a proof of concept for the algorithm "
							+ "described in\n\"Linear Space Direct Pattern Sampling using"
							+ " Coupling From The Past\"\nby M. Boley, S. Moens and"
							+ " T. Gaertner\n\n");
			System.out.println("Please specify: [input] ([inputNeg]) [output] "
					+ "[numberOfSamples] [-mDistribution]");
			String newline = "\n\t";
			// @formatter:off
			System.out.println("Distribution:"
					+ newline + "0       = General"
					+ newline + "11-19   = Frequency Family*"
					+ newline + "101-109 = Weighted Frequency Family*"
					+ newline + "21-29   = Area Family*"
					+ newline + "31-39   = Rare Family*"
					+ newline + "42      = Discriminativity"
					+ newline + "431     = Discriminativity x Frequency"
					+ newline + "432     = Discriminativity x Positive Frequency"
					+ newline + "433     = Discriminativity x Negative Frequency"
					+ newline + "441     = Discriminativity x Frequency2"
					+ newline + "442     = Discriminativity x Positive Frequency2");
			// @formatter:on
			System.out.println();
			System.out
					.println("*Family Distributions are instantiated as follows:");
			System.out.println("  The first index specifies the the family");
			System.out.println("   E.g., 21 specifies the Area family");

			System.out
					.println("  The (second index-1) specifies the number of frequency functions");
			System.out
					.println("   E.g., 21 instantiates the Area distribution");
			System.out
					.println("   E.g., 25 instantiates the Freq^4 x Area distribution");

			return;
		}
		List<String> inputs = newArrayList();

		String biasFile = "";
		int d = -1, v = 2, t = -1;

		for (int i = 0; i < args.length; i++) {
			if (args[i].charAt(0) == '-') {
				char c = args[i].charAt(1);
				if (c == 'm') {
					d = Integer.parseInt(args[i].substring(2));
				} else if (c == 'v') {
					v = Integer.parseInt(args[i].substring(2));
				} else if (c == 'b') {
					biasFile = args[i].substring(2);
				} else if (c == 't') {
					t = Integer.parseInt(args[i].substring(2));
				}
			} else {
				inputs.add(args[i]);
			}
		}

		DebugPrinter.verbose = (v == 2);

		long beg = System.currentTimeMillis();

		if (d == 0) {
			DebugPrinter
					.println("Sampling patterns using general distribution");
			GeneralDistributionRunner gdr = new GeneralDistributionRunner();
			try {
				gdr.perfomSamplingTask();
			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
				System.out.println("Terminating execution");
				return;
			} catch (ImpossibleToSampleException e) {
				System.out.println(e.getMessage());
				System.out.println("Terminating execution");
				return;
			}
			return;
		}

		TransactionDBInterface db = null;
		try {
			if (isDiscriminavityFamily(d)) {
				if (inputs.size() == 4) {
					db = DatabaseFactory.loadLabeledDB(inputs.get(0),
							inputs.get(1));
				} else if (inputs.size() == 3) {
					db = DatabaseFactory.loadLabeledDB(inputs.get(0));
				}
			} else if (isWeightedFrequencyFamily(d)) {
				db = DatabaseFactory.loadWeightedTransactionsDb(inputs.get(0),
						biasFile);
			} else {
				db = DatabaseFactory.loadTransactionsDB(inputs.get(0));
			}
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			System.out.println("Terminating execution");
			return;
		}
		if (db == null) {
			System.out.println("Something went wrong!! No database read");
			return;
		}

		DebugPrinter.print(db.toString());
		DebugPrinter.println("============");

		DebugPrinter.println("Sampling " + inputs.get(inputs.size() - 1)
				+ " patterns using " + d + " distribution");

		if (t != -1) {
			CouplingFromThePastSampler2 sampler = new CouplingFromThePastSampler2(
					db, d, Integer.parseInt(inputs.get(inputs.size() - 1)));
			IdsListWithTimesDataFactory dsFactory = new IdsListWithTimesDataFactory();
			dsFactory.setItemDb(db.getItemDB());
			sampler.setResultDataStructureFactory(dsFactory);
			long nbeg = System.currentTimeMillis();
			ResultDataStructure rds = sampler.run(t, TimeUnit.MILLISECONDS);
			DebugPrinter.println("Time taken: "
					+ (System.currentTimeMillis() - nbeg));
			writeToFile(rds, inputs.get(inputs.size() - 2));
			writeTimesToFile(rds);
			DebugPrinter.println("Sampled " + rds.size() + " patterns");
		}

		DebugPrinter.println("Time taken: "
				+ (System.currentTimeMillis() - beg));
	}

	private static void writeTimesToFile(ResultDataStructure rds) {
		IdsListWithTimes idwt = (IdsListWithTimes) rds;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					"times.txt"));

			for (long time : idwt.getTimes()) {
				writer.write(time + "\n");
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeToFile(ResultDataStructure rds, String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			Iterator<PlainItemSet> iterator = rds.iterator();
			StringBuilder builder = new StringBuilder();
			while (iterator.hasNext()) {
				builder.setLength(0);
				PlainItemSet itemSet = iterator.next();
				for (PlainItem item : itemSet) {
					builder.append(item + " ");
				}
				builder.setLength(builder.length() - 1);
				writer.write(builder.toString() + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

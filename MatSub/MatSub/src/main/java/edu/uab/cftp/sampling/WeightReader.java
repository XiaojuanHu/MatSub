package edu.uab.cftp.sampling;

public class WeightReader extends AssignmentFileReader<Integer, Double> {

	public WeightReader(String fileName, String delimiter) {
		super(fileName, delimiter, Integer.class, Double.class);
	}

}

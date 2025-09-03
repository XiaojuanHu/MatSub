package edu.uab.cftp.sampling;

public class ImpossibleToSampleException extends Exception {

	private static final long serialVersionUID = 820531355680722907L;

	private String message;

	public ImpossibleToSampleException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

}

package ua.ac.be.mime.errorhandling;

public class SupportRemovedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6775616099002544747L;

	private String value;

	public SupportRemovedException(String value) {
		this.value = value;
	}

	public String getMessage() {
		return "Support for " + value + " has been removed";
	}
}

package ua.ac.be.mime.errorhandling;

public class IncorrectParameterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6775616099002544747L;

	private String value;
	private String parameterName;
	private String option;

	public IncorrectParameterException(String value, String parameterName,
			String option) {
		this.value = value;
		this.parameterName = parameterName;
		this.option = option;
	}

	public String getMessage() {
		return "Parameter value " + this.value + " for parameter "
				+ this.parameterName + " (" + this.option + ") is not correct";
	}
}

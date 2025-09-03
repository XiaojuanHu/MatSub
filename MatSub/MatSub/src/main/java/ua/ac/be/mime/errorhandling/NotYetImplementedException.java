package ua.ac.be.mime.errorhandling;

public class NotYetImplementedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3855905170051075949L;

	public static enum Type {
		CLASS {
			public String toString() {
				return "Class";
			}
		},
		FUNCTION {
			public String toString() {
				return "Function";
			}
		}
	}

	private Type type;
	private String name;

	public NotYetImplementedException(Type type, String name) {
		this.type = type;
		this.name = name;
	}

	public String getMessage() {
		return this.type + " " + this.name
				+ (this.type.equals(Type.FUNCTION) ? "()" : "")
				+ " has not yet been implemented";
	}
}

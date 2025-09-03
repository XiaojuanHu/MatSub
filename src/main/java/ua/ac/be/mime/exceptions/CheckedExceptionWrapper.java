package ua.ac.be.mime.exceptions;

public class CheckedExceptionWrapper extends RuntimeException {
	private static final long serialVersionUID = -8960528439112158203L;

	public CheckedExceptionWrapper(Throwable e) {
		super(e);
	}
}

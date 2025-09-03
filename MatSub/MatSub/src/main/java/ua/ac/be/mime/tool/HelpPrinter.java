package ua.ac.be.mime.tool;

public class HelpPrinter {

	public static void printPleaseSpecify(String... parameters) {
		StringBuffer buf = new StringBuffer("Please specify: ");
		for (String parameter : parameters) {
			buf.append("[" + parameter + "] ");
		}
		System.out.println(buf.toString());
	}
}

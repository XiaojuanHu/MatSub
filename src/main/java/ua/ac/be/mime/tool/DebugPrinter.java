package ua.ac.be.mime.tool;

/**
 * Provides easier debug printing methods. The printer only prints to the output
 * stream if the verbose option is set to true.
 * 
 * @author Sandy Moens
 * 
 */
public class DebugPrinter {

	public static boolean verbose = false;

	/**
	 * Prints a single message to the output stream if verbose
	 * 
	 * @param message
	 *            the message to be printed
	 */
	public static void print(String message) {
		if (DebugPrinter.verbose) {
			System.out.print(message);
		}
	}

	/**
	 * Prints the name of the object and a single message to the output stream
	 * if verbose: e.g. "[DebugPrinter]: testMessage"
	 * 
	 * @param o
	 *            the object that sends the message
	 * @param message
	 *            the message to be printed
	 */
	public static void print(Object o, String message) {
		if (DebugPrinter.verbose) {
			System.out.print("[" + o.getClass().getSimpleName() + "]: "
					+ message);
		}
	}

	/**
	 * Prints a name and a single message to the output stream if verbose: e.g.
	 * "[aName]: testMessage"
	 * 
	 * @param o
	 *            the object that sends the message
	 * @param message
	 *            the message to be printed
	 */
	public static void print(String name, String message) {
		if (DebugPrinter.verbose) {
			System.out.print("[" + name + "]: " + message);
		}
	}

	/**
	 * Prints a new line to the output stream if verbose
	 * 
	 * @param message
	 *            the message to be printed
	 */
	public static void println() {
		if (DebugPrinter.verbose) {
			System.out.println();
		}
	}

	/**
	 * Prints a single message to the output stream if verbose. The message is
	 * terminated by a new line
	 * 
	 * @param message
	 *            the message to be printed
	 */
	public static void println(String message) {
		if (DebugPrinter.verbose) {
			System.out.println(message);
		}
	}

	/**
	 * Prints the name of the object and a single message to the output stream
	 * if verbose. The message is terminated by a new line: e.g.
	 * "[DebugPrinter]: testMessage\n"
	 * 
	 * @param o
	 *            the object that sends the message
	 * @param message
	 *            the message to be printed
	 */
	public static void println(Object o, String message) {
		if (DebugPrinter.verbose) {
			System.out.println("[" + o.getClass().getSimpleName() + "]: "
					+ message);
		}
	}

	/**
	 * Prints a name and a single message to the output stream if verbose. The
	 * message is terminated by a new line: e.g. "[aName]: testMessage\n"
	 * 
	 * @param o
	 *            the object that sends the message
	 * @param message
	 *            the message to be printed
	 */
	public static void println(String name, String message) {
		if (DebugPrinter.verbose) {
			System.out.println("[" + name + "]: " + message);
		}
	}

	/**
	 * Flushes the output to the stream if verbose.
	 */
	public static void flush() {
		if (DebugPrinter.verbose) {
			System.out.flush();
		}
	}
}

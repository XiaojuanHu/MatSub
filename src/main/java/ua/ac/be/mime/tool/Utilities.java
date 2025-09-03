/**
 * 
 */
package ua.ac.be.mime.tool;

/**
 * Determines the operating system (lesser version of qt.com.trolltech.Utilities
 * used on the server side to become independent of qtjambi)
 * 
 * @author Sandy
 * 
 */
public class Utilities {

	// Different types of operating systems
	public enum OperatingSystem {
		WINDOWS, LINUX, MACOSX
	}

	public final static OperatingSystem operatingSystem = decideOperatingSystem();

	/**
	 * Checks if the current operating system is windows based
	 * 
	 * @return true if the current operating system is windows
	 */
	public static boolean isWindows() {
		if (operatingSystem.equals(OperatingSystem.WINDOWS)) {
			return true;
		} else
			return false;
	}

	/**
	 * Checks if the current operating system is linux based
	 * 
	 * @return true if the current operating system is linux
	 */
	public static boolean isLinux() {
		if (operatingSystem.equals(OperatingSystem.LINUX)) {
			return true;
		} else
			return false;
	}

	/**
	 * Checks if the current operating system is os x based
	 * 
	 * @return true if the current operating system is os x
	 */
	public static boolean isMacOSX() {
		if (operatingSystem.equals(OperatingSystem.MACOSX)) {
			return true;
		} else
			return false;
	}

	/**
	 * Determines the type of the operating system
	 * 
	 * @return the type of operating system
	 */
	private static OperatingSystem decideOperatingSystem() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.startsWith("windows"))
			return OperatingSystem.WINDOWS;
		if (osName.startsWith("mac os x"))
			return OperatingSystem.MACOSX;
		return OperatingSystem.LINUX;
	}
}

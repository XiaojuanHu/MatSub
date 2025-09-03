package de.unibonn.realkd;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Test;

public class RealKDTest {

	private static final String RESULT_FILE = "output.txt";
	public static final String GERMANY_TEST_SCRIPT = "src/test/resources/testscripts/germany_run_algorithm.rs";

	@Test
	public void mainTest() {
		File outputfile = new File(RESULT_FILE);
		outputfile.delete();
		// String[] args = {
		// "load",
		// TestConstants.GERMANY_DATA_TXT,
		// TestConstants.GERMANY_ATTRIBUTES_TXT,
		// TestConstants.GERMANY_GROUPS_TXT,
		// "run",
		// "EMM_BEAMSEARCH",
		// ModelClassParameter.NAME + "=" +
		// MetricEmpiricalDistributionFactory.STRING_NAME,
		// EMMParameters.distanceFunctionParameterName()
		// + "=Manhattan mean distance",
		// // "Model class and distance function=Eucledian distance between
		// means",
		// "Target attributes=[CDU 2005,SPD 2005]", "test=blupp" };
		String[] args = { GERMANY_TEST_SCRIPT };
		RealKD.main(args);
		assertTrue(outputfile.exists());
	}

	@After
	public void cleanUp() {
		File outputfile = new File(RESULT_FILE);
		outputfile.delete();
	}

}

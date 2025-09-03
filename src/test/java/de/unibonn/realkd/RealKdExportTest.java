package de.unibonn.realkd;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Test;

public class RealKdExportTest {

	private static final String NAMES_FILE = "data.names";
	private static final String DATA_FILE = "data.fimi";
	public static final String GERMANY_EXPORT_SCRIPT = "src/test/resources/testscripts/germany_export_fimi.rs";

	@Test
	public void mainTest() {
		File outputfile = new File(DATA_FILE);
		File namesfile = new File(NAMES_FILE);
		outputfile.delete();
		namesfile.delete();
		String[] args = { GERMANY_EXPORT_SCRIPT };
		RealKD.main(args);
		assertTrue(outputfile.exists());
		assertTrue(namesfile.exists());
	}

	@After
	public void cleanUp() {
		 File outputfile = new File(DATA_FILE);
		 outputfile.delete();
		 new File(NAMES_FILE).delete();
	}

}

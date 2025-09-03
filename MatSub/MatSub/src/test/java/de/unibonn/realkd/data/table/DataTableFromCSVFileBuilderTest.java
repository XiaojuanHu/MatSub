package de.unibonn.realkd.data.table;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.unibonn.realkd.common.testing.TestConstants;

public class DataTableFromCSVFileBuilderTest {

	public static DataTableFromCSVFileBuilder builder = new DataTableFromCSVFileBuilder()
			.setDataCSVFilename(TestConstants.GERMANY_DATA_TXT)
			.setAttributeMetadataCSVFilename(
					TestConstants.GERMANY_ATTRIBUTES_TXT)
			.setAttributeGroupCSVFilename(TestConstants.GERMANY_GROUPS_TXT);

	private DataTable loadedDatatable;

	@Test
	public void testBuild() throws DataFormatException {
		this.loadedDatatable = builder.build();
		assertNotNull(loadedDatatable);
	}

}

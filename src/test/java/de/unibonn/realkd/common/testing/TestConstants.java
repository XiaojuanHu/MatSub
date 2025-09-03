package de.unibonn.realkd.common.testing;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.base.Lazy;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.common.workspace.Workspaces;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.propositions.PropositionalContextFromTableBuilder;
import de.unibonn.realkd.data.table.DataFormatException;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.DataTableFromCSVFileBuilder;

public class TestConstants {

	public static final Identifier TABLE_ID = Identifier.id("germany");

	public static final String GERMANY_GROUPS_TXT = "src/main/resources/data/germany/groups.txt";

	public static final String GERMANY_ATTRIBUTES_TXT = "src/main/resources/data/germany/attributes.txt";

	public static final String GERMANY_DATA_TXT = "src/main/resources/data/germany/data.txt";

	public static final String GERMANY_DATA_SAMPLE_TXT = "src/main/resources/data/germany/data_sample.txt";

	public static Workspace getGermanyWorkspace() {
		Workspace result = Workspaces.workspace();
		result.add(getGermanyDataTable());
		result.add(getGermanyPropositionalLogic());
		return result;
	}

	private static DataTable createGermanyDatatable() {
		DataTableFromCSVFileBuilder builder = new DataTableFromCSVFileBuilder();
		builder.setAttributeGroupCSVFilename(TestConstants.GERMANY_GROUPS_TXT)
				.setDataCSVFilename(TestConstants.GERMANY_DATA_TXT)
				.setAttributeMetadataCSVFilename(TestConstants.GERMANY_ATTRIBUTES_TXT).setId(TABLE_ID);
		try {
			return builder.build();
		} catch (DataFormatException e) {
			throw new RuntimeException(e);
		}
	}

	private static Lazy<DataTable> GERMANY_DATATABLE = Lazy.lazy(() -> createGermanyDatatable());

	private static PropositionalContext GERMANY_PROPOSITIONS = null;

	public static DataTable getGermanyDataTable() {
		return GERMANY_DATATABLE.get();
	}

	public static PropositionalContext getGermanyPropositionalLogic() {
		if (GERMANY_PROPOSITIONS == null) {
			GERMANY_PROPOSITIONS = createGermanyPropositions();
		}
		return GERMANY_PROPOSITIONS;
	}

	private static PropositionalContext createGermanyPropositions() {
		return new PropositionalContextFromTableBuilder().apply(getGermanyDataTable());
	}

}

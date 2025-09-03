/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package de.unibonn.realkd.patterns.sequence;

import java.nio.file.Paths;

import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.common.workspace.Workspaces;
import de.unibonn.realkd.data.propositions.PropositionalContextFromTableBuilder;
import de.unibonn.realkd.data.sequences.SequentialPropositionalContext;
import de.unibonn.realkd.data.sequences.SequentialPropositionalContextFromTableFactory;
import de.unibonn.realkd.data.table.DataFormatException;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.DataTableFromCSVFileBuilder;

/**
 * @author Sandy Moens
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public class TestConstants {

	private static String SEQUENCE_DIR_DATE = Paths.get("src", "main", "resources", "data", "sequences", "date").toString();
	private static String SEQUENCE_DATA_DATE = Paths.get(SEQUENCE_DIR_DATE, "data.txt").toString();
	private static String SEQUENCE_ATTRIBUTES_DATE = Paths.get(SEQUENCE_DIR_DATE, "attributes.txt").toString();
	
	private static String SEQUENCE_DIR_ORDINAL = Paths.get("src", "main", "resources", "data", "sequences", "ordinal").toString();
	private static String SEQUENCE_DATA_ORDINAL = Paths.get(SEQUENCE_DIR_ORDINAL, "data.txt").toString();
	private static String SEQUENCE_ATTRIBUTES_ORDINAL = Paths.get(SEQUENCE_DIR_ORDINAL, "attributes.txt").toString();
		
	private static Workspace getSequenceWorkspace(String dataFile, String attributesFile) {
		DataTableFromCSVFileBuilder builder = new DataTableFromCSVFileBuilder().setDataCSVFilename(dataFile)
				.setAttributeMetadataCSVFilename(attributesFile);
		
		SequentialPropositionalContextFromTableFactory pFactory = new SequentialPropositionalContextFromTableFactory();
		pFactory.groupingAttributeName("id");
		pFactory.distanceAttributeName("date");
		pFactory.mappers(PropositionalContextFromTableBuilder.DEFAULT_MAPPERS);
		
		try {
			DataTable dataTable = builder.build();
			
			SequentialPropositionalContext context = pFactory.build(dataTable);
			
			Workspace workspace = Workspaces.workspace();
			workspace.add(dataTable);
			workspace.add(context);
			
			return workspace;
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Workspace getSequenceWorkspaceWithOrdinalDistance() {
		return getSequenceWorkspace(SEQUENCE_DATA_ORDINAL, SEQUENCE_ATTRIBUTES_ORDINAL);
	}
	
	public static Workspace getSequenceWorkspaceWithDateDistance() {
		return getSequenceWorkspace(SEQUENCE_DATA_DATE, SEQUENCE_ATTRIBUTES_DATE);
	}
	
}

package de.unibonn.realkd.data.table;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import de.unibonn.realkd.common.base.Identifier;

/**
 * Builds a datatable from csv files for attribute metadata, data, and attribute
 * groups. Provides same options as {@link DataTableFromCSVBuilder} except that
 * instead accepting plain csv files reads content from files.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.1.0.1
 * 
 */
public class DataTableFromCSVFileBuilder {

	private final DataTableFromCSVBuilder fromCSVBuilder;

	private String dataFilename = null;

	private String attributesFilename = null;

	private String attributeGroupFileName = null;

	public DataTableFromCSVFileBuilder() {
		this.fromCSVBuilder = new DataTableFromCSVBuilder();
	}

	public DataTableFromCSVFileBuilder setAttributeMetadataCSVFilename(
			String attributesFilename) {
		this.attributesFilename = attributesFilename;
		return this;
	}

	public DataTableFromCSVFileBuilder setAttributeGroupCSVFilename(
			String attributeGroupFileName) {
		this.attributeGroupFileName = attributeGroupFileName;
		return this;
	}

	public DataTableFromCSVFileBuilder setDataCSVFilename(String dataFilename) {
		this.dataFilename = dataFilename;
		return this;
	}

	/**
	 * Id of the datatable to be build.
	 */
	public DataTableFromCSVFileBuilder setId(Identifier id) {
		this.fromCSVBuilder.id(id);
		return this;
	}
	
	/**
	 * Name of the datatable to be build.
	 */
	public DataTableFromCSVFileBuilder setName(String name) {
		this.fromCSVBuilder.name(name);
		return this;
	}

	/**
	 * Description of the datatable to be build.
	 */
	public DataTableFromCSVFileBuilder setDescription(String description) {
		this.fromCSVBuilder.description(description);
		return this;
	}

	/**
	 * Delimiter used in CSV files to separate values (default ';').
	 */
	public DataTableFromCSVFileBuilder setDelimiter(Character delimiter) {
		this.fromCSVBuilder.delimiter(delimiter);
		return this;
	}

	/**
	 * Symbol used in CSV files to indicate missing values (default '?').
	 */
	public DataTableFromCSVFileBuilder setMissingSymbol(String symbol) {
		this.fromCSVBuilder.missingSymbol(symbol);
		return this;
	}

	public DataTable build() throws DataFormatException {
		setDataCSV();
		setAttributesCSV();
		setGroupsCSV();
		return this.fromCSVBuilder.build();
	}

	private void setGroupsCSV() {
		String attributeGroupsFileContent = "";
		if (attributeGroupFileName != null) {
			try {
				attributeGroupsFileContent = loadFileToString(attributeGroupFileName);
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException(
						"attribute-groups file not found", e);
			}
		}
		this.fromCSVBuilder.attributeGroupCSV(attributeGroupsFileContent);
	}

	private void setAttributesCSV() {
		if (attributesFilename == null) {
			throw new IllegalStateException(
					"attributes metadata filename not specified");
		}
		String attributesFileContent = null;
		try {
			attributesFileContent = loadFileToString(attributesFilename);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("attributes file '"+attributesFilename+"' not found", e);
		}
		this.fromCSVBuilder.attributeMetadataCSV(attributesFileContent);
	}

	private void setDataCSV() {
		if (dataFilename == null) {
			throw new IllegalStateException("data filename not specified");
		}
		String dataFileContent = null;
		try {
			dataFileContent = loadFileToString(dataFilename);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("data file not found", e);
		}
		this.fromCSVBuilder.dataCSV(dataFileContent);
	}

	private static String loadFileToString(String fileName)
			throws FileNotFoundException {
		File file = new File(fileName);
		return convertStreamToString(new FileInputStream(file));
	}

	private static String convertStreamToString(InputStream is) {
		Scanner s = new Scanner(is);
		s.useDelimiter("\\A");
		String result = s.hasNext() ? s.next() : "";
		s.close();
		return result;
	}

}

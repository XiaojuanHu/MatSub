/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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
package de.unibonn.realkd.data.xarf;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.CharSource.concat;
import static com.google.common.io.CharSource.wrap;
import static com.google.common.io.Files.asCharSource;
import static de.unibonn.realkd.data.xarf.XarfParsing.parse;
import static java.util.Optional.empty;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;

import de.unibonn.realkd.data.table.AttributesFromGroupMapper;
import de.unibonn.realkd.data.table.DataTable;

/**
 * @author Panagiotis Mandros
 * 
 * @author Michael Hedderich
 * 
 * @author Mario Boley
 * 
 * @since 0.4.0
 * 
 * @version 0.7.0
 *
 */
public class XarfImport {

	private String dataFilename;
	private Optional<String> metadataFilename;
	private List<AttributesFromGroupMapper> groupMappers = ImmutableList.copyOf(AttributesFromGroupMapper.values());

	private XarfImport() {
		this.dataFilename = null;
		this.metadataFilename = empty();
	}

	private XarfImport(String filename, String metadataFilename) {
		this.dataFilename = filename;
		this.metadataFilename = Optional.ofNullable(metadataFilename);
	}

	private XarfImport(String filename) {
		this.dataFilename = filename;
	}

	public static XarfImport xarfImport() {
		return new XarfImport();
	}

	public static XarfImport xarfImport(String filename) {
		return new XarfImport().dataFilename(filename);
	}

	public static XarfImport xarfImport(String filename, String metadataFilename) {
		return new XarfImport(filename, metadataFilename);
	}

	public XarfImport dataFilename(String filename) {
		this.dataFilename = filename;
		return this;
	}

	public XarfImport groupMappers(List<AttributesFromGroupMapper> groupMappers) {
		this.groupMappers = groupMappers;
		return this;
	}

	/**
	 * Parses the files provided and returns a data table
	 * 
	 * 
	 * @return a data table
	 * @throws IOException
	 */
	public DataTable get() {
		if (dataFilename == null) {
			throw new IllegalStateException("Filename not provided");
		}

		CharSource dataSource = asCharSource(new File(dataFilename), UTF_8);
		CharSource inputSource = metadataFilename.isPresent()
				? concat(asCharSource(new File(metadataFilename.get()), UTF_8), wrap("\n"), dataSource)
				: dataSource;
		// NOTE concat does not insert a linebreak after last line; need to insert one
		// for safety in order to not merge last line of first file with first line of
		// second file

		try {
			Xarf xarf = parse(inputSource.openBufferedStream());
			return xarf.toDatatable(groupMappers);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}

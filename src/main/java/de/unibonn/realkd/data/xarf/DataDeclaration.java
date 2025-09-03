/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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

import static java.util.Optional.empty;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.unibonn.realkd.data.table.CSV;
import de.unibonn.realkd.data.xarf.XarfParsing.CsvPortion;
import de.unibonn.realkd.data.xarf.XarfParsing.Token;

public class DataDeclaration {

	public static enum HeaderOption {
		YES {
			@Override
			public CsvPortion parseDataPortion(List<List<String>> dataRows) {
				return new CsvPortion(dataRows.subList(1, dataRows.size()), Optional.of(dataRows.get(0)));
			}
		},
		NO {
			@Override
			public CsvPortion parseDataPortion(List<List<String>> dataRows) {
				return new CsvPortion(dataRows, empty());
			}
		},
		AUTO {
			@Override
			public CsvPortion parseDataPortion(List<List<String>> dataRows) {
				Optional<List<String>> header = XarfParsing.autodetectedCSVHeader(dataRows);
				if (header.isPresent()) {
					return new CsvPortion(dataRows.subList(1, dataRows.size()), header);
				} else {
					return new CsvPortion(dataRows, empty());
				}
			}
		};

		public abstract CsvPortion parseDataPortion(List<List<String>> dataRows);

	}

	public final DataDeclaration.HeaderOption headerOption;

	public DataDeclaration(DataDeclaration.HeaderOption headerOption) {
		this.headerOption = headerOption;
	}

	public CsvPortion parseDataPortion(List<String> dataRows) {
		List<List<String>> csvParsed = CSV.csvStringListToListOfLists(dataRows);
		return headerOption.parseDataPortion(csvParsed);
	}

	static Optional<HeaderOption> parseHeaderOptionValue(String str) {
		switch (str.toLowerCase()) {
		case "yes":
			return Optional.of(HeaderOption.YES);
		case "no":
			return Optional.of(HeaderOption.NO);
		case "auto":
			return Optional.of(HeaderOption.AUTO);
		default:
			XarfParsing.LOGGER.warning(
					"Unknown value for header parameter " + str + " (possible values are 'yes', 'no', and 'auto').");
			return empty();
		}
	}

	public static DataDeclaration dataDeclaration(String dataDeclarationLine) {
		Token[] tokens = XarfParsing.tokens(dataDeclarationLine);
		Map<String, Object> parameters = XarfParsing.parameters(tokens);

		Optional<String> headerOptionString = Optional.ofNullable(parameters.get("header"))
				.filter(v -> v instanceof String).map(v -> (String) v);
		HeaderOption headerOption = headerOptionString.flatMap(s -> DataDeclaration.parseHeaderOptionValue(s))
				.orElse(HeaderOption.AUTO);

		return new DataDeclaration(headerOption);
	}

	public static final DataDeclaration DEFAULT_DATA_DECLARATION = new DataDeclaration(HeaderOption.AUTO);

}
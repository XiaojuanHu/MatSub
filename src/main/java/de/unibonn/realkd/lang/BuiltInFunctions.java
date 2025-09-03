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
package de.unibonn.realkd.lang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.Populations;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.propositions.PropositionalContextFromTableBuilder;
import de.unibonn.realkd.data.propositions.PropositionalizationRule;
import de.unibonn.realkd.data.table.DataFormatException;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.DataTableFromCSVFileBuilder;
import de.unibonn.realkd.data.xarf.XarfImport;
import de.unibonn.realkd.lang.types.FunctionDefinition;
import de.unibonn.realkd.lang.types.NumericValue;
import de.unibonn.realkd.lang.types.StringValue;
import de.unibonn.realkd.lang.types.Types;
import de.unibonn.realkd.util.PropositionalLogicExport;

/**
 * @author Mario Boley
 *
 * @since 0.3.0
 * 
 * @version 0.3.0
 * 
 */
public class BuiltInFunctions implements FunctionProvider {

	private static final FunctionDefinition<NumericValue> LOG = Types.function("log", NumericValue.class,
			NumericValue.class, ImmutableList.of(), ImmutableList.of(), ImmutableList.of("base"),
			ImmutableList.of(NumericValue.class), (m, mand, opt) -> {
				NumericValue arg = (NumericValue) m;
				NumericValue base = (NumericValue) opt.get(0).orElse(Types.numericValue(Math.E));
				return Types.numericValue(Math.log(arg.asDouble()) / Math.log(base.asDouble()));
			});

	private static final FunctionDefinition<Population> POPULATION = (FunctionDefinition<Population>) Types.function(
			"population", Population.class, List.class, ImmutableList.of("id"), ImmutableList.of(StringValue.class),
			ImmutableList.of("name", "description"), ImmutableList.of(StringValue.class, StringValue.class),
			(m, mand, opt) -> {
				StringValue id = (StringValue) mand.get(0);
				StringValue name = (StringValue) opt.get(0).orElse(id);
				StringValue description = (StringValue) opt.get(1).orElse(Types.stringValue(""));
				@SuppressWarnings("unchecked")
				List<StringValue> entityNames = (List<StringValue>) m;
				return Populations.population(Identifier.id(id.asString()), name.asString(), description.asString(),
						entityNames.stream().map(v -> v.asString()).collect(Collectors.toList()));
			});

	/**
	 * Maps propositional logic to string stream of fimi format, which can be
	 * used by export statement.
	 */
	private static final FunctionDefinition<Iterable<String>> FIMI_DATA = Types.function("fimidata", Iterable.class,
			PropositionalContext.class, ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(),
			(m, mand, opt) -> PropositionalLogicExport.fimiIterable((PropositionalContext) m));

	/**
	 * Maps propositional logic to string stream of fimi names, i.e., the name
	 * for each proposition index, which can be used by export statement.
	 */
	private static final FunctionDefinition<Iterable<?>> FIMI_NAMES = Types.function("fiminames", Iterable.class,
			PropositionalContext.class, ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(),
			(m, mand, opt) -> PropositionalLogicExport.fimiMetaDataStream((PropositionalContext) m));

	public static final FunctionDefinition<PropositionalContext> STATEMENTS = Types.function("statements",
			PropositionalContext.class, DataTable.class, ImmutableList.of("id"), ImmutableList.of(StringValue.class),
			ImmutableList.of("mappers", "name"), ImmutableList.of(Set.class, StringValue.class), (m, mand, opt) -> {
				PropositionalContextFromTableBuilder logicBuilder = new PropositionalContextFromTableBuilder();
				logicBuilder.id(Identifier.id(((StringValue) mand.get(0)).asString()));
				if (opt.get(0).isPresent()) {
					@SuppressWarnings("unchecked")
					Set<PropositionalizationRule> mappers = (Set<PropositionalizationRule>) opt.get(0).get();
					logicBuilder.mappers(mappers);
				}
				if (opt.get(1).isPresent()) {
					logicBuilder.name(((StringValue) opt.get(1).get()).asString());
				}
				PropositionalContext result = logicBuilder.apply((DataTable) m);
				return result;
			});

	private static final FunctionDefinition<MiningAlgorithm> COMPUTATION = Types.function("computation",
			MiningAlgorithm.class, MiningAlgorithm.class, ImmutableList.of(), ImmutableList.of(),
			ImmutableList.of("params", "values"), ImmutableList.of(List.class, List.class), (m, mand, opt) -> {
				MiningAlgorithm algorithm = (MiningAlgorithm) m;
				Map<String, String> nameToValue = new HashMap<>();
				@SuppressWarnings("unchecked")
				List<StringValue> params = (List<StringValue>) opt.get(0).orElse(ImmutableList.of());
				@SuppressWarnings("unchecked")
				List<StringValue> values = (List<StringValue>) opt.get(1).orElse(ImmutableList.of());
				if (params.size() != values.size()) {
					throw new IllegalArgumentException("params and values lists must be of same length");
				}
				for (int i = 0; i < params.size(); i++) {
					nameToValue.put(params.get(i).asString(), values.get(i).asString());
				}
				algorithm.passValuesToParameters(nameToValue);
				return algorithm;
			});

	private static final FunctionDefinition<DataTable> XARFIMPORT = Types.function("xarfimport", DataTable.class,
			StringValue.class, ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(),
			(m, mand, opt) -> {
				StringValue filename = (StringValue) m;
				XarfImport xarfImport=XarfImport.xarfImport(filename.asString());
				return xarfImport.get();
			});

	private static final FunctionDefinition<DataTable> CSVIMPORT = Types.function("csvimport", DataTable.class,
			StringValue.class, ImmutableList.of("id", "attributes"),
			ImmutableList.of(StringValue.class, StringValue.class),
			ImmutableList.of("groups", "delimeter", "missingsymb", "description", "name"), ImmutableList
					.of(StringValue.class, StringValue.class, StringValue.class, StringValue.class, StringValue.class),
			(m, mand, opt) -> {
				StringValue dataFilename = (StringValue) m;
				StringValue id = (StringValue) mand.get(0);
				StringValue attributesFilename = (StringValue) mand.get(1);

				DataTableFromCSVFileBuilder builder = new DataTableFromCSVFileBuilder();
				builder.setId(Identifier.id(id.asString()));
				builder.setDataCSVFilename(dataFilename.asString());
				builder.setAttributeMetadataCSVFilename(attributesFilename.asString());
				if (opt.get(4).isPresent()) {
					builder.setName(((StringValue) opt.get(4).get()).asString());
				}
				if (opt.get(0).isPresent()) {
					builder.setAttributeGroupCSVFilename(((StringValue) opt.get(0).get()).asString());
				}
				if (opt.get(3).isPresent()) {
					builder.setDescription(((StringValue) opt.get(3).get()).asString());
				}
				if (opt.get(1).isPresent()) {
					String delimeter = ((StringValue) opt.get(1).get()).asString();
					if (delimeter.length() != 1) {
						throw new IllegalArgumentException(
								"csvimport: delimeter symbol must contain exactly one char (found '" + delimeter
										+ "')");
					}
					builder.setDelimiter(delimeter.charAt(0));
				}
				if (opt.get(2).isPresent()) {
					String missingSymb = ((StringValue) opt.get(2).get()).asString();
					if (missingSymb.length() != 1) {
						throw new IllegalArgumentException(
								"csvimport: missing symbol must contain exactly one char (found '" + missingSymb
										+ "')");
					}
					builder.setMissingSymbol(missingSymb);
				}

				try {
					return builder.build();
				} catch (DataFormatException e) {
					throw new IllegalArgumentException(e.getMessage());
				}

			});

	public static final List<FunctionDefinition<?>> ALL = ImmutableList.of(LOG, POPULATION, STATEMENTS, COMPUTATION,
			XARFIMPORT, CSVIMPORT, FIMI_DATA, FIMI_NAMES);

	@Override
	public String name() {
		return "build-in functions";
	}

	@Override
	public List<FunctionDefinition<?>> get() {
		return ALL;
	}

}

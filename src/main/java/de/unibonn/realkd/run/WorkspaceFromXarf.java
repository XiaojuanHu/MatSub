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
package de.unibonn.realkd.run;

import static de.unibonn.realkd.common.workspace.NamedStringValue.value;
import static de.unibonn.realkd.data.propositions.StandardPropositionalizationScheme.standardPropositionalizationScheme;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.propositions.PropositionalizationScheme;
import de.unibonn.realkd.data.propositions.StandardPropositionalizationScheme;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.xarf.XarfImport;

/**
 * @author Mario Boley
 *
 * @version 0.6.0
 * 
 * @since 0.7.0
 *
 */
@KdonTypeName("workspaceFromXarf")
@KdonDoc("Provides a workspace with a table generated from a single xarf file along with a matching propositional context generated according to some propositionalization scheme.")
public class WorkspaceFromXarf implements WorkspaceSpecification {

	private static final StandardPropositionalizationScheme DEFAULT_PROP_SCHEME = standardPropositionalizationScheme();

	public static WorkspaceFromXarf workspaceFromXarf(Identifier id, String xarfFilePath) {
		return new WorkspaceFromXarf(id, xarfFilePath, null, null);
	}

	public static WorkspaceFromXarf workspaceFromXarf(Identifier id, String xarfFilePath,
			Map<Identifier, String> values) {
		return new WorkspaceFromXarf(id, xarfFilePath, null, new HashMap<>(values));
	}

	@JsonProperty("id")
	private final Identifier id;

	@JsonProperty("datafile")
	private final String dataFile;

	private final PropositionalizationScheme propScheme;

	@JsonProperty("values")
	@JsonInclude(Include.NON_EMPTY)
	@KdonDoc("Additional values that are stored in the workspace.")
	private final HashMap<Identifier, String> values;

	@JsonCreator
	private WorkspaceFromXarf(@JsonProperty("id") Identifier id, @JsonProperty("datafile") String path,
			@JsonProperty("propScheme") PropositionalizationScheme propScheme,
			@JsonProperty("values") HashMap<Identifier, String> values) {
		this.id = id;
		this.dataFile = path;
		this.propScheme = (propScheme != null) ? propScheme : DEFAULT_PROP_SCHEME;
		this.values = (values != null) ? values : new HashMap<>();
	}

	@JsonProperty("propScheme")
	@JsonInclude(Include.NON_EMPTY)
	@KdonDoc("Scheme used to create propositional context from datatable (default is standard propositionalization scheme with all default propositionalization rules).")
	private PropositionalizationScheme propScheme() {
		// using deliberately '==' in order to write even equivalent scheme out if it
		// has been set
		return (propScheme == DEFAULT_PROP_SCHEME) ? null : propScheme;
	}

	@Override
	public Workspace build(ExecutionContext context) throws ValidationException {
		Optional<Path> inputPath = context.inputFile(dataFile);
		String datafilePath = inputPath.orElseThrow(
				() -> new ValidationException("could not find input file: " + dataFile, "check input directories"))
				.toString();
		DataTable table = XarfImport.xarfImport(datafilePath).get();
		PropositionalContext propContext = propScheme.apply(table);
		Workspace workspace=context.workspace(id);
		workspace.addAll(propContext);
		values.entrySet().forEach(e -> workspace.add(value(e.getKey(), e.getValue())));
		return workspace;
	}

	@Override
	public Identifier identifier() {
		return id;
	}

}

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
package de.unibonn.realkd.data.propositions;

import static de.unibonn.realkd.data.constraints.Constraints.equalTo;
import static de.unibonn.realkd.data.propositions.Propositions.proposition;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;

/**
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.7.0
 *
 */
@KdonTypeName("categoricEqualityPropRule")
@KdonDoc("Creates equality proposition for all categories of given categoric attribute; ignores ordinal attributes by default.")
public class CategoricEqualityPropositionalizationRule implements PropositionalizationRule {

	private static final Boolean DEFAULT_IGNORE_ORDINAL = TRUE;

	private static final CategoricEqualityPropositionalizationRule CATEGORIC_EQUALITY_IGNORING_ORDINAL = new CategoricEqualityPropositionalizationRule(
			TRUE);

	private static final CategoricEqualityPropositionalizationRule CATEGORIC_EQUALITY = new CategoricEqualityPropositionalizationRule(
			FALSE);

	public static CategoricEqualityPropositionalizationRule categoricEquality() {
		return CATEGORIC_EQUALITY_IGNORING_ORDINAL;
	}

	public static CategoricEqualityPropositionalizationRule categoricEqualityIncludingOrdinal() {
		return CATEGORIC_EQUALITY;
	}

	@JsonCreator
	public static CategoricEqualityPropositionalizationRule categoricEquality(
			@JsonProperty("ignoreOrdinal") Boolean ignoreOrdinal) {
		Boolean _ignoreOrdinal = (ignoreOrdinal == null) ? DEFAULT_IGNORE_ORDINAL : ignoreOrdinal;
		if (_ignoreOrdinal)
			return CATEGORIC_EQUALITY_IGNORING_ORDINAL;
		else
			return CATEGORIC_EQUALITY;
	}

	private final Boolean ignoreOrdinal;

	private final String stringRepresentation;

	private CategoricEqualityPropositionalizationRule(Boolean ignoreOrdinal) {
		this.ignoreOrdinal = ignoreOrdinal;
		this.stringRepresentation = ignoreOrdinal ? "CATEGORIC_EQUALITY" : "CATEGORIC_EQUALITY_INCL_ORDINAL";
	}

	@JsonProperty("ignoreOrdinal")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@KdonDoc("Whether to ignore ordinal attributes (default true).")
	private Boolean ignoreOrdinal() {
		return (DEFAULT_IGNORE_ORDINAL.equals(ignoreOrdinal)) ? null : ignoreOrdinal;
	}

	@Override
	public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
		List<AttributeBasedProposition<?>> result = new ArrayList<>();
		if (attribute instanceof CategoricAttribute && !(attribute instanceof OrdinalAttribute && ignoreOrdinal)) {
			for (Object category : ((CategoricAttribute<?>) attribute).categories()) {
				result.add(proposition(table, attribute, equalTo(category)));
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}

}

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

import java.util.Map;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.xarf.XarfParsing.StringToken;
import de.unibonn.realkd.data.xarf.XarfParsing.Token;

public class RelationDeclaration {

	public static final String DESCRIPTION_PARAMETER_NAME = "description";

	public static final String CAPTION_PARAMETER_NAME = "caption";

	public final Identifier id;

	public final String caption;

	public static RelationDeclaration DEFAULT_RELATION_DECLARATION = new RelationDeclaration(Identifier.id("datatable"),
			"datatable");

	RelationDeclaration(Identifier id, String caption) {
		this.id = id;
		this.caption = caption;
	}

	public static RelationDeclaration fromLine(String line) {
		Token[] tokens = XarfParsing.tokens(line);
		Map<String, Object> parameters = XarfParsing.parameters(tokens);
		Identifier id = Identifier.id(((StringToken) tokens[1]).value());
		String caption = parameters.containsKey(CAPTION_PARAMETER_NAME) ? (String) parameters.get(CAPTION_PARAMETER_NAME)
				: id.toString();
		return new RelationDeclaration(id, caption);
	}

}
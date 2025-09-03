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
package de.unibonn.realkd;

import static de.unibonn.realkd.algorithms.emm.ExceptionalSubgroupSampler.exceptionalSubgroupSampler;
import static de.unibonn.realkd.common.base.Identifier.identifier;
import static de.unibonn.realkd.common.workspace.Workspaces.workspace;
import static de.unibonn.realkd.data.propositions.Propositions.propositionalContext;
import static de.unibonn.realkd.data.xarf.XarfImport.xarfImport;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.junit.Test;

import de.unibonn.realkd.algorithms.emm.ExceptionalSubgroupSampler;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.Pattern;

/**
 * Runs the exceptional subgroup sampler on the titanic dataset with target
 * attribute "survived" and positive category "1".
 * 
 * @author Mario Boley
 * 
 * @since 0.5.1
 * 
 * @version 0.5.1
 *
 */
public class ExceptionalSubgroupSamplingTestTitanic {

	@Test
	public void runSubgroupSamplingOnTitanic() throws ValidationException {
		Workspace workspace = workspace();
		DataTable table = xarfImport("src/main/resources/data/titanic/titanic_1.0.0.xarf").get();
		PropositionalContext propositions = propositionalContext(table);
		workspace.addAll(table, propositions);

		Attribute<?> survived = table.attribute(identifier("survived")).get();
		ExceptionalSubgroupSampler sampler = exceptionalSubgroupSampler(workspace);
		sampler.targetAttributes(survived).useSingleEventModel().positiveCategory("1").numberOfResults(5)
				.numberOfSeeds(500);

		Collection<? extends Pattern<?>> subgroups = sampler.call();
		System.out.println(subgroups);

		assertNotNull(subgroups);
		assertFalse(subgroups.isEmpty());
	}

	public static void main(String[] args) throws ValidationException {
		new ExceptionalSubgroupSamplingTestTitanic().runSubgroupSamplingOnTitanic();
	}

}

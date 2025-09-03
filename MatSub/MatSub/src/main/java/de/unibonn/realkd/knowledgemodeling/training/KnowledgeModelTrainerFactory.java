/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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
package de.unibonn.realkd.knowledgemodeling.training;

import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.TableBasedPropositionalContext;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.discovery.DiscoveryProcess;
import de.unibonn.realkd.discovery.DiscoveryProcessState;

/**
 * Generate knowledge model trainer based on data workspace and a discovery
 * process state which created trainer observes.
 * 
 * @see #createKnowledgeModelTrainer(Workspace, DiscoveryProcessState)
 * 
 * @author Bo Kang
 *
 */
public class KnowledgeModelTrainerFactory {

	public static KnowledgeModelTrainerFactory INSTANCE = new KnowledgeModelTrainerFactory();

	private KnowledgeModelTrainerFactory() {
		;
	}

	/**
	 * <p>
	 * Creates a knowledge model trainer which maintains a knowledge model
	 * learner which in turn maintains a knowledge model.
	 * </p>
	 * 
	 * <p>
	 * User knowledge is assessed based on her actions as reflected in some
	 * given {@link DiscoveryProcess}.
	 * </p>
	 * 
	 * <p>
	 * NOTE: Knowledge modeling requires that the data work space exactly
	 * consists of one {@link DataTable} and one
	 * {@link TableBasedPropositionalContext} referring to that data table.
	 * </p>
	 * 
	 * @param dataWorkspace
	 *            workspace containing the data that modeled user's knowledge
	 *            refers to.
	 * @param discoveryProcessState
	 *            the state object maintained by the discovery process that
	 *            reflects user interactions.
	 * @return configured knowledge model trainer. 
	 */
	public KnowledgeModelTrainer createKnowledgeModelTrainer(
			Workspace dataWorkspace,
			DiscoveryProcessState discoveryProcessState) {
		// check whether data work space contains valid data table that is
		// compatible with knowledge model trainer.
		if (dataWorkspace.datatables().size() != 1) {
			throw new IllegalArgumentException(
					"Konwledge model trainer works with exactly one data table.");
		}
		DataTable dataTable = dataWorkspace.datatables().get(0);

		// check whether data work space contains valid proposition logic that
		// is compatible with knowledge model trainer.
		if (dataWorkspace.propositionalContexts().size() != 1
				&& dataWorkspace.propositionalContexts().get(0) instanceof TableBasedPropositionalContext
				&& ((TableBasedPropositionalContext) dataWorkspace
						.propositionalContexts().get(0)).getDatatable() == dataTable) {
			throw new IllegalArgumentException(
					"Konwledge model trainer only works with TableBasedPropositionalLogic referring to a unique data table in the workspace.");
		}
		TableBasedPropositionalContext propositionalLogic = (TableBasedPropositionalContext) dataWorkspace
				.propositionalContexts().get(0);

		return new KnowledgeModelTrainer(dataTable, propositionalLogic,
				discoveryProcessState);
	}
}

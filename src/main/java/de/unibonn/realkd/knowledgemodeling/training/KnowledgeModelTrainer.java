package de.unibonn.realkd.knowledgemodeling.training;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.propositions.TableBasedPropositionalContext;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.discovery.DiscoveryProcessObserver;
import de.unibonn.realkd.discovery.DiscoveryProcessState;
import de.unibonn.realkd.knowledgemodeling.constraints.FrequencyConstraint;
import de.unibonn.realkd.knowledgemodeling.learning.KnowledgeModelLearner;
import de.unibonn.realkd.patterns.Frequency;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.association.Associations;
import de.unibonn.realkd.patterns.logical.LogicalDescriptors;

/**
 * Translates user interface interaction into knowledge constraints and updates
 * for a knowledge model learner.
 * 
 * @author Bo Kang
 * 
 * @since 0.1.0
 * 
 * @version 0.1.0.1
 * 
 */
public class KnowledgeModelTrainer implements DiscoveryProcessObserver {

	// private static final Logger
	// LOGGER=Logger.getLogger(KnowledgeModelLearner.class.getName());

	private KnowledgeModelLearner knowledgeModelLearner;

	private DiscoveryProcessState discoveryProcessState;

	private Set<Pattern<?>> oldResultSnapShot;

	public KnowledgeModelTrainer(DataTable dataTable, TableBasedPropositionalContext propositionalLogic,
			DiscoveryProcessState discoveryProcessState) {
		this.knowledgeModelLearner = new KnowledgeModelLearner(dataTable);
		this.discoveryProcessState = discoveryProcessState;
		this.oldResultSnapShot = new HashSet<>();
		init(propositionalLogic);
	}

	private void init(TableBasedPropositionalContext propositionalLogic) {
		injectPriorKnowledge(propositionalLogic);
		knowledgeModelLearner.doUpdate();
	}

	private void injectPriorKnowledge(PropositionalContext propositionalLogic) {
		propositionalLogic.propositions().forEach(p -> {
			tellConstraintFor(propositionalLogic, p);
		});

	}

	/**
	 * converts an attribute based proposition into frequency constraint and
	 * tell this constraint to learner.
	 * 
	 */
	private void tellConstraintFor(PropositionalContext propositionalLogic, Proposition proposition) {
		List<Proposition> description = new ArrayList<>();
		description.add(proposition);

		FrequencyConstraint frequencyConstraint = new FrequencyConstraint(Associations.association(
				LogicalDescriptors.create(propositionalLogic.population(), description), ImmutableList.of()));

		// FrequencyConstraint frequencyConstraint = new FrequencyConstraint(
		// new
		// DefaultAssociationBuilder().apply(LogicalDescriptors.create(propositionalLogic,
		// description)));
		knowledgeModelLearner.tellConstraint(frequencyConstraint);
	}

	@Override
	public void justBeganNewRound() {
		updateResultSnapshot();
	}

	@Override
	public void roundEnded() {
		Set<Pattern<?>> newAddedPatterns = getAddedToResultThisRound();
		for (Pattern<?> p : newAddedPatterns) {
			if (p.hasMeasure(Frequency.FREQUENCY)) {
				knowledgeModelLearner.tellConstraint(new FrequencyConstraint(p));
			}
		}
		knowledgeModelLearner.doUpdate();
	}

	private Set<Pattern<?>> getAddedToResultThisRound() {
		Set<Pattern<?>> newResults = new HashSet<>(discoveryProcessState.resultPatterns());
		newResults.removeAll(oldResultSnapShot);
		return newResults;
	}

	private void updateResultSnapshot() {
		oldResultSnapShot.clear();
		oldResultSnapShot.addAll(discoveryProcessState.resultPatterns());
	}

	public KnowledgeModelLearner getKnowledgeModelLearner() {
		return knowledgeModelLearner;
	}

	@Override
	public void markAsSeen(Pattern<?> p) {
		;
	}

	@Override
	public void aboutToSave(Pattern<?> p) {
		;
	}

	@Override
	public void aboutToDeleteFromCandidates(Pattern<?> p) {
		;
	}

	@Override
	public void aboutToDeletePatternFromResults(Pattern<?> p) {
		;
	}
}

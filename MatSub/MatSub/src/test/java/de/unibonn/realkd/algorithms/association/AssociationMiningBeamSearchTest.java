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
package de.unibonn.realkd.algorithms.association;

import static de.unibonn.realkd.common.IndexSets.copyOf;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.common.workspace.Workspaces;
import de.unibonn.realkd.data.Populations;
import de.unibonn.realkd.data.propositions.DefaultPropositionalContext;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.propositions.Propositions;
import de.unibonn.realkd.data.table.DataFormatException;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.association.Associations;
import de.unibonn.realkd.patterns.logical.Lift;
import de.unibonn.realkd.patterns.logical.LogicalDescriptors;

/**
 * @author Mario Boley
 * @author Sandy Moens
 *
 */
public class AssociationMiningBeamSearchTest {

	private static Workspace workspace;
	private static Workspace workspace2;

	@BeforeClass
	public static void createData() throws DataFormatException {
		{
			Proposition prop0 = Propositions.proposition(0, copyOf(ImmutableSet.of(0, 1, 2)));
			Proposition prop1 = Propositions.proposition(1, copyOf(ImmutableSet.of(0, 1, 3)));
			Proposition prop2 = Propositions.proposition(2, copyOf(ImmutableSet.of(0, 2)));

			workspace = Workspaces.workspace();
			workspace.add(new DefaultPropositionalContext("TestData", "", Populations.population(Identifier.id("TestData_population"),
					"TestData population", "", ImmutableList.of("t0", "t1", "t2", "t3")),
					ImmutableList.of(prop0, prop1, prop2)));
		}

		{
			// Creates the following data
			// t0 : 0 1 2
			// t1 : 0 1 2
			// t2 : 0 1 2
			// t3 : 0 1
			// t4 : 0 1
			// t5 : 0 1
			// t6 : 0 1
			// t7 : 0 2
			// t8 : 0 2
			// t9 : 0
			// t10: 1 2
			Proposition prop0 = Propositions.proposition(0, copyOf(ImmutableSet.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
			Proposition prop1 = Propositions.proposition(1, copyOf(ImmutableSet.of(0, 1, 2, 3, 4, 5, 6, 10)));
			Proposition prop2 = Propositions.proposition(2, copyOf(ImmutableSet.of(0, 1, 2, 7, 8, 10)));

			workspace2 = Workspaces.workspace();
			workspace2.add(new DefaultPropositionalContext("TestData2", "",
					Populations.population(Identifier.id("TestData2_population"), "TestData2 population", "",
							ImmutableList.of("t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9", "t10")),
					ImmutableList.of(prop0, prop1, prop2)));
		}
	}

	@Test
	public void test() throws ValidationException {
		AssociationMiningBeamSearch associationMiningBeamSearch = new AssociationMiningBeamSearch(workspace);
		associationMiningBeamSearch.findParameterByName("Number of results").setByString("1");
		// associationMiningBeamSearch.getOptimizationOrderParameter().set(
		// Association.POSITIVE_LIFT_COMPARATOR);
		associationMiningBeamSearch.findParameterByName(AssociationTargetFunctionParameter.NAME).setByString("lift");
		// .setComparator(Association.POSITIVE_LIFT_COMPARATOR);
		Collection<Pattern<?>> resultPatterns = associationMiningBeamSearch.call();
		assertTrue(resultPatterns.size() == 1);
		Pattern<?> pattern2 = resultPatterns.iterator().next();
		assertTrue("Pattern must have lift measure bound", pattern2.hasMeasure(Lift.LIFT));
		assertTrue("Pattern must have positive lift", pattern2.value(Lift.LIFT) > 0);
	}

	private Collection<Pattern<?>> runBeamSearch(Workspace workspace, int beamWidth, int numberOfResults,
			String targetName) throws ValidationException {
		AssociationMiningBeamSearch associationMiningBeamSearch = new AssociationMiningBeamSearch(workspace);
		associationMiningBeamSearch.findParameterByName("Beam width").setByString(String.valueOf(beamWidth));
		associationMiningBeamSearch.findParameterByName("Number of results")
				.setByString(String.valueOf(numberOfResults));
		associationMiningBeamSearch.findParameterByName(AssociationTargetFunctionParameter.NAME)
				.setByString(targetName);
		return associationMiningBeamSearch.call();
	}

	private void checkExpectedVsResultPattern(int numberOfResults, Collection<Pattern<?>> expectedResults,
			Collection<Pattern<?>> resultPatterns) {
		assertTrue("Incorrect number of results. Expected: " + numberOfResults + ", but was: " + resultPatterns.size(),
				resultPatterns.size() == numberOfResults);
		assertTrue("Result output collection does not match. Expected: " + expectedResults + ", but was: "
				+ resultPatterns, expectedResults.containsAll(resultPatterns));
	}

	@Test
	public void testWorkspace2BeamWidth1NumberOfResults1() throws ValidationException {
		PropositionalContext logic = workspace2.propositionalContexts().get(0);

		Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace2, 1, 1, "frequency");

		List<Pattern<?>> expectedResults = new ArrayList<>();
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(0)))));

		checkExpectedVsResultPattern(1, expectedResults, resultPatterns);
	}

	@Test
	public void testWorkspace2BeamWidth1NumberOfResults2() throws ValidationException {
		PropositionalContext logic = workspace2.propositionalContexts().get(0);

		Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace2, 1, 2, "frequency");

		List<Pattern<?>> expectedResults = new ArrayList<>();
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(0)))));
		expectedResults.add(Associations.association(LogicalDescriptors.create(logic.population(),
				ImmutableList.of(logic.proposition(0), logic.proposition(1)))));

		checkExpectedVsResultPattern(2, expectedResults, resultPatterns);
	}

	@Test
	public void testWorkspace2BeamWidth1NumberOfResults3() throws ValidationException {
		PropositionalContext logic = workspace2.propositionalContexts().get(0);

		Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace2, 1, 3, "frequency");

		List<Pattern<?>> expectedResults = new ArrayList<>();
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(0)))));
		expectedResults.add(Associations.association(LogicalDescriptors.create(logic.population(),
				ImmutableList.of(logic.proposition(0), logic.proposition(1)))));
		expectedResults.add(Associations.association(LogicalDescriptors.create(logic.population(),
				ImmutableList.of(logic.proposition(0), logic.proposition(1), logic.proposition(2)))));

		checkExpectedVsResultPattern(3, expectedResults, resultPatterns);
	}

	@Test
	public void testWorkspace2BeamWidth1NumberOfResults4() throws ValidationException {
		PropositionalContext logic = workspace2.propositionalContexts().get(0);

		Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace2, 1, 4, "frequency");

		List<Pattern<?>> expectedResults = new ArrayList<>();
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(0)))));
		expectedResults.add(Associations.association(LogicalDescriptors.create(logic.population(),
				ImmutableList.of(logic.proposition(0), logic.proposition(1)))));
		expectedResults.add(Associations.association(LogicalDescriptors.create(logic.population(),
				ImmutableList.of(logic.proposition(0), logic.proposition(1), logic.proposition(2)))));

		checkExpectedVsResultPattern(3, expectedResults, resultPatterns);
	}

	@Test
	public void testWorkspace2BeamWidth2NumberOfResults1() throws ValidationException {
		PropositionalContext logic = workspace2.propositionalContexts().get(0);

		Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace2, 2, 1, "frequency");

		List<Pattern<?>> expectedResults = new ArrayList<>();
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(0)))));

		checkExpectedVsResultPattern(1, expectedResults, resultPatterns);
	}

	@Test
	public void testWorkspace2BeamWidth2NumberOfResults2() throws ValidationException {
		PropositionalContext logic = workspace2.propositionalContexts().get(0);

		Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace2, 2, 2, "frequency");

		List<Pattern<?>> expectedResults = new ArrayList<>();
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(0)))));
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(1)))));

		checkExpectedVsResultPattern(2, expectedResults, resultPatterns);
	}

	@Test
	public void testWorkspace2BeamWidth2NumberOfResults3() throws ValidationException {
		PropositionalContext logic = workspace2.propositionalContexts().get(0);

		Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace2, 2, 3, "frequency");

		List<Pattern<?>> expectedResults = new ArrayList<>();
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(0)))));
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(1)))));
		expectedResults.add(Associations.association(LogicalDescriptors.create(logic.population(),
				ImmutableList.of(logic.proposition(0), logic.proposition(1)))));

		checkExpectedVsResultPattern(3, expectedResults, resultPatterns);
	}

	@Test
	public void testWorkspace2BeamWidth2NumberOfResults4() throws ValidationException {
		PropositionalContext logic = workspace2.propositionalContexts().get(0);

		Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace2, 2, 4, "frequency");

		List<Pattern<?>> expectedResults = new ArrayList<>();
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(0)))));
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(1)))));
		expectedResults.add(Associations.association(LogicalDescriptors.create(logic.population(),
				ImmutableList.of(logic.proposition(0), logic.proposition(1)))));
		expectedResults.add(Associations.association(LogicalDescriptors.create(logic.population(),
				ImmutableList.of(logic.proposition(0), logic.proposition(2)))));

		checkExpectedVsResultPattern(4, expectedResults, resultPatterns);
	}

	@Test
	public void testWorkspace2BeamWidth3NumberOfResults1() throws ValidationException {
		PropositionalContext logic = workspace2.propositionalContexts().get(0);

		Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace2, 3, 1, "frequency");

		List<Pattern<?>> expectedResults = new ArrayList<>();
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(0)))));

		checkExpectedVsResultPattern(1, expectedResults, resultPatterns);
	}

	@Test
	public void testWorkspace2BeamWidth3NumberOfResults2() throws ValidationException {
		PropositionalContext logic = workspace2.propositionalContexts().get(0);

		Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace2, 3, 2, "frequency");

		List<Pattern<?>> expectedResults = new ArrayList<>();
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(0)))));
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(1)))));

		checkExpectedVsResultPattern(2, expectedResults, resultPatterns);
	}

	@Test
	public void testWorkspace2BeamWidth3NumberOfResults3() throws ValidationException {
		PropositionalContext logic = workspace2.propositionalContexts().get(0);

		Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace2, 3, 3, "frequency");

		List<Pattern<?>> expectedResults = new ArrayList<>();
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(0)))));
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(1)))));
		expectedResults.add(Associations.association(LogicalDescriptors.create(logic.population(),
				ImmutableList.of(logic.proposition(0), logic.proposition(1)))));

		checkExpectedVsResultPattern(3, expectedResults, resultPatterns);
	}

	@Test
	public void testWorkspace2BeamWidth3NumberOfResults4() throws ValidationException {
		PropositionalContext logic = workspace2.propositionalContexts().get(0);

		Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace2, 3, 4, "frequency");

		List<Pattern<?>> expectedResults = new ArrayList<>();
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(0)))));
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(1)))));
		expectedResults.add(Associations
				.association(LogicalDescriptors.create(logic.population(), ImmutableList.of(logic.proposition(2)))));
		expectedResults.add(Associations.association(LogicalDescriptors.create(logic.population(),
				ImmutableList.of(logic.proposition(0), logic.proposition(1)))));

		checkExpectedVsResultPattern(4, expectedResults, resultPatterns);
	}

	// @Test
	// public void testWorkspaceBeamWidth1NumberOfResults1() {
	// PropositionalLogic logic = workspace.getAllPropositionalLogics().get(0);
	//
	// Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace, 1, 1,
	// "frequency");
	//
	// List<Pattern<?>> expectedResults = new ArrayList<>();
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0)))));
	//
	// checkExpectedVsResultPattern(1, expectedResults, resultPatterns);
	// }
	//
	// @Test
	// public void testWorkspaceBeamWidth1NumberOfResults2() {
	// PropositionalLogic logic = workspace.getAllPropositionalLogics().get(0);
	//
	// Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace, 1, 2,
	// "frequency");
	//
	// List<Pattern<?>> expectedResults = new ArrayList<>();
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0), logic.proposition(1)))));
	//
	// checkExpectedVsResultPattern(2, expectedResults, resultPatterns);
	// }
	//
	// @Test
	// public void testWorkspaceBeamWidth1NumberOfResults3() {
	// PropositionalLogic logic = workspace.getAllPropositionalLogics().get(0);
	//
	// Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace, 1, 3,
	// "frequency");
	//
	// List<Pattern<?>> expectedResults = new ArrayList<>();
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0), logic.proposition(1)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0), logic.proposition(1),
	// logic.proposition(2)))));
	//
	// checkExpectedVsResultPattern(3, expectedResults, resultPatterns);
	// }
	//
	// @Test
	// public void testWorkspaceBeamWidth1NumberOfResults4() {
	// PropositionalLogic logic = workspace.getAllPropositionalLogics().get(0);
	//
	// Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace, 1, 4,
	// "frequency");
	//
	// List<Pattern<?>> expectedResults = new ArrayList<>();
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0), logic.proposition(1)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0), logic.proposition(1),
	// logic.proposition(2)))));
	//
	// checkExpectedVsResultPattern(3, expectedResults, resultPatterns);
	// }
	//
	// @Test
	// public void testWorkspaceBeamWidth2NumberOfResults1() {
	// PropositionalLogic logic = workspace.getAllPropositionalLogics().get(0);
	//
	// Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace, 2, 1,
	// "frequency");
	//
	// List<Pattern<?>> expectedResults = new ArrayList<>();
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(1)))));
	//
	// checkExpectedVsResultPattern(1, expectedResults, resultPatterns);
	// }
	//
	// @Test
	// public void testWorkspaceBeamWidth2NumberOfResults2() {
	// PropositionalLogic logic = workspace.getAllPropositionalLogics().get(0);
	//
	// Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace, 2, 2,
	// "frequency");
	//
	// List<Pattern<?>> expectedResults = new ArrayList<>();
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(1)))));
	//
	// checkExpectedVsResultPattern(2, expectedResults, resultPatterns);
	// }
	//
	// @Test
	// public void testWorkspaceBeamWidth2NumberOfResults3() {
	// PropositionalLogic logic = workspace.getAllPropositionalLogics().get(0);
	//
	// Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace, 2, 3,
	// "frequency");
	//
	// List<Pattern<?>> expectedResults = new ArrayList<>();
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(1)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0), logic.proposition(1)))));
	//
	// checkExpectedVsResultPattern(3, expectedResults, resultPatterns);
	// }
	//
	// @Test
	// public void testWorkspaceBeamWidth2NumberOfResults4() {
	// PropositionalLogic logic = workspace.getAllPropositionalLogics().get(0);
	//
	// Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace, 2, 4,
	// "frequency");
	//
	// List<Pattern<?>> expectedResults = new ArrayList<>();
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(1)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0), logic.proposition(1)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0), logic.proposition(2)))));
	//
	// checkExpectedVsResultPattern(4, expectedResults, resultPatterns);
	// }
	//
	// @Test
	// public void testWorkspaceBeamWidth3NumberOfResults1() {
	// PropositionalLogic logic = workspace.getAllPropositionalLogics().get(0);
	//
	// Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace, 3, 1,
	// "frequency");
	//
	// List<Pattern<?>> expectedResults = new ArrayList<>();
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0)))));
	//
	// checkExpectedVsResultPattern(1, expectedResults, resultPatterns);
	// }
	//
	// @Test
	// public void testWorkspaceBeamWidth3NumberOfResults2() {
	// PropositionalLogic logic = workspace.getAllPropositionalLogics().get(0);
	//
	// Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace, 3, 2,
	// "frequency");
	//
	// List<Pattern<?>> expectedResults = new ArrayList<>();
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(1)))));
	//
	// checkExpectedVsResultPattern(2, expectedResults, resultPatterns);
	// }
	//
	// @Test
	// public void testWorkspaceBeamWidth3NumberOfResults3() {
	// PropositionalLogic logic = workspace.getAllPropositionalLogics().get(0);
	//
	// Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace, 3, 3,
	// "frequency");
	//
	// List<Pattern<?>> expectedResults = new ArrayList<>();
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(1)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0), logic.proposition(2)))));
	//
	// checkExpectedVsResultPattern(3, expectedResults, resultPatterns);
	// }
	//
	// @Test
	// public void testWorkspaceBeamWidth3NumberOfResults4() {
	// PropositionalLogic logic = workspace.getAllPropositionalLogics().get(0);
	//
	// Collection<Pattern<?>> resultPatterns = runBeamSearch(workspace, 3, 4,
	// "frequency");
	//
	// List<Pattern<?>> expectedResults = new ArrayList<>();
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(1)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0), logic.proposition(1)))));
	// expectedResults.add(Associations.association(LogicalDescriptors.create(logic,
	// ImmutableList.of(logic.proposition(0), logic.proposition(2)))));
	//
	// checkExpectedVsResultPattern(4, expectedResults, resultPatterns);
	// }

}

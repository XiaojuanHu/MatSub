package de.unibonn.realkd.knowledgemodeling.learning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.knowledgemodeling.UserKnowledgeModel;
import de.unibonn.realkd.knowledgemodeling.constraints.MaxEntConstraint;

/**
 * @author bkang
 */
public class KnowledgeModelLearner {

	private List<MaxEntConstraint> constraints;

	private UserKnowledgeModel userKnowledgeModel;

	private List<ConnectedComponent> connectedComponents;

	/*
	 * TODO: where should those go?
	 */
	// private final double ALPHA = 0.1;
	//
	// private final double BETA = 0.8;
	//
	// private final double EPSILON = 0.001;

	public KnowledgeModelLearner(DataTable dataTable) {
		this.constraints = new ArrayList<>();
		this.userKnowledgeModel = new UserKnowledgeModel(dataTable, connectedComponents);
		this.connectedComponents = new ArrayList<>();
	}

	public void doUpdate() {
		if (constraints.size() != 0) {
			updateConnectedComponents();
			for (ConnectedComponent connectedComponent : connectedComponents) {
				computeOptimalMultiplierForConnectedComponent(connectedComponent);
			}
			userKnowledgeModel.update(constraints, connectedComponents);
		}
	}

	private void computeOptimalMultiplierForConnectedComponent(ConnectedComponent connectedComponent) {
		OptimizationFunction optimizationFunction = new OptimizationFunction(connectedComponent);
		double[] x = new double[connectedComponent.getConstraints().size()];
		// Lower and upper bounds: 1st row is lower bounds, 2nd is upper
		double[][] bounds = new double[2][connectedComponent.getConstraints().size()];
		for (int i = 0; i < connectedComponent.getConstraints().size(); i++) {
			bounds[0][i] = Double.NEGATIVE_INFINITY;
			bounds[1][i] = Double.POSITIVE_INFINITY;
		}

		// Find the minimum, 200 iterations as default
		try {
			x = optimizationFunction.findArgmin(x, bounds);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < connectedComponent.getConstraints().size(); i++) {
			connectedComponent.getConstraints().get(i).updateMultiplier(x[i]);
		}

	}

	private void updateConnectedComponents() {
		RealMatrix adjacencyMatrix = computeAdjacencyMatrix();
		List<List<Integer>> connectedComponentsIndices = computeIndicesOfConnectedComponents(adjacencyMatrix);
		this.connectedComponents = constructConnectedComponents(connectedComponentsIndices);
	}

	private RealMatrix computeAdjacencyMatrix() {
		RealMatrix adjacencyMatrix = new Array2DRowRealMatrix(constraints.size(), constraints.size());
		for (int i = 0; i < constraints.size(); i++) {
			for (int j = i + 1; j < constraints.size(); j++) {
				if (isOverlapped(constraints.get(i), constraints.get(j))) {
					adjacencyMatrix.setEntry(i, j, 1.);
					adjacencyMatrix.setEntry(j, i, 1.);
				}
			}
		}
		return adjacencyMatrix;
	}

	private boolean isOverlapped(MaxEntConstraint constraintI, MaxEntConstraint constraintJ) {
		Set<Integer> intersection = new HashSet<>(constraintI.getAttributeIndices());
		intersection.retainAll(constraintJ.getAttributeIndices());
		return (intersection.size() != 0);
	}

	List<List<Integer>> computeIndicesOfConnectedComponents(RealMatrix adjacencyMatrix) {
		int numberOfConnectedComponents = 0;
		int numberOfVertices = adjacencyMatrix.getColumnDimension();

		int[] mark = new int[numberOfVertices];

		for (int i = 0; i < numberOfVertices; i++) {
			if (mark[i] == 0) {
				numberOfConnectedComponents++;
				Queue<Integer> queue = new LinkedList<>();
				queue.add(i);
				mark[i] = numberOfConnectedComponents;
				while (!queue.isEmpty()) {
					int current = queue.remove();
					for (int j = 0; j < numberOfVertices; j++) {
						if (adjacencyMatrix.getEntry(current, j) == 1 && mark[j] == 0) {
							mark[j] = numberOfConnectedComponents;
							queue.add(j);
						}
					}
				}
			}
		}

		List<List<Integer>> indicesOfConnectedComponents = new ArrayList<>();
		for (int i = 0; i < numberOfConnectedComponents; i++) {
			indicesOfConnectedComponents.add(new ArrayList<Integer>());
		}

		for (int i = 0; i < numberOfVertices; i++) {
			indicesOfConnectedComponents.get(mark[i] - 1).add(i);
		}

		return indicesOfConnectedComponents;
	}

	private List<ConnectedComponent> constructConnectedComponents(List<List<Integer>> connectedComponentsIndices) {
		List<ConnectedComponent> connectedComponents = new ArrayList<>();
		for (List<Integer> indices : connectedComponentsIndices) {
			Set<MaxEntConstraint> constraintSet = new HashSet<>();
			for (Integer index : indices) {
				constraintSet.add(constraints.get(index));
			}
			connectedComponents.add(new ConnectedComponent(new ArrayList<>(constraintSet)));
		}
		return connectedComponents;
	}

	public void tellConstraint(MaxEntConstraint constraint) {
		this.constraints.add(constraint);
	}

	/*
	 * private void updateLambdas(RealVector newLambdas) { for (MaxEntConstraint
	 * constraint : constraints) {
	 * constraint.updateMultiplier(newLambdas.getEntry
	 * (constraints.indexOf(constraint))); } }
	 */

	public List<ConnectedComponent> getConnectedComponents() {
		return this.connectedComponents;
	}

	public UserKnowledgeModel getUserKnowledgeModel() {
		return this.userKnowledgeModel;
	}
}

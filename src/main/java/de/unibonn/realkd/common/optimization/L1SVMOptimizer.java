package de.unibonn.realkd.common.optimization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import de.unibonn.realkd.common.base.Pair;

/**
 * @author Pavel Tokmakov
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 * 
 */
public class L1SVMOptimizer<T> implements
		RegressionModelFromPreferenceLearner<T> {

	private static final double C = 0.1;

	private List<Pair<T, T>> trainingData;

	private PreferenceSupportVectorBasedRegressionModel<T> model;

	public L1SVMOptimizer(Kernel<T> kernel) {
		trainingData = new ArrayList<>();
		model = new PreferenceSupportVectorBasedRegressionModel<T>(kernel);
	}

	@Override
	public void tellPreference(T superior, T inferior) {
		Pair<T, T> pair = Pair.pair(superior, inferior);

		trainingData.add(pair);
	}

	@Override
	public void doUpdate() {
		SimplexSolver solver = new SimplexSolver();
		PointValuePair solution;

		try {
			solution = solver.optimize(initObjective(), initConstraints(),
					GoalType.MINIMIZE);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		updateModel(solution);
	}

	private void updateModel(PointValuePair solution) {
		model.getCoefficients().clear();
		model.getSupportVectors().clear();

		for (int i = 0; i < trainingData.size(); i++) {
			if (solution.getPoint()[i] != 0.) {
				model.getCoefficients().add(solution.getPoint()[i]);
				model.getSupportVectors().add(trainingData.get(i));
			}
		}
	}

	private LinearConstraintSet initConstraints() {
		Collection<LinearConstraint> constraints = new ArrayList<>();

		for (int i = 0; i < trainingData.size(); i++) {
			constraints.add(initConstraint(i));
			constraints.add(initConstraintForAlpha(i));
			constraints.add(initConstraintForSlack(i));
		}

		return new LinearConstraintSet(constraints);
	}

	private LinearConstraint initConstraintForAlpha(int index) {
		double[] coeffs = new double[trainingData.size() * 2];

		for (int i = 0; i < trainingData.size(); i++) {
			if (i == index) {
				coeffs[i] = 1;
			} else {
				coeffs[i] = 0;
			}

			coeffs[trainingData.size() + i] = 0;
		}

		return new LinearConstraint(coeffs, Relationship.GEQ, 0);
	}

	private LinearConstraint initConstraintForSlack(int index) {
		double[] coeffs = new double[trainingData.size() * 2];

		for (int i = 0; i < trainingData.size(); i++) {
			if (i == index) {
				coeffs[trainingData.size() + i] = 1;
			} else {
				coeffs[trainingData.size() + i] = 0;
			}

			coeffs[i] = 0;
		}

		return new LinearConstraint(coeffs, Relationship.GEQ, 0);
	}

	private LinearConstraint initConstraint(int constrIndex) {
		double[] coeffs = new double[trainingData.size() * 2];
		Pair<T, T> constrInstance = trainingData.get(constrIndex);

		for (int i = 0; i < trainingData.size(); i++) {
			Pair<T, T> coeffInstance = trainingData.get(i);

			coeffs[i] = model.getFeatureSpace().innerProduct(
					coeffInstance._1(), constrInstance._1())
					- model.getFeatureSpace().innerProduct(
							coeffInstance._1(), constrInstance._2())
					- model.getFeatureSpace().innerProduct(
							coeffInstance._2(), constrInstance._1())
					+ model.getFeatureSpace().innerProduct(
							coeffInstance._2(), constrInstance._2());

			if (i == constrIndex) {
				coeffs[trainingData.size() + i] = -1.;
			} else {
				coeffs[trainingData.size() + i] = 0.;
			}
		}

		return new LinearConstraint(coeffs, Relationship.GEQ, 1);
	}

	private LinearObjectiveFunction initObjective() {
		double[] coeffs = new double[trainingData.size() * 2];

		for (int i = 0; i < trainingData.size(); i++) {
			coeffs[i] = 1.;
			coeffs[trainingData.size() + i] = C;
		}

		return new LinearObjectiveFunction(coeffs, 0);
	}

	public InnerProductSpaceBasedRegressionModel<T> getModel() {
		return model;
	}
}

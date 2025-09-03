package de.unibonn.realkd.common.optimization;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.realkd.common.base.Pair;
import de.unibonn.realkd.common.inspection.Inspectable;
import de.unibonn.realkd.common.inspection.InspectableObjectState;
import de.unibonn.realkd.common.math.types.InnerProductSpace;

/**
 * 
 * Maintains a set of preference pairs and co-efficients and computes regression
 * value based on kernel-expansion with the difference of pairs fixed as first
 * component.
 * 
 * @author Pavel Tokmakov
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 * 
 */
public class PreferenceSupportVectorBasedRegressionModel<T> implements
		InnerProductSpaceBasedRegressionModel<T>, Inspectable {

	private class SupportVectorModelInspectableState implements
			InspectableObjectState {

		@Override
		public List<List<String>> getTable() {
			List<List<String>> table = new ArrayList<List<String>>();
			List<String> temp;
			for (int i = 0; i < supportVectors.size(); i++) {
				temp = new ArrayList<String>();
				temp.add(supportVectors.get(i)._1() + " PREFERRED TO "
						+ supportVectors.get(i)._2());
				temp.add(String.valueOf(coefficients.get(i)));
				table.add(temp);
			}
			return table;
		}

		@Override
		public List<String> getHeader() {
			List<String> header = new ArrayList<String>();
			header.add("Pair");
			header.add("Coefficient");
			return header;
		}

	}

	private SupportVectorModelInspectableState supportVectorModelState = new SupportVectorModelInspectableState();

	private List<Pair<T, T>> supportVectors;

	private List<Double> coefficients;

	private KernelInducedFeatureSpace<T> featureSpace;

	public PreferenceSupportVectorBasedRegressionModel(Kernel<T> kernel) {
		supportVectors = new ArrayList<>();
		coefficients = new ArrayList<>();
		featureSpace = new KernelInducedFeatureSpace<T>(kernel);
	}

	public PreferenceSupportVectorBasedRegressionModel(
			PreferenceSupportVectorBasedRegressionModel<T> model) {
		supportVectors = new ArrayList<>();
		coefficients = new ArrayList<>();
		featureSpace = (KernelInducedFeatureSpace<T>) model.getFeatureSpace();
	}

	public synchronized double value(T p) {
		double result = 0;

		for (int i = 0; i < coefficients.size(); i++) {
			double component = coefficients.get(i)
					* (featureSpace.innerProduct(
							supportVectors.get(i)._1(), p) - featureSpace
							.innerProduct(supportVectors.get(i)._2(), p));
			result += component;
		}

		return result;
	}

	public synchronized double pairNorm(Pair<T, T> p) {
		return featureSpace.innerProduct(p._1(), p._1()) - 2
				* featureSpace.innerProduct(p._1(), p._2())
				+ featureSpace.innerProduct(p._2(), p._2());
	}

	public synchronized double pairInnerProd(Pair<T, T> p1, Pair<T, T> p2) {
		return featureSpace.innerProduct(p1._1(), p2._1())
				- featureSpace.innerProduct(p1._1(), p2._2())
				- featureSpace.innerProduct(p1._2(), p2._1())
				+ featureSpace.innerProduct(p1._2(), p2._2());
	}

	public synchronized InnerProductSpace<T> getFeatureSpace() {
		return featureSpace;
	}

	public synchronized List<Pair<T, T>> getSupportVectors() {
		return supportVectors;
	}

	public synchronized List<Double> getCoefficients() {
		return coefficients;
	}

	public String toString() {
		return this.getClass().toString();
	}

	@Override
	public InspectableObjectState getInspectableState() {
		return this.supportVectorModelState;
	}

	@Override
	public String caption() {
		return "Support Vector Utility Model";
	}

	@Override
	public List<Inspectable> getSubInspectables() {
		return new ArrayList<>();
	}

	@Override
	public String description() {
		return "";
	}
}

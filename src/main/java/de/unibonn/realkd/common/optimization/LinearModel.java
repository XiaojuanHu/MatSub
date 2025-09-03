package de.unibonn.realkd.common.optimization;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.realkd.common.inspection.Inspectable;
import de.unibonn.realkd.common.inspection.InspectableObjectState;

public class LinearModel<T> implements
		InnerProductSpaceBasedRegressionModel<T>, Inspectable {

	private class LinearModelInspectableState implements InspectableObjectState {

		@Override
		public List<List<String>> getTable() {
			List<List<String>> table = new ArrayList<List<String>>();
			List<String> row;
			for (int i = 0; i < featureSpace.getFeatures().size(); i++) {
				row = new ArrayList<String>();
				LinearFeature<T> feature = featureSpace.getFeatures().get(i);
				row.add((feature instanceof PatternFeature) ? ((PatternFeature) feature)
						.getDescription() : feature.toString());
				row.add(String.valueOf(weights.get(i)));
				row.add((feature instanceof PatternFeature) ? ((PatternFeature) feature)
						.getCategory().toString() : "");
				table.add(row);
			}
			return table;
		}

		@Override
		public List<String> getHeader() {
			List<String> header = new ArrayList<String>();
			header.add("Description");
			header.add("Weight");
			header.add("Category");
			return header;
		}

	}

	private LinearModelInspectableState linearModelState = new LinearModelInspectableState();

	private LinearFeatureSpace<T> featureSpace;

	private List<Double> weights;

	public LinearModel(LinearFeatureSpace<T> space) {
		this.featureSpace = space;
		this.weights = new ArrayList<>(space.getFeatures().size());
		// this.featureSpace = new LinearFeatureSpace<T>(features, zeroElement);
		for (LinearFeature<T> feature : space.getFeatures()) {
			this.addFeature(feature);
		}
	}

	public List<Double> getWeights() {
		return weights;
	}

	public double value(T p) {
		double res = 0.0;

		for (int i = 0; i < this.featureSpace.getFeatures().size(); i++) {
			res += this.featureSpace.getFeatures().get(i).value(p)
					* weights.get(i);
		}

		return res;
	}

	public LinearFeatureSpace<T> getFeatureSpace() {
		return featureSpace;
	}

	private void addFeature(LinearFeature<T> feature) {
		this.weights.add(feature.getDefaultCoefficient());
	}

	@Override
	public InspectableObjectState getInspectableState() {
		return this.linearModelState;
	}

	@Override
	public String caption() {
		return "Linear Utility Model";
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

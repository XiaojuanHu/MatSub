/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
 */

package de.unibonn.realkd.algorithms.outlier;

import static de.unibonn.realkd.common.IndexSets.copyOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.common.MiningParameters;
import de.unibonn.realkd.algorithms.emm.EMMParameters;
//import de.unibonn.realkd.algorithms.common.FreePropositionalLogicParameter;
import de.unibonn.realkd.algorithms.outlier.libsvm.svm;
import de.unibonn.realkd.algorithms.outlier.libsvm.svm_model;
import de.unibonn.realkd.algorithms.outlier.libsvm.svm_node;
import de.unibonn.realkd.algorithms.outlier.libsvm.svm_parameter;
import de.unibonn.realkd.algorithms.outlier.libsvm.svm_problem;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.outlier.Outlier;

/**
 * 
 * @author Sebastian Bothe
 * 
 */
public class OneClassModelMiner extends AbstractMiningAlgorithm {

	private Parameter<DataTable> dataTableParameter;

	public OneClassModelMiner(Workspace workspace) {
		super();

		dataTableParameter = MiningParameters.dataTableParameter(workspace);
		targetAttrParam = EMMParameters.getEMMTargetAttributesParameter(dataTableParameter,
				attribute -> attribute instanceof MetricAttribute);

		nuParamter = new FractionOfOutliersParameter(this);
		sigmaParameter = new SigmaParameter(this);

		registerParameter(dataTableParameter);
		registerParameter(nuParamter);
		registerParameter(sigmaParameter);
		registerParameter(targetAttrParam);
	}

	// Parameters for this algorithm
	private final FractionOfOutliersParameter nuParamter;
	private final Parameter<List<Attribute<?>>> targetAttrParam;
	private final SigmaParameter sigmaParameter;

	private static final double EPSILON = 0.000000000001d;

	// private static double GAMMA = 0.01d;

	@Override
	public String caption() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String description() {
		return "One class svm learner.";
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.OUTLIER_DETECTION;
	}

	/*
	 * @Override public List<MiningParameter> getParameters() { if (myParams ==
	 * null) { myParams = new ArrayList<>(); myParams.add(nu);
	 * myParams.add(sigma); myParams.add(targetAttrParam); } return myParams; }
	 */

	@Override
	protected Collection<Pattern<?>> concreteCall() {

		DataTable dt = this.getDataTable();

		// Determine indices for target attrs and make sure they are numeric
		List<String> names = dt.attributeNames();
		List<Integer> idxs = new ArrayList<>();

		for (Attribute<?> attribute : this.targetAttrParam.current()) {
			int idx = names.indexOf(attribute.caption());
			// Check.isTrue(dt.isNumeric(idx));
			idxs.add(idx);
		}

		int numExamples = dt.population().size();
		int numDims = idxs.size();

		// Construct the training data array
		svm_node[][] svmTrainData = generateSvmTrainData(dt, idxs, numExamples, numDims);

		double gamma;
		double sigma2;
		// Indicator value is -1, if sigma param is set to this, we determine
		// it's value here
		if (sigmaParameter.current() == 0) {
			sigma2 = SvmUtils.calcAvgDist(svmTrainData);
			// System.out.println("Setting sigma to mean of distancens");
		} else
			sigma2 = sigmaParameter.current();
		gamma = 1 / (2 * Math.pow(sigma2, 2) * Math.PI);

		// Learn the svm model
		svm_model model = generateSvmModel(svmTrainData, nuParamter.current(), gamma);

		// Apply the model, collect outlier instance indxs
		SortedSet<Integer> outlierIdxs = new TreeSet<>();
		double[] predictions = new double[numExamples];

		// Get length of w, c.f.
		// http://www.csie.ntu.edu.tw/~cjlin/libsvm/faq.html#f4151
		// |w|^2 = w^Tw = alpha^T Q alpha = 2*(dual_obj + sum alpha_i).
		double w = Math.sqrt(2 * (model.obj + Miscellaneous.sum(model.alphas)));

		double sumDistances = 0.0d;
		for (int i = 0; i < numExamples; ++i) {
			double[] dec_value = new double[1];
			svm.svm_predict_values(model, svmTrainData[i], dec_value);
			predictions[i] = dec_value[0];

			if (predictions[i] < 0) {
				outlierIdxs.add(i);
				sumDistances += predictions[i] / w;
			}
			// System.out.println(predictions[i]);
		}

		if (outlierIdxs.size() > numExamples * Math.ceil(nuParamter.current()))
			System.out.println("############  TO MANY OUTLIERS, got : " + outlierIdxs.size() + " expected: "
					+ numExamples * nuParamter.current());

		Set<Attribute<?>> attrs = new HashSet<>();
		attrs.addAll(targetAttrParam.current());

		double propOutliers = (numExamples - outlierIdxs.size()) * 1.0 / numExamples;

		Outlier oPattern = new Outlier(this.getDataTable(), copyOf(outlierIdxs), attrs, Math.abs(sumDistances),
				1 - propOutliers);

		List<Pattern<?>> results = new ArrayList<>();
		results.add(oPattern);

		return results;
	}

	public DataTable getDataTable() {
		return dataTableParameter.current();
	}

	private svm_node[][] generateSvmTrainData(DataTable dt, List<Integer> idxs, int numExamples, int numDims) {
		List<svm_node[]> trainData = new ArrayList<>();
		List<svm_node> x = new ArrayList<>();

		// Create normalization parameters
		Double xm[] = new Double[numDims];
		Double xf[] = new Double[numDims];

		for (int i = 0; i < idxs.size(); ++i) {
			MetricAttribute a = (MetricAttribute) dt.attribute(idxs.get(i));
			xf[i] = a.max() - a.min();
			xm[i] = a.min();
		}

		for (int i = 0; i < numExamples; i++) {
			x.clear();

			for (int j = 0; j < numDims; j++) {
				svm_node xj = new svm_node();
				// we number features sequentially here, needs to start at 1 not
				// 0!
				xj.index = j + 1;
				// but need to receive the correct column from dt, so lookup via
				// idxs
				try {
					Number parsedVal = (Number) dt.attribute(idxs.get(j)).value(i);

					// Normalize to -1/1
					xj.value = parsedVal.doubleValue() - xm[j] / (xf[j]);
					x.add(xj);
				} catch (IllegalArgumentException e) {
					// Value is missing, we assume 0.0 here
					// FIXME: @sbothe do sth. more clever here?
					xj.value = 0.0d;
					x.add(xj);

				}
			}
			trainData.add(x.toArray(new svm_node[0]));
		}
		svm_node[][] svmTrainData = trainData.toArray(new svm_node[0][]);
		return svmTrainData;
	}

	// @Override
	// public void setTargetAttributes(List<Attribute> targets) {
	// Check.notNull(targets);
	// this.targetAttrParam.set(targets);
	//
	// }

	public void setNuParamter(double nu) {
		this.nuParamter.set(nu);
	}

	// @Override
	// public List<Attribute> getTargetAttributes() {
	// return this.targetAttrParam.getCurrentValue();
	// }

	// @Override
	// public void setPatternFactory(ExceptionalModelPatternFactory factory) {
	// Check.notNull(factory);
	// this.patternFactory = factory;
	// }

	/**
	 * @param trainData
	 *            the training data the model shall be based upon.
	 * @param fracOutliers
	 *            the acceptable fraction of outliers \in (0,1]
	 * @param gamma
	 *            the width of the Gaussian kernel
	 * @return the svm_model for this scenario
	 */
	private svm_model generateSvmModel(final svm_node[][] trainData, final double fracOutliers, double gamma) {
		svm_problem problem = new svm_problem();

		problem.x = trainData;
		// manually need to set problem length = number of training samples
		problem.l = trainData.length;
		// and assign labels 0.0, which are ignored, but are required to start
		// optimizer
		problem.y = new double[problem.l];

		svm_parameter params = new svm_parameter();
		// Choose one-class svm
		params.svm_type = svm_parameter.ONE_CLASS;

		// params.svm_type = svm_parameter.C_SVC;
		// Use Gaussian kernel, k(x,y) = exp (-\gamma \|x-y\|)
		params.kernel_type = svm_parameter.RBF;
		params.gamma = gamma;

		params.nu = fracOutliers;

		params.eps = EPSILON;

		svm.svm_check_parameter(problem, params);

		svm_model model = svm.svm_train_interruptable(problem, params, new Callable<Boolean>() {
			public Boolean call() {
				return stopRequested();
			}
		});

		return model;
	}

	public Parameter<List<Attribute<?>>> getTargetAttributesParameter() {
		return targetAttrParam;
	}

	public FractionOfOutliersParameter getFractionOfOutlierParameter() {
		return nuParamter;
	}

	public Parameter<DataTable> getDataTableParameter() {
		return dataTableParameter;
	}

}

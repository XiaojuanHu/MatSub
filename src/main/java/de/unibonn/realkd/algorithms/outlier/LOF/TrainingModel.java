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
package de.unibonn.realkd.algorithms.outlier.LOF;

import static de.unibonn.realkd.common.base.Pair.pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.unibonn.realkd.common.base.Pair;
import de.unibonn.realkd.data.table.DataTable;

/**
 * <p>
 * training data model that is used when computing the Local Outlier factors for
 * examples currentyly we compute n*n matrix that compute the distance between
 * all examples and compute also the sorted version of the matrix as well.
 * </p>
 * 
 * @author amr Koura
 *
 */
public class TrainingModel {

	private double[][] matrix;
	private int[][] matrixSortedIndicies;
	DataTable data;
	private double[] maxValues;
	private double[] minValues;
	List<Integer> interestedIndecies;
	int numOfTrainingInstances;
	int numOfDimensions;

	public double[][] getMatirx() {
		return matrix;
	}

	public int[][] getMatrixSortedIndicies() {
		return matrixSortedIndicies;
	}

	public TrainingModel(DataTable data, List<Integer> indicies, String newExample) {
		this.data = data;
		interestedIndecies = indicies;
		numOfTrainingInstances = data.population().size() + 1; // for the new
																	// instance
		numOfDimensions = indicies.size();
	}

	/**
	 * build the training model from the incoming data table
	 */
	public TrainingModel(DataTable data, List<Integer> indicies) {
		this.data = data;
		interestedIndecies = indicies;
		numOfTrainingInstances = data.population().size();
		numOfDimensions = indicies.size();
		matrix = new double[numOfTrainingInstances][numOfTrainingInstances];
		matrixSortedIndicies = new int[numOfTrainingInstances][numOfTrainingInstances];
		List<double[]> maxAndMinValues = computeMaxAndMinValues();
		maxValues = maxAndMinValues.get(0);
		minValues = maxAndMinValues.get(1);

		// fill the matrix with distance between instances
		for (int i = 0; i < numOfTrainingInstances; i++) {
			matrix[i][i] = 0.0; // all the diagonal will be equal to zero
			for (int j = i + 1; j < numOfTrainingInstances; j++) {
				matrix[i][j] = computeDisctance(i, j);
				matrix[j][i] = matrix[i][j]; // symmetric

			}

		}

		// fill the sorted matrix
		for (int i = 0; i < numOfTrainingInstances; i++) {
			matrixSortedIndicies[i] = getSortOrder(matrix[i]);

		}
	}

	/**
	 * Computes the ascending sort order of a list of doubles,
	 * such that the ordering of the same values in the original
	 * array remains.
	 * 
	 * @param arr the list of doubles
	 * @return the sorted indices of the values in the original matrix
	 */
	private static int[] getSortOrder(double[] arr) {
		List<Pair<Double, Integer>> valuesWithIndex = IntStream.range(0, arr.length).mapToObj(i -> pair(arr[i], i)).collect(Collectors.toList());
		
		Collections.sort(valuesWithIndex, new Comparator<Pair<Double, Integer>>() {

			@Override
			public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2) {
				int c = o1._1().compareTo(o2._1());
				
				if(c != 0) {
					return c;
				}
				
				return o1._2().compareTo(o2._2());
			}
			
		});

		int[] indices = new int[arr.length];
		
		IntStream.range(0, arr.length).forEach(i -> indices[i] = valuesWithIndex.get(i)._2());
		
		return indices;
	}
	
	/**
	 * compute the distance between two elements
	 * 
	 * @param firstElementIndex:
	 *            the position of the first instance in the data table
	 * @param secondElementIndex:
	 *            the position of the second element in the data table
	 * @return
	 */
	public double computeDisctance(int firstElementIndex, int secondElementIndex) {
		double result = 0.0;
		double diff = 0.0;
		double firstValue = 0.0;
		double secondValue = 0.0;

		for (int i = 0; i < numOfDimensions; i++) {
			try {
				firstValue = ((Number) data.attribute(interestedIndecies.get(i)).value(firstElementIndex))
						.doubleValue();
			} catch (Exception ex) {
				firstValue = 0;
			}
			try {
				secondValue = ((Number) data.attribute(interestedIndecies.get(i)).value(secondElementIndex))
						.doubleValue();
			} catch (Exception ex) {
				secondValue = 0;
			}
			diff = Math.abs(firstValue - secondValue);
			diff = diff / (maxValues[i] - minValues[i]);
			result += diff;
		}
		return result;
	}

	/**
	 * insert example into existing data table
	 * 
	 * @param example
	 *            , comma separated string
	 */
	public void insertExample(String example) {

	}

	/**
	 * get the max and minValues from each intested Values
	 * 
	 * @return
	 */
	private List<double[]> computeMaxAndMinValues() {
		double[] minValues = new double[numOfDimensions];
		double[] maxValues = new double[numOfDimensions];
		List<double[]> result = new ArrayList<double[]>();

		// initialize them
		for (int i = 0; i < numOfDimensions; i++) {
			maxValues[i] = Double.MIN_VALUE;
			minValues[i] = Double.MAX_VALUE;
		}
		double value;
		// fill them with correct Data
		for (int i = 0; i < numOfTrainingInstances; i++) {
			for (int j = 0; j < numOfDimensions; j++) {
				try {
					value = ((Number) data.attribute(interestedIndecies.get(j)).value(i)).doubleValue();
				} catch (IllegalArgumentException ex) {
					value = Double.NaN; // in case of missing value , we will
										// treat this vale as zero
				}
				if (value < minValues[j]) {
					minValues[j] = value;
				}
				if (value > maxValues[j]) {
					maxValues[j] = value;
				}
			}
		}
		result.add(maxValues);
		result.add(minValues);
		return result;
	}

}

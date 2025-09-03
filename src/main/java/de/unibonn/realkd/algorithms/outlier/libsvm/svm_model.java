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

//
// svm_model
//
package de.unibonn.realkd.algorithms.outlier.libsvm;

public class svm_model implements java.io.Serializable {
	public svm_parameter param; // parameter
	public int nr_class; // number of classes, = 2 in regression/one class svm
	public int l; // total #SV
	public svm_node[][] SV; // SVs (SV[l])
	public double[][] sv_coef; // coefficients for SVs in decision functions
								// (sv_coef[k-1][l])
	public double[] rho; // constants in decision functions (rho[k*(k-1)/2])
	public double[] probA; // pariwise probability information
	public double[] probB;
	public int[] sv_indices; // sv_indices[0,...,nSV-1] are values in
								// [1,...,num_traning_data] to indicate SVs in
								// the training set

	// for classification only

	public int[] label; // label of each class (label[k])
	public int[] nSV; // number of SVs for each class (nSV[k])
	// nSV[0] + nSV[1] + ... + nSV[k-1] = l

	public double obj; // value of dual objective function
	public double[] alphas;
};

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.graphs;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * <p>
 * This class is a helper class for graph function.
 * </p>
 *
 * @author Ali Doku
 * 
 * @since 0.7.1
 * 
 * @version 0.7.1
 */

public class GraphHelper {

	private static BiMap<Integer, Integer> uniqueIdNodeMaping = HashBiMap.create();

	/**
	 * this function gets as input a list of edges.
	 * 
	 * @return a matrix (0,1) where 1 is an edge from node start to node end.
	 */
	private static int[][] matrixV_E(List<Node> nodes, List<Edge> edges) {
		if (nodes == null & edges == null)
			return null;
		int nr_nodes = nodes.size();
		int[][] matrix = new int[nr_nodes][nr_nodes];

		for (int i = 0; i < nodes.size(); i++) {
			uniqueIdNodeMaping.put(nodes.get(i).id(), i);
		}

		for (Edge edge : edges) {
			matrix[uniqueIdNodeMaping.get(edge.start())][uniqueIdNodeMaping.get(edge.end())] = 1;
		}
		return matrix;
	}

	public static GraphDescriptor hsutransitive(List<Node> nodes, List<Edge> edges) {
		int[][] matrix = matrixV_E(nodes, edges);
		if (matrix == null)
			return null;

		List<Edge> newEdges = new ArrayList<>();
		for (int j = 0; j < matrix.length; j++) {
			for (int i = 0; i < matrix.length; i++)
				if (matrix[i][j] == 1)
					for (int k = 0; k < matrix.length; k++)
						if (matrix[j][k] == 1)
							matrix[i][k] = 0;
		}

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				if (matrix[i][j] == 1) {
					newEdges.add(Edges.create(uniqueIdNodeMaping.inverse().get(i), uniqueIdNodeMaping.inverse().get(j)));
				}
			}
		}

		return GraphDescriptors.create(nodes, newEdges);
	}

}

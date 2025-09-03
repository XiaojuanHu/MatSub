/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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
package de.unibonn.realkd.lang;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.AlgorithmProvider;
import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.algorithms.MiningAlgorithmFactory;
import de.unibonn.realkd.algorithms.association.AssociationMiningBeamSearch;
import de.unibonn.realkd.algorithms.association.AssociationSampler;
import de.unibonn.realkd.algorithms.emm.ExceptionalModelBeamSearch;
import de.unibonn.realkd.algorithms.emm.ExceptionalSubgroupBestFirstBranchAndBound;
import de.unibonn.realkd.algorithms.emm.ExceptionalSubgroupSampler;
import de.unibonn.realkd.algorithms.emm.dssd.DiverseSubgroupSetDiscovery;
import de.unibonn.realkd.common.workspace.Workspace;

/**
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class BuiltInAlgorithms implements AlgorithmProvider {

	public static final MiningAlgorithmFactory ASSOCIATION_BEAMSEARCH = new MiningAlgorithmFactory() {

		@Override
		public MiningAlgorithm create(Workspace workspace) {
			return new AssociationMiningBeamSearch(workspace);
		}

		@Override
		public String id() {
			return "ASSOCIATION_BEAMSEARCH";
		}

	};

	public static final MiningAlgorithmFactory EMM_BEAMSEARCH = new MiningAlgorithmFactory() {

		@Override
		public MiningAlgorithm create(Workspace workspace) {
			return new ExceptionalModelBeamSearch(workspace);
		}

		@Override
		public String id() {
			return "EMM_BEAMSEARCH";
		}

	};

	public static final MiningAlgorithmFactory EMM_SAMPLER = new MiningAlgorithmFactory() {

		@Override
		public MiningAlgorithm create(Workspace dataTub) {
			return ExceptionalSubgroupSampler.exceptionalSubgroupSampler(dataTub);
		}

		@Override
		public String id() {
			return "EMM_SAMPLER";
		}

	};
	
	public static final MiningAlgorithmFactory EXT_EXC_SGD = new MiningAlgorithmFactory() {

		@Override
		public MiningAlgorithm create(Workspace workspace) {
			return new ExceptionalSubgroupBestFirstBranchAndBound(workspace);
		}

		@Override
		public String id() {
			return "EXT_EXC_SGD";
		}

	};

	public static final MiningAlgorithmFactory ASSOCIATION_SAMPLER = new MiningAlgorithmFactory() {

		@Override
		public MiningAlgorithm create(Workspace dataTub) {
			return new AssociationSampler(dataTub);
		}

		public String id() {
			return "ASSOCIATION_SAMPLER";
		}

	};

	// TODO: decide what to do with those algorithms
	// ALGORITHM_FACTORIES.put("OUTLIER", new MiningAlgorithmFactory() {
	//
	// @Override
	// public MiningAlgorithm create(DataWorkspace dataTub) {
	// return new OneClassModelMiner(dataTub);
	// }
	//
	// });
	//
	// ALGORITHM_FACTORIES.put("LOF", new MiningAlgorithmFactory() {
	//
	// @Override
	// public MiningAlgorithm create(DataWorkspace dataTub) {
	// return new LOFOutlier(dataTub);
	// }
	//
	// });
	//
	// ALGORITHM_FACTORIES.put("ILOF", new MiningAlgorithmFactory() {
	//
	// @Override
	// public MiningAlgorithm create(DataWorkspace dataTub) {
	// return new ILOFOutlier(dataTub);
	// }
	//
	// });

	public static final MiningAlgorithmFactory DSSD = new MiningAlgorithmFactory() {

		@Override
		public MiningAlgorithm create(Workspace workspace) {
			return DiverseSubgroupSetDiscovery.createStandardDiverseSubgroupSetDiscovery(workspace);
		}

		@Override
		public String id() {
			return "DSSD";
		}

	};

	public static final List<MiningAlgorithmFactory> ALL = ImmutableList.of(ASSOCIATION_BEAMSEARCH, ASSOCIATION_SAMPLER,
			EXT_EXC_SGD, EMM_BEAMSEARCH, EMM_SAMPLER, DSSD);

	@Override
	public String name() {
		return "realKD built-in algorithms";
	}

	@Override
	public List<MiningAlgorithmFactory> get() {
		return ALL;
	}

}

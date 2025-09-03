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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.Pattern;

/**
 * 
 * * <p>
 * Class compute the iterative Local Outlier factor for all examples in the datasets
 * we use this class for iterative mode of LOF.
 * in iterative mode new example can be added or deleted from the datset.
 * </p>
 * @author amr Koura
 *
 */
public class ILOFOutlier extends LOFOutlier {

	protected ILOFNewDataParameter newPoint;
	
	public ILOFOutlier(Workspace workspace) {
		
		super(workspace);

		newPoint=new ILOFNewDataParameter(this);
		registerParameter(newPoint);
		
	}

	
	/* (non-Javadoc)
	 * @see de.unibonn.realkd.algorithms.MiningAlgorithm#getName()
	 */
	@Override
	public String caption() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see de.unibonn.realkd.algorithms.MiningAlgorithm#getDescription()
	 */
	@Override
	public String description() {
		// TODO Auto-generated method stub
		return "Incremental Local Outlier Factor Algorithm";
	}

	/* (non-Javadoc)
	 * @see de.unibonn.realkd.algorithms.MiningAlgorithm#getCategory()
	 */
	@Override
	public AlgorithmCategory getCategory() {
		// TODO Auto-generated method stub
		return AlgorithmCategory.OUTLIER_DETECTION;
	}

	/**
	 * used to add set of Points dynamically
	 */
	@Override
	protected Collection<Pattern<?>> concreteCall() {
		// TODO Auto-generated method stub
		
		
		DataTable dt = this.getDataTable();

		// Determine indices for target attrs and make sure they are numeric
		List<String> names = dt.attributeNames();
		List<Integer> idxs = new ArrayList<>();

		for (Attribute<?> attribute : this.targetAttrParam.current()) {
			int idx = names.indexOf(attribute.caption());
//			Check.isTrue(dt.isNumeric(idx));
			idxs.add(idx);
		}

		numExamples = dt.population().size();
		int numDims = idxs.size();
		
		// insert the new point
		for(Integer tmpAttributeIndex:idxs)
		{
			//dt.getAttribute(tmpAttributeIndex).
		}
		
		
		return null;
	}

}

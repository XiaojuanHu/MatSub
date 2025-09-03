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
package de.unibonn.realkd.patterns.subgroups;

import java.util.List;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.LocalPatternDescriptor;
import de.unibonn.realkd.patterns.TableSubspaceDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.models.Model;
import de.unibonn.realkd.patterns.models.ModelFactory;

/**
 * Describes a sub-population of a given global population (logically) as well
 * as set of target attributes and global and local (in terms of the
 * sub-population) models of the value distribution of those targets.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.5.0
 *
 */
public interface Subgroup<M extends Model> extends TableSubspaceDescriptor, LocalPatternDescriptor {

	public M referenceModel();

	public M localModel();

	public ModelFactory<? extends M> fittingAlgorithm();

	public List<? extends Attribute<?>> targetAttributes();

	public List<Integer> targetAttributeIndices();

	public LogicalDescriptor extensionDescriptor();
	
	public ReferenceDescriptor referenceDescriptor();

	@Override
	public IndexSet supportSet();

	@Override
	public List<Attribute<?>> getReferencedAttributes();

	@Override
	public Population population();

	@Override
	public SerialForm<? extends Subgroup<M>> serialForm();

	/**
	 * 
	 * @return the datatable that contains the target attributes
	 * 
	 */
	public DataTable getTargetTable();

	/**
	 * Factory method for obtaining a simplified subgroup descriptor.
	 * 
	 * @return subgroup with approximately shortest extension descriptor equivalent
	 *         to extension descriptor of this
	 * 
	 */
	public Subgroup<M> greedySimplification();

}

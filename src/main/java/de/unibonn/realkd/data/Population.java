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
package de.unibonn.realkd.data;

import java.util.List;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.workspace.Entity;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.data.Populations.PopulationSerialForm;

/**
 * <p>
 * Contains data about a fixed population of objects (entities), which are
 * identified by consecutive indices from 0 to m-1 where m is referred to as the
 * size of the population.
 * </p>
 * 
 * <p>
 * Each object (entity) has a name. In the future there might be an explicit
 * object representation for each (data) object as its own entity.
 * </p>
 * 
 * <p>
 * Populations can be identified by an identifier, which conceptually should be
 * unique to the real-world entity corresponding to this population.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.1.2
 *
 */
public interface Population extends Entity, HasSerialForm<Population> {

	/**
	 * 
	 * @return the number of objects subsumed in this population
	 */
	public int size();

	/**
	 * 
	 * @return the sorted set of consecutive integers {0,getSize()-1}
	 */
	public IndexSet objectIds();

	/**
	 * 
	 * @param id
	 *            integer index of an object
	 * @return name of object with queried id
	 */
	public String objectName(int id);

	/**
	 * 
	 * @return list of all object names in order of indices
	 */
	public List<String> objectNames();
	
	@Override
	public PopulationSerialForm serialForm();

}

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
package de.unibonn.realkd.data.table;

import java.util.List;
import java.util.Optional;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;

/**
 * Table with only categorical attributes.
 * 
 * @author Mario Boley
 * 
 * @since 0.4.1
 * 
 * @version 0.4.1
 *
 */
public interface DiscreteDataTable extends DataTable {
	
	@Override
	public abstract List<? extends CategoricAttribute<?>> attributes();
	
	@Override
	public abstract CategoricAttribute<?> attribute(int attributeIndex);
	
	@Override
	public Optional<? extends CategoricAttribute<?>> attribute(Identifier identifier);

}

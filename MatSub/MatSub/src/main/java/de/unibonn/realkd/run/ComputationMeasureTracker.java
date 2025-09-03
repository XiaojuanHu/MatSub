/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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
package de.unibonn.realkd.run;

import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import de.unibonn.realkd.algorithms.ComputationMeasure;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.computations.core.Computation;
import de.unibonn.realkd.util.IO;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
@KdonTypeName("computationMeasureTracker")
public class ComputationMeasureTracker implements Tracker {

	@JsonProperty("measure")
	private final ComputationMeasure measure;

	private final Table<Identifier, Identifier, String> results;

	@JsonCreator
	private ComputationMeasureTracker(@JsonProperty("measure") ComputationMeasure measure) {
		this.measure = measure;
		this.results = TreeBasedTable.create();
	}

	@Override
	public void consume(Identifier inputId, Identifier compuationId, Computation<?> computation,
			Object result) {
		results.put(inputId, compuationId, String.valueOf(measure.perform(computation).value()));
	}

	@Override
	public void writeResults(Path path) {
		String output = IO.csv(results, "input", ",");
		IO.writeOut(path.resolve(measure.toString() + ".csv"), output);
	}

}

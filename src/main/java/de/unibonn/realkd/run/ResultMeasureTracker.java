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
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.computations.core.Computation;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.util.IO;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
@KdonTypeName("resultMeasureTracker")
public class ResultMeasureTracker implements Tracker {

	private static final Logger LOGGER = Logger.getLogger(ResultMeasureTracker.class.getName());

	@JsonProperty("measure")
	private final Measure measure;

	@JsonInclude(Include.NON_ABSENT)
	@JsonProperty("measureType")
	private final String measureType;

	private final Class<? extends Measure> measureClass;

	private final Table<Identifier, Identifier, String> results = HashBasedTable.create();

	@JsonCreator
	@SuppressWarnings("unchecked")
	private ResultMeasureTracker(@JsonProperty("measure") Measure measure,
			@JsonProperty("measureType") String measureType) {
		this.measure = measure;
		this.measureType = measureType;
		if (measure != null && measureType != null) {
			LOGGER.warning(String.format("Ignoring measure type '%s' because measure '%s' was given", measureType,
					measure.identifier()));
		}
		Class<? extends Measure> clazz = null;
		try {
			clazz = (Class<? extends Measure>) Class.forName(measureType);
		} catch (Exception e) {
			LOGGER.warning("Could not find measure type: " + measureType);
		}
		this.measureClass = clazz;
	}

	private Optional<Measurement> measurement(Pattern<?> p) {
		if (measure != null && p.hasMeasure(measure)) {
			return p.measurement(measure);
		}
		if (measureClass != null) {
			return p.measurement(measureClass);
		}
		return Optional.empty();
	}

	private String filename() {
		if (measure != null) {
			return measure.identifier().toString();
		}
		return measureClass.getSimpleName();
	}

	@Override
	public void consume(Identifier inputId, Identifier computationId, Computation<?> computation, Object result) {
		// Function<Pattern<?>, Optional<Measurement>> function = p ->
		// p.measurement(measure);
		if (!(result instanceof Collection)) {
			return;
		}
		Optional<? extends Pattern<?>> topPattern = ((Collection<?>) result).stream().filter(e -> e instanceof Pattern)
				.map(e->(Pattern<?>)e).findFirst();
		String value = topPattern.flatMap(this::measurement).map(m -> String.valueOf(m.value())).orElse("?");
		results.put(inputId, computationId, value);
	}

	@Override
	public void writeResults(Path path) {
		String output = IO.csv(results, "input", ",");
		IO.writeOut(path.resolve(filename() + ".csv"), output);
	}

}

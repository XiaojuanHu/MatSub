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
package de.unibonn.realkd.patterns.models.bernoulli;

import static java.util.EnumSet.allOf;
import static java.util.Optional.empty;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.patterns.models.Model;
import de.unibonn.realkd.patterns.models.ModelParameter;

/**
 * Models the probability of a single discrete event over one data attribute.
 * 
 * @author Mario Boley
 * 
 * @author Michael Hedderich
 * 
 * @since 0.4.0
 * 
 * @version 0.5.0
 */
@KdonTypeName("bernoulli")
@KdonDoc("Models binary random variable.")
public class BernoulliDistribution implements Model, HasSerialForm<BernoulliDistribution>, SerialForm<BernoulliDistribution> {

	private static enum SingleEventModelParameters implements ModelParameter {

		PROBABILITY;

		@Override
		public String caption() {
			return "p";
		}

		@Override
		public String description() {
			return "The probability of the specific event modelled";
		}

	}

	@JsonProperty("probability")
	@KdonDoc("Modelled probability of positive outcome.")
	private final double probability;
	
	private String stringRepresentation;

	@JsonCreator
	public BernoulliDistribution(@JsonProperty("probability") double probability) {
		this.probability = probability;
		this.stringRepresentation = String.format(Locale.ENGLISH, "p=%.4f", probability);
	}

	public double probability() {
		return probability;
	}

	@Override
	public Collection<SingleEventModelParameters> parameters() {
		return allOf(SingleEventModelParameters.class);
	}

	public Optional<Double> value(ModelParameter p) {
		if (p != SingleEventModelParameters.PROBABILITY) {
			return empty();
		}
		return Optional.of(probability);
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}

	@Override
	public SerialForm<BernoulliDistribution> serialForm() {
		return this;
	}

	@Override
	public BernoulliDistribution build(Workspace workspace) {
		return this;
	}

}

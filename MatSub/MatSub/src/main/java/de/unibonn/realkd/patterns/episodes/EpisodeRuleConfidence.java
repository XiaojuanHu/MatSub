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
package de.unibonn.realkd.patterns.episodes;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.data.sequences.Window;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternDescriptor;

/**
 *
 * @author Ali Doku
 * @author Sandy Moens
 * 
 * @since 0.7.1
 * 
 * @version 0.7.1
 */
public enum EpisodeRuleConfidence implements Measure, MeasurementProcedure<Measure, PatternDescriptor> {

	EPISODE_RULE_CONDIFENCE;

	private EpisodeRuleConfidence() {
		;
	}

	@Override
	public Identifier identifier() {
		return Identifier.id("episode_rule_confidence");
	}

	@Override
	public String caption() {
		return "rule confidence";
	}

	@Override
	public String description() {
		return "Confidence of the episode given a windows size. "
				+ "This implementation is based on minimal non-overlapping windows.";
	}

	@JsonIgnore
	@Override
	public Measure getMeasure() {
		return this;
	}

	@Override
	public boolean isApplicable(PatternDescriptor descriptor) {
		return EpisodeRuleDescriptor.class.isAssignableFrom(descriptor.getClass());
	}

	@Override
	public Measurement perform(PatternDescriptor descriptor) {
		EpisodeRuleDescriptor episodeRuleDescriptor = (EpisodeRuleDescriptor) descriptor;

		List<Window> antecedentWindows = episodeRuleDescriptor.antecedentWindows();
		List<Window> consequentWindows = episodeRuleDescriptor.consequentWindows();

		double confidence;

		if (antecedentWindows.size() == 0) {
			confidence = 0;
		} else {
			int c = 0;

			for (Window w : antecedentWindows) {
				for (Window v : consequentWindows)
					if (v.start() <= w.start() && v.end() >= w.end()) {
						c += 1;
						break;
					}
			}

			confidence = c / (double) antecedentWindows.size();
		}

		return Measures.measurement(getMeasure(), confidence);
	}

}

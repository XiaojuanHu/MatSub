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
package de.unibonn.realkd.patterns.rules;

import static com.google.common.collect.Sets.newHashSet;
import static de.unibonn.realkd.common.IndexSets.intersection;
import static java.util.Objects.hash;

import java.util.Collection;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;

/**
 * <p>
 * Composes two logical descriptors as a rule where the antecedent implies the
 * consequent in the rule descriptor.
 * </p>
 * 
 * @author Sandy Moens
 * 
 * @since 0.1.2
 * 
 * @version 0.7.1
 * 
 */
public class DefaultRuleDescriptor implements RuleDescriptor {

	public static RuleDescriptor create(Population population,
			LogicalDescriptor antecedent, LogicalDescriptor consequent) {
		return new DefaultRuleDescriptor(population, antecedent,
				consequent);
	}

	private final Population population;

	private final LogicalDescriptor antecedent;

	private final LogicalDescriptor consequent;

	private IndexSet supportSet;
	
	private DefaultRuleDescriptor(Population population,
			LogicalDescriptor antecedent, LogicalDescriptor consequent) {
		this.population = population;
		this.antecedent = antecedent;
		this.consequent = consequent;
		this.supportSet = null;
	}

	@Override
	public LogicalDescriptor getAntecedent() {
		return this.antecedent;
	}

	@Override
	public LogicalDescriptor getConsequent() {
		return this.consequent;
	}

	private static class DefaultRuleDescriptorBuilder implements
			SerialForm<RuleDescriptor> {
		
		@JsonProperty("population")
		private final Identifier populationIdentifier;
		
		@JsonProperty("antecedent")
		private final SerialForm<LogicalDescriptor> antecedentBuilder;

		@JsonProperty("consequent")
		private final SerialForm<LogicalDescriptor> consequentBuilder;

		@JsonCreator
		public DefaultRuleDescriptorBuilder(
				@JsonProperty("population") Identifier populationIdentifier,
				@JsonProperty("antecedent") SerialForm<LogicalDescriptor> antecedentBuilder,
				@JsonProperty("consequent") SerialForm<LogicalDescriptor> consequentBuilder) {
			this.populationIdentifier = populationIdentifier;
			this.antecedentBuilder = antecedentBuilder;
			this.consequentBuilder = consequentBuilder;
		}
		
		@Override
		public DefaultRuleDescriptor build(Workspace context) {
			return new DefaultRuleDescriptor(
					context.get(populationIdentifier, Population.class).get(),
					this.antecedentBuilder.build(context),
					this.consequentBuilder.build(context));
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			Set<Identifier> dependencyIds = newHashSet();
			dependencyIds.add(this.populationIdentifier);
			dependencyIds.addAll(this.antecedentBuilder.dependencyIds());
			dependencyIds.addAll(this.consequentBuilder.dependencyIds());
			return dependencyIds;
		}

	}

	public SerialForm<RuleDescriptor> serialForm() {
		return new DefaultRuleDescriptorBuilder(population.identifier(), antecedent.serialForm(),
				consequent.serialForm());
	}

	@Override
	public int hashCode() {
		return hash(this.antecedent, this.consequent);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DefaultRuleDescriptor)) {
			return false;
		}
		
		DefaultRuleDescriptor other = (DefaultRuleDescriptor) obj;
		return this.antecedent.equals(other.antecedent) && this.consequent.equals(other.consequent);
	}

	@Override
	public Population population() {
		return population;
	}

	@Override
	public IndexSet supportSet() {
		if(this.supportSet == null) {
			this.supportSet = intersection(this.antecedent.supportSet(), this.consequent.supportSet());
		}
		return this.supportSet;
	}

}

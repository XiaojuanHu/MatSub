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

import static de.unibonn.realkd.common.base.Identifier.id;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Enums;

import de.unibonn.realkd.algorithms.AlgorithmFactory;
import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.workspace.Entity;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.lang.types.StringValue;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
@KdonTypeName("legacyComputation")
public class LegacyComputationSpecification implements ComputationSpecification {

	public static LegacyComputationSpecification legacyComputation(Identifier id, Identifier algorithm,
			HashMap<String, String> params) {
		return new LegacyComputationSpecification(id, algorithm, params);
	}

	private static final Logger LOGGER = Logger.getLogger(LegacyComputationSpecification.class.getName());

	@JsonProperty("id")
	private final Identifier id;

	@JsonProperty("algorithm")
	private final Identifier algorithm;

	@JsonProperty("parameters")
	private final HashMap<String, String> parameters;

	private LegacyComputationSpecification(@JsonProperty("id") Identifier id,
			@JsonProperty("algorithm") Identifier algorithm,
			@JsonProperty("parameters") HashMap<String, String> params) {
		this.id = id;
		this.algorithm = algorithm;
		this.parameters = params;
	}

	@Override
	public MiningAlgorithm build(Workspace context) throws ValidationException {
		com.google.common.base.Optional<AlgorithmFactory> factory = Enums.getIfPresent(AlgorithmFactory.class,
				algorithm.toString());
		if (!factory.isPresent()) {
			throw new ValidationException(algorithm + " not found", "Check spelling");
		}
		MiningAlgorithm miningAlgorithm = factory.get().create(context);
		Map<Identifier, String> resolvedParameters = new HashMap<>();
		for (String key : parameters.keySet()) {
			String value = parameters.get(key);
			if (value.startsWith("@")) {
				Identifier valueId = id(value.substring(1));
				Optional<Entity> optional = context.get(valueId, Entity.class);
				if (!optional.isPresent()) {
					LOGGER.warning("Could not resolve symbol " + valueId + " in workspace; skipping parameter");
					continue;
				}
				if (!(optional.get() instanceof StringValue)) {
					LOGGER.warning(valueId + " does not refer to string value; skipping parameter");
					continue;
				}
				value = ((StringValue) optional.get()).asString();
			}
			resolvedParameters.put(id(key), value);
		}
		miningAlgorithm.pass(resolvedParameters);

		LOGGER.info("accepted parameter values:\n" + miningAlgorithm.getAllParameters().stream()
				.map(p -> p.id() + "=" + p.current()).collect(Collectors.joining("\n")));

		return miningAlgorithm;
	}

	@Override
	public Identifier identifier() {
		return id;
	}

}

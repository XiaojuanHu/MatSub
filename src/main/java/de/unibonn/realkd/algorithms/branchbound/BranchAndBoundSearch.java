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
package de.unibonn.realkd.algorithms.branchbound;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptors;

/**
 * 
 * 
 * 
 * @author Mario Boley
 * 
 * @since 0.4.0
 * 
 * @version 0.4.0
 *
 */
public class BranchAndBoundSearch {

	public static class BranchAndBoundSearchNode<T> {

		public final T content;

		private BranchAndBoundSearchNode(T content) {
			this.content = content;
		}

		@Override
		public String toString() {
			return content.toString();
		}

	}

	public static class LogicalDescriptorBasedBranchAndBoundSearchNode<T> extends BranchAndBoundSearchNode<T> {

		public final LogicalDescriptor descriptor;

		private LogicalDescriptorBasedBranchAndBoundSearchNode(T content, LogicalDescriptor descriptor) {
			super(content);
			this.descriptor = descriptor;
		}

	}

	// public static class LogicalDescriptorLexicographicalSearchNode<T> extends
	// BranchAndBoundSearchNode<T> {
	//
	// public final LogicalDescriptor descriptor;
	//
	// public final Optional<In
	//
	// private LogicalDescriptorLexicographicalSearchNode(T content,
	// LogicalDescriptor descriptor) {
	// super(content);
	// this.descriptor = descriptor;
	// }
	//
	// }

	public static class LogicalDescriptorWithValidAugmentationsNode<T>
			extends LogicalDescriptorBasedBranchAndBoundSearchNode<T> {

		public final List<Proposition> augmentations;

		public LogicalDescriptorWithValidAugmentationsNode(T content, LogicalDescriptor descriptor,
				List<Proposition> augmentations) {
			super(content, descriptor);
			this.augmentations = augmentations;
		}

	}

	public static class LcmSearchNode<T> extends LogicalDescriptorBasedBranchAndBoundSearchNode<T> {

		public final int minAugmentationIndex;

		private LcmSearchNode(LogicalDescriptor closure, int minAugmentationIndex, T content) {
			super(content, closure);
			// lcm-core-index+1 OR 0 for root closure
			this.minAugmentationIndex = minAugmentationIndex;
		}

	}

	private BranchAndBoundSearch() {
		;
	}

	public static <T> Function<LogicalDescriptorWithValidAugmentationsNode<T>, Collection<LogicalDescriptorWithValidAugmentationsNode<T>>> minimalGeneratorsExpander(
			PropositionalContext props, Function<LogicalDescriptor, T> toT) {
		return new Function<LogicalDescriptorWithValidAugmentationsNode<T>, Collection<LogicalDescriptorWithValidAugmentationsNode<T>>>() {

			@Override
			public Collection<LogicalDescriptorWithValidAugmentationsNode<T>> apply(
					LogicalDescriptorWithValidAugmentationsNode<T> n) {
				List<LogicalDescriptor> refinementDescriptions = new ArrayList<>();
				List<Proposition> filteredAugmentations = new ArrayList<>();
				for (Proposition a : n.augmentations) {
					LogicalDescriptor refinement = n.descriptor.specialization(a);
					// TODO check whether minimality refinement is valid for
					// removing from augmentation indices
					if (refinement.supportSet().size() < n.descriptor.supportSet().size() && refinement.minimal()) {
						filteredAugmentations.add(a);
						refinementDescriptions.add(refinement);
					}
				}
				Collection<LogicalDescriptorWithValidAugmentationsNode<T>> result = new ArrayList<>();
				for (int i = 0; i < filteredAugmentations.size(); i++) {
					result.add(new LogicalDescriptorWithValidAugmentationsNode<T>(
							toT.apply(refinementDescriptions.get(i)), refinementDescriptions.get(i),
							filteredAugmentations.subList(i + 1, filteredAugmentations.size())));
				}
				return result;
			}

		};
	}

	public static <T> LogicalDescriptorWithValidAugmentationsNode<T> minimalGeneratorRootNode(PropositionalContext props,
			Predicate<Proposition> filter, Function<LogicalDescriptor, T> toT) {
		LogicalDescriptor rootDescriptor = LogicalDescriptors.create(props.population(), ImmutableList.of());
		return new LogicalDescriptorWithValidAugmentationsNode<T>(toT.apply(rootDescriptor), rootDescriptor,
				props.propositions().stream().filter(filter).collect(Collectors.toList()));
	}

	public static <T> Function<LcmSearchNode<T>, Collection<LcmSearchNode<T>>> allDescriptorsExpander(
			PropositionalContext props, Predicate<Proposition> filter, Function<LogicalDescriptor, T> toT) {

		return new Function<LcmSearchNode<T>, Collection<LcmSearchNode<T>>>() {

			@Override
			public Collection<LcmSearchNode<T>> apply(LcmSearchNode<T> t) {
				// Optional<Integer> maxIndex =
				// t.descriptor.getElements().stream().map(p -> p.getId())
				// .max(Comparator.naturalOrder());
				// int minAugmentationIndex = maxIndex.map(i -> i +
				// 1).orElse(0);
				IntFunction<LcmSearchNode<T>> augmentation = i -> {
					LogicalDescriptor specialization = t.descriptor.specialization(props.proposition(i));
					return new LcmSearchNode<T>(specialization, i + 1, toT.apply(specialization));
				};
				return IntStream.range(t.minAugmentationIndex, props.propositions().size())
						.filter(i -> filter.test(props.proposition(i))).mapToObj(augmentation)
						.collect(Collectors.toList());
			}
		};
	}

	public static <T> LcmSearchNode<T> allDescriptorRootNode(PropositionalContext props,
			Function<LogicalDescriptor, T> toT) {
		LogicalDescriptor rootDescriptor = LogicalDescriptors.create(props.population(), ImmutableList.of());
		return new LcmSearchNode<T>(rootDescriptor, 0, toT.apply(rootDescriptor));
	}

	public static <T> LcmSearchNode<T> allDescriptorNode(LogicalDescriptor descriptor,
			Function<LogicalDescriptor, T> toT, int minAugmentationIndex) {
		return new LcmSearchNode<T>(descriptor, minAugmentationIndex, toT.apply(descriptor));
	}

	private static <T> LcmSearchNode<T> lcmSearchNode(LogicalDescriptor closure, int minAugmentationIndex, T content) {
		return new LcmSearchNode<>(closure, minAugmentationIndex, content);
	}

	public static <T> LcmSearchNode<T> lcmRootNode(PropositionalContext props, Predicate<Proposition> filter,
			Function<LogicalDescriptor, T> toT) {
		List<Proposition> newClosureElements = new ArrayList<>();
		for (int i = 0; i < props.propositions().size(); i++) {
			if (filter.test(props.proposition(i)) && props.holdsFor(i, props.population().objectIds())) {
				newClosureElements.add(props.proposition(i));
			}
		}
		LogicalDescriptor newClosure = LogicalDescriptors.create(props.population(), newClosureElements);

		return lcmSearchNode(newClosure, 0, toT.apply(newClosure));
	}

	public static <T> Function<LcmSearchNode<T>, Collection<LcmSearchNode<T>>> closedDescriptorsExpander(
			PropositionalContext props, Predicate<Proposition> filter, Function<LogicalDescriptor, T> toT) {

		return new Function<LcmSearchNode<T>, Collection<LcmSearchNode<T>>>() {

			private Optional<LcmSearchNode<T>> lcmRefinement(LogicalDescriptor current, int augmentationIndex) {
				Proposition augmentation = props.proposition(augmentationIndex);
				if (current.elements().contains(augmentation)) {
					return Optional.empty();
				}
				LogicalDescriptor generator = current.specialization(augmentation);
				// generator prefix preservation check
				for (int i = 0; i < augmentationIndex; i++) {
					Proposition p = props.proposition(i);
					if (filter.test(p) && !current.elements().contains(p) && generator.empiricallyImplies(p)) {
						return Optional.empty();
					}
				}

				// List<Proposition> newClosureElements = new
				// ArrayList<>(generator.getElements());
				// for (int i = augmentation.getId() + 1; i <
				// props.propositions().size(); i++) {
				// if (!generator.elementIndexList().contains(i) &&
				// filter.test(props.proposition(i))
				// && (generator.empiricallyImplies(props.proposition(i)))) {
				// newClosureElements.add(props.proposition(i));
				// }
				// }
				// LogicalDescriptor newClosure =
				// LogicalDescriptors.create(props, newClosureElements);
				// return Optional.of(lcmSearchNode(newClosure,
				// augmentation.getId() + 1, toT.apply(newClosure)));

				List<Proposition> closureCandidates = new ArrayList<>(generator.elements());
				for (int i = augmentationIndex + 1; i < props.propositions().size(); i++) {
					if (filter.test(props.proposition(i))) {
						closureCandidates.add(props.proposition(i));
					}
				}
				LogicalDescriptor newClosure = generator.supportPreservingSpecialization(closureCandidates);

				return Optional.of(lcmSearchNode(newClosure, augmentationIndex + 1, toT.apply(newClosure)));

			}

			@Override
			public Collection<LcmSearchNode<T>> apply(LcmSearchNode<T> n) {
				return range(n.minAugmentationIndex, props.propositions().size())
						.filter(i -> filter.test(props.proposition(i))).mapToObj(i -> lcmRefinement(n.descriptor, i))
						.filter(o -> o.isPresent()).map(o -> o.get()).collect(toList());
				// return props.propositions().subList(n.minAugmentationIndex,
				// props.propositions().size()).stream()
				// .filter(filter).map(p -> lcmRefinement(n.descriptor,
				// p)).filter(o -> o.isPresent())
				// .map(o -> o.get()).collect(Collectors.toList());
			}
		};
	}
	/*
	 * The following code should allow a more efficient implementation in the
	 * future
	 */
	// public static <T> LogicalDescriptorWithValidAugmentationsNode<T>
	// closedDescriptorsRootNode(PropositionalLogic props,
	// Predicate<Proposition> filter, Function<LogicalDescriptor, T> toT) {
	// List<Proposition> newClosureElements = new ArrayList<>();
	// List<Proposition> augmentationElements = new ArrayList<>();
	// for (int i = 0; i < props.propositions().size(); i++) {
	// if (filter.test(props.proposition(i))) {
	// if (props.holdsFor(i, props.population().objectIds())) {
	// newClosureElements.add(props.proposition(i));
	// } else {
	// augmentationElements.add(props.proposition(i));
	// }
	// }
	// }
	// LogicalDescriptor newClosure = LogicalDescriptors.create(props,
	// newClosureElements);
	// return new
	// LogicalDescriptorWithValidAugmentationsNode<>(toT.apply(newClosure),
	// newClosure,
	// augmentationElements);
	// }
	//
	// public static <T>
	// Function<LogicalDescriptorWithValidAugmentationsNode<T>,
	// Collection<LogicalDescriptorWithValidAugmentationsNode<T>>>
	// newClosedDescriptorsExpander(
	// PropositionalLogic props, Predicate<Proposition> filter,
	// Function<LogicalDescriptor, T> toT) {
	//
	// return new Function<LogicalDescriptorWithValidAugmentationsNode<T>,
	// Collection<LogicalDescriptorWithValidAugmentationsNode<T>>>() {
	//
	// private boolean impliesBlocked(LogicalDescriptor generator, int augIndex)
	// {
	// for (int i = 0; i < augIndex; i++) {
	// if (filter.test(props.proposition(i)) &&
	// !generator.elementIndexList().contains(i)
	// && generator.empiricallyImplies(props.proposition(i))) {
	// return true;
	// }
	// }
	// return false;
	// }
	//
	// private LogicalDescriptor closure(LogicalDescriptor generator, int
	// augIndex) {
	// List<Proposition> closureCandidates = new
	// ArrayList<>(generator.getElements());
	// for (int i = augIndex + 1; i < props.propositions().size(); i++) {
	// if (filter.test(props.proposition(i))) {
	// closureCandidates.add(props.proposition(i));
	// }
	// }
	// return generator.supportPreservingExtension(closureCandidates);
	// // List<Proposition> newClosureElements = new
	// // ArrayList<>(generator.getElements());
	// // for (int i = augIndex; i < props.propositions().size(); i++)
	// // {
	// // if (!generator.elementIndexList().contains(i) &&
	// // filter.test(props.proposition(i))
	// // && (generator.empiricallyImplies(props.proposition(i)))) {
	// // // && (props.holdsFor(i, generator.indices()))) {
	// // newClosureElements.add(props.proposition(i));
	// // }
	// // }
	// // return LogicalDescriptors.create(props, newClosureElements);
	// }
	//
	// @Override
	// public Collection<LogicalDescriptorWithValidAugmentationsNode<T>> apply(
	// LogicalDescriptorWithValidAugmentationsNode<T> n) {
	// List<Proposition> reversePrefixPreservingProperAugmentations = new
	// ArrayList<>(n.augmentations.size());
	// List<LogicalDescriptor> refinements = new
	// ArrayList<>(n.augmentations.size());
	// for (int i = n.augmentations.size() - 1; i >= 0; i--) {
	// Proposition a = n.augmentations.get(i);
	// if (n.descriptor.elementIndexList().contains(a.getId())) {
	// continue;
	// }
	// LogicalDescriptor generator = n.descriptor.getSpecialization(a);
	// if (impliesBlocked(generator, a.getId())) {
	// continue;
	// }
	// reversePrefixPreservingProperAugmentations.add(a);
	// refinements.add(closure(generator, a.getId() + 1));
	// }
	// List<Proposition> prefixPreservingProperAugmentations = Lists
	// .reverse(reversePrefixPreservingProperAugmentations);
	// refinements = Lists.reverse(refinements);
	// List<LogicalDescriptorWithValidAugmentationsNode<T>> result = new
	// ArrayList<>(refinements.size());
	// for (int i = 0; i < refinements.size(); i++) {
	// // result.add(new
	// //
	// LogicalDescriptorWithValidAugmentationsNode<T>(toT.apply(refinements.get(i)),
	// // refinements.get(i),
	// // prefixPreservingProperAugmentations.subList(i + 1,
	// // prefixPreservingProperAugmentations.size())));
	// LogicalDescriptor refinement = refinements.get(i);
	// List<Proposition> augEls = props.propositions()
	// .subList(prefixPreservingProperAugmentations.get(i).getId(),
	// props.propositions().size())
	// .stream().filter(filter).filter(a ->
	// !refinement.elementIndexList().contains(a.getId()))
	// // .filter(a ->
	// // prefixPreservingProperAugmentations.contains(a))
	// .collect(Collectors.toList());
	// result.add(new
	// LogicalDescriptorWithValidAugmentationsNode<T>(toT.apply(refinements.get(i)),
	// refinements.get(i), augEls));
	// }
	// return result;
	// }
	// };
	// }

}

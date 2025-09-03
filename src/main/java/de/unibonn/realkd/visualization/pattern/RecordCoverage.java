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
package de.unibonn.realkd.visualization.pattern;

import static com.google.common.collect.Sets.newHashSet;
import static de.unibonn.realkd.common.IndexSets.union;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Set;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.association.Association;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;
import de.unibonn.realkd.patterns.rules.AssociationRule;
import de.unibonn.realkd.visualization.Visualization;

/**
 * Implements a record coverage plot by plotting the entire database on a
 * horizontal axis. The full set of data objects is shown in blue, the objects
 * supported by the pattern is shown in red and the union of the support sets of
 * the pattern is shown in purple.
 *
 * @author Sandy Moens
 * 
 * @since 0.3.0
 * 
 * @version 0.6.0
 */
public class RecordCoverage implements Visualization<Pattern<?>> {

	private static Color COLOR_ALL = Color.blue;
	private static Color COLOR_INTERSECTION = Color.red;
	private static Color COLOR_UNION = Color.magenta.darker();

	private static Set<Proposition> getUniquePropositions(Pattern<?> pattern) {
		Set<Proposition> propositions = newHashSet();
		if (Association.class.isAssignableFrom(pattern.getClass())) {
			propositions.addAll(((Association) pattern).descriptor().elements());
		} else if (AssociationRule.class.isAssignableFrom(pattern.getClass())) {
			propositions.addAll(((AssociationRule) pattern).descriptor().getAntecedent().elements());
			propositions.addAll(((AssociationRule) pattern).descriptor().getConsequent().elements());
		} else if (ExceptionalModelPattern.class.isAssignableFrom(pattern.getClass())) {
			propositions.addAll(((ExceptionalModelPattern) pattern).descriptor().extensionDescriptor().elements());
		}
		return propositions;
	}

	private static IndexSet getSupportSet(Pattern<?> pattern) {
		if (Association.class.isAssignableFrom(pattern.getClass())) {
			return ((Association) pattern).descriptor().supportSet();
		} else if (AssociationRule.class.isAssignableFrom(pattern.getClass())) {
			// TODO check what this does
			if (((AssociationRule) pattern).descriptor().getAntecedent().elements().isEmpty()) {
				return IndexSets.intersection(((AssociationRule) pattern).descriptor().getAntecedent().supportSet(),
						((AssociationRule) pattern).descriptor().getConsequent().supportSet());
			}
			return IndexSets.union(((AssociationRule) pattern).descriptor().getAntecedent().supportSet(),
					((AssociationRule) pattern).descriptor().getConsequent().supportSet());
		} else if (ExceptionalModelPattern.class.isAssignableFrom(pattern.getClass())) {
			return ((ExceptionalModelPattern) pattern).descriptor().extensionDescriptor().supportSet();
		}
		return IndexSets.empty();
	}

	// private static PropositionalLogic getPropositionalLogic(Pattern<?>
	// pattern) {
	// if (Association.class.isAssignableFrom(pattern.getClass())) {
	// return ((Association) pattern).descriptor().getPropositionalLogic();
	// } else if (AssociationRule.class.isAssignableFrom(pattern.getClass())) {
	// return ((AssociationRule) pattern).descriptor().getPropositionalLogic();
	// } else if
	// (ExceptionalModelPattern.class.isAssignableFrom(pattern.getClass())) {
	// return ((ExceptionalModelPattern)
	// pattern).descriptor().extensionDescriptor().getPropositionalLogic();
	// }
	// return null;
	// }

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		// return (pattern.descriptor() instanceof LocalPatternDescriptor
		// && pattern.descriptor() instanceof TableSubspaceDescriptor);
		if (Association.class.isAssignableFrom(pattern.getClass())
				|| AssociationRule.class.isAssignableFrom(pattern.getClass())
				|| ExceptionalModelPattern.class.isAssignableFrom(pattern.getClass())) {
			return true;
		}
		return false;
	}

	@Override
	public BufferedImage getBufferedImage(Pattern<?> pattern, int width, int height) {
		int recordSize = pattern.population().objectIds().size();
		Set<Proposition> propositions = getUniquePropositions(pattern);
		IndexSet supportSet = getSupportSet(pattern);//((LocalPatternDescriptor) pattern.descriptor()).supportSet();
		IndexSet cumulativeSupportSet = IndexSets.empty();
		for (Proposition p : propositions) {
			cumulativeSupportSet = union(cumulativeSupportSet, p.supportSet());
		}

		int myWidth = width - 20, myHeight = height - 10;

		// TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
		// into integer pixels
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D ig2 = bi.createGraphics();

		ig2.setColor(COLOR_ALL);
		ig2.fillRect(10, 5, myWidth, myHeight);
		ig2.setColor(Color.gray);
		ig2.drawRect(10, 5, myWidth, myHeight);

		int x;
		for (int objectId : cumulativeSupportSet) {
			ig2.setColor(COLOR_UNION);
			if (supportSet.contains(objectId)) {
				ig2.setColor(COLOR_INTERSECTION);
			}
			x = 10 + (int) 1. * objectId * myWidth / recordSize;
			ig2.drawLine(x, 5, x, 5 + myHeight);
		}

		return bi;
	}

}

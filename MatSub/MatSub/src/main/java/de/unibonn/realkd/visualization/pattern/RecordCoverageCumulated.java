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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Lists;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.association.Association;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;
import de.unibonn.realkd.patterns.rules.AssociationRule;
import de.unibonn.realkd.visualization.Visualization;

/**
 * Implements a record coverage plot by plotting the entire database on a horizontal axis.
 * The full set of data objects is shown in blue, the objects supported by the pattern is
 * shown in red and the union of the support sets of the pattern is shown in purple. 
 *
 * @author Sandy Moens
 * @since 0.3.0
 * @version 0.3.0
 */
public class RecordCoverageCumulated implements Visualization<Pattern<?>> {

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
	
	private static IndexSet getSupportSet(Pattern<?> pattern, IndexSet smallSet) {
		if (Association.class.isAssignableFrom(pattern.getClass())) {
			return IndexSets.intersection(smallSet, ((Association) pattern).descriptor().supportSet());
		} else if (AssociationRule.class.isAssignableFrom(pattern.getClass())) {
			//TODO check what this does
			if (((AssociationRule) pattern).descriptor().getAntecedent().elements().isEmpty()) {
				return IndexSets.intersection(
						IndexSets.intersection(smallSet,((AssociationRule) pattern).descriptor().getAntecedent().supportSet()),
					((AssociationRule) pattern).descriptor().getConsequent().supportSet());
			} 
			return IndexSets.union(
					IndexSets.intersection(smallSet,((AssociationRule) pattern).descriptor().getAntecedent().supportSet()),
					IndexSets.intersection(smallSet,(((AssociationRule) pattern).descriptor().getConsequent().supportSet())));
		} else if (ExceptionalModelPattern.class.isAssignableFrom(pattern.getClass())) {
			return IndexSets.intersection(smallSet,( ((ExceptionalModelPattern) pattern).descriptor().extensionDescriptor().supportSet()));
		}
		return IndexSets.empty();
	}

//	private static PropositionalLogic getPropositionalLogic(Pattern<?> pattern) {
//		if (Association.class.isAssignableFrom(pattern.getClass())) {
//			return ((Association) pattern).descriptor().getPropositionalLogic();
//		} else if (AssociationRule.class.isAssignableFrom(pattern.getClass())) {
//			return ((AssociationRule) pattern).descriptor().getPropositionalLogic();
//		} else if (ExceptionalModelPattern.class.isAssignableFrom(pattern.getClass())) {
//			return ((ExceptionalModelPattern) pattern).descriptor().extensionDescriptor().getPropositionalLogic();
//		}
//		return null;
//	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		if (Association.class.isAssignableFrom(pattern.getClass())
				|| AssociationRule.class.isAssignableFrom(pattern.getClass())
				|| ExceptionalModelPattern.class.isAssignableFrom(pattern.getClass())) {
			return true;
		}
		return false;
	}

	@Override
	public BufferedImage getBufferedImage(Pattern<?> pattern, int width, int height) {
		IndexSet smallSet = getIndexSet(pattern.population().objectIds()); 
		int recordSize = smallSet.size();
		
		IndexSet supportSet = getSupportSet(pattern, smallSet);
		
		int myWidth = width - 20, myHeight = height - 10;

		// TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
		// into integer pixels
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D ig2 = bi.createGraphics();
		
		int bins = 50;
		
		int rectWidth = recordSize / bins;
		int rectWidthWidth  = myWidth / bins;
		
		ig2.setColor(COLOR_ALL);
		ig2.fillRect(10, 5, myWidth, myHeight);

		int supports[] = new int[bins];
		for(int i : supportSet) {
			supports[Math.min(bins-1, i/rectWidth)] += 1;
		}
		
		for(int i = 0; i < bins; i++) {
			ig2.setColor(new Color(COLOR_INTERSECTION.getRed(), COLOR_INTERSECTION.getGreen(), COLOR_INTERSECTION.getBlue(), Math.min(255, (int)(255.*supports[i]/rectWidth))));
			ig2.fillRect(10 + (i * rectWidthWidth), 5, rectWidthWidth, myHeight);
		}
		
		ig2.setColor(Color.gray);
		ig2.drawRect(10, 5, myWidth, myHeight);
		
		return bi;
	}

	private IndexSet getIndexSet(IndexSet indexSet) {
		if(indexSet.size() < 10000) {
			return indexSet;
		}			
		Collection<Integer> set = Lists.newArrayList();
		int jump = indexSet.size() / 10000;
		for(int i = 0; i < 10000; i++) {
			set.add(jump*i);
		}
		return IndexSets.copyOf(set);
	}

}

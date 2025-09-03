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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.LocalPatternDescriptor;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.TableSubspaceDescriptor;
import de.unibonn.realkd.visualization.Visualization;

/**
 * Plots the attribute values on a parallel coordinates plot by laying out the
 * axis individually next to each other. In this plot only the attributes are
 * shown that are referenced by the pattern. For metric attributes the height
 * indicates the actual value scaled between min and max. For categoric attributes
 * the  * categories are mapped to values between 0 and the number of categories
 * - 1. The full set of data objects is shown in blue, the objects supported by
 * the pattern is shown in red.
 *
 * @author Sandy Moens
 * @since 0.3.0
 * @version 0.3.0
 */
public class ParallelCoordinatesReferencedAttributes implements Visualization<Pattern<?>> {

	private static interface HeightMapper {
		public int getHeight(int id, double scaleFactor);
	}

	private static class MetricAttributeHeightMapper implements HeightMapper {
		private MetricAttribute attribute;
		private double min;
		private double max;

		public MetricAttributeHeightMapper(MetricAttribute attribute) {
			this.attribute = attribute;
			this.min = attribute.min();
			this.max = attribute.max();
		}

		public int getHeight(int id, double scaleFactor) {
			return (int) (scaleFactor * (attribute.value(id) - min) / (max - min));
		}
	}

	private static class CategoricAttributeHeightMapper implements HeightMapper {
		private CategoricAttribute<?> attribute;
		private Map<Object, Double> stages;

		public CategoricAttributeHeightMapper(CategoricAttribute<?> attribute) {
			this.attribute = attribute;
			this.stages = newHashMap();
			int size = attribute.categories().size() - 1;
			int i = 0;
			for (Object o : attribute.categories()) {
				stages.put(o, 1. * i++ / size);
			}
		}

		public int getHeight(int id, double scaleFactor) {
			Object key = this.attribute.getValueOption(id).get();
			if (!this.stages.containsKey(key)) {
				throw new IllegalArgumentException("Key " + key + "not found");
			}
			return (int) (scaleFactor * stages.get(key));
		}
	}

	private static List<HeightMapper> getHeightMappers(Collection<Attribute<?>> attributes) {
		List<Attribute<?>> targets = newArrayList(attributes);
		if (attributes.size() == 1) {
			targets.addAll(attributes);
		}
		List<HeightMapper> heightMappers = newArrayListWithCapacity(targets.size());
		for (Attribute<?> target : targets) {
			if (MetricAttribute.class.isAssignableFrom(target.getClass())) {
				heightMappers.add(new MetricAttributeHeightMapper((MetricAttribute) target));
			} else { // CategoricAttribute.class.isAssignableFrom(target.getClass())
				heightMappers.add(new CategoricAttributeHeightMapper((CategoricAttribute<?>) target));
			}
		}
		return heightMappers;
	}

	private static void drawLines(IndexSet objectIds, int height, Graphics2D ig2, List<HeightMapper> mappers,
			int fWidth) {
		for (int objectId : objectIds) {
			for (int i = 0; i < mappers.size() - 1; i++) {
				try {
					ig2.drawLine(10 + i * fWidth, height + 5 - mappers.get(i).getHeight(objectId, height),
							10 + (i + 1) * fWidth, height + 5 - mappers.get(i + 1).getHeight(objectId, height));
				} catch (IllegalArgumentException e) {
				}
			}
		}
	}

	private static void drawLines(IndexSet objectIds, int height, Graphics2D ig2, List<HeightMapper> mappers,
			int xStart, int fWidth) {
		for (int objectId : objectIds) {
			for (int i = 0; i < mappers.size() - 1; i++) {
				try {
					ig2.drawLine(xStart + i * fWidth, height + 5 - mappers.get(i).getHeight(objectId, height),
							xStart + (i + 1) * fWidth, height + 5 - mappers.get(i + 1).getHeight(objectId, height));
				} catch (IllegalArgumentException e) {
				}
			}
		}
	}

//	private static Collection<Attribute<?>> getOrderedReferencedAttributes(Pattern<?> pattern) {
//		List<Attribute<?>> attributes = newArrayList();
//		if (Association.class.isAssignableFrom(pattern.getClass())) {
//			attributes.addAll(((Association) pattern).descriptor().getReferencedAttributes());
//		} else if (AssociationRule.class.isAssignableFrom(pattern.getClass())) {
//			attributes.addAll(((AssociationRule) pattern).descriptor().getAntecedent().getReferencedAttributes());
//			attributes.addAll(((AssociationRule) pattern).descriptor().getConsequent().getReferencedAttributes());
//		} else if (ExceptionalModelPattern.class.isAssignableFrom(pattern.getClass())) {
//			attributes.addAll(((ExceptionalModelPattern) pattern).descriptor().targetAttributes());
//			attributes.addAll(
//					((ExceptionalModelPattern) pattern).descriptor().extensionDescriptor().getReferencedAttributes());
//		}
//		return attributes;
//	}

//	private static IndexSet getSupportSet(Pattern<?> pattern) {
//		if (Association.class.isAssignableFrom(pattern.getClass())) {
//			return ((Association) pattern).descriptor().supportSet();
//		} else if (AssociationRule.class.isAssignableFrom(pattern.getClass())) {
//			return IndexSets.intersection(
//					((AssociationRule) pattern).descriptor().getAntecedent().supportSet(),
//					((AssociationRule) pattern).descriptor().getConsequent().supportSet());
//		} else if (ExceptionalModelPattern.class.isAssignableFrom(pattern.getClass())) {
//			return ((ExceptionalModelPattern) pattern).descriptor().extensionDescriptor().supportSet();
//		}
//		return IndexSets.empty();
//	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		return (pattern.descriptor() instanceof TableSubspaceDescriptor && pattern.descriptor() instanceof LocalPatternDescriptor);
//		return  (isApplicableAssociation(pattern) || isApplicableAssociationRule(pattern)
//				|| isApplicableExceptionalModel(pattern));
	}

	@Override
	public BufferedImage getBufferedImage(Pattern<?> pattern, int width, int height) {
//		if (ExceptionalModelPattern.class.isAssignableFrom(pattern.getClass())) {
//			return draw((ExceptionalModelPattern) pattern, width, height);
//		}
		IndexSet objectIds = pattern.population().objectIds();
		Collection<Attribute<?>> attributes = ((TableSubspaceDescriptor) pattern.descriptor()).getReferencedAttributes();
		IndexSet supportSet = ((LocalPatternDescriptor) pattern.descriptor()).supportSet();
//		IndexSet supportSet = getSupportSet(pattern);

		int myWidth = width - 20, myHeight = height - 10;

		// TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
		// into integer pixels
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D ig2 = bi.createGraphics();
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		ig2.setRenderingHints(rh);

		ig2.setColor(Color.gray);
		ig2.drawRect(10, 5, myWidth, myHeight);

		List<HeightMapper> heightMappers = getHeightMappers(attributes);
		int fWidth = (int) (1. * myWidth / (heightMappers.size() - 1));

		ig2.setColor(Color.blue);
		drawLines(objectIds, myHeight, ig2, heightMappers, fWidth);
		ig2.setColor(Color.red);
		drawLines(supportSet, myHeight, ig2, heightMappers, fWidth);

		return bi;
	}

//	private BufferedImage draw(ExceptionalModelPattern pattern, int width, int height) {
//		IndexSet objectIds = pattern.population().objectIds();
//		Collection<Attribute<?>> targetAttributes = pattern.descriptor().targetAttributes();
//		Collection<Attribute<?>> subgroupAttributes = pattern.descriptor().extensionDescriptor()
//				.getReferencedAttributes();
//		IndexSet supportSet = getSupportSet(pattern);
//
//		int myWidth = width - 20, myHeight = height - 10;
//
//		// TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
//		// into integer pixels
//		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//
//		Graphics2D ig2 = bi.createGraphics();
//		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		ig2.setRenderingHints(rh);
//
//		List<HeightMapper> heightMappersTargets = getHeightMappers(targetAttributes);
//		List<HeightMapper> heightMappersSubgroup = getHeightMappers(subgroupAttributes);
//
//		int fWidth = (int) (1. * myWidth / (heightMappersTargets.size() + heightMappersSubgroup.size() - 1));
//
//		ig2.setColor(Color.gray);
//		ig2.drawRect(10, 5, fWidth * (heightMappersTargets.size() - 1), myHeight);
//		ig2.drawRect(10 + fWidth * heightMappersTargets.size(), 5, fWidth * (heightMappersSubgroup.size() - 1),
//				myHeight);
//		ig2.drawLine(10 + fWidth * (heightMappersTargets.size() - 1), 5 + (int) (myHeight / 2.0),
//				10 + fWidth * (heightMappersTargets.size()), 5 + (int) (myHeight / 2.0));
//
//		ig2.setColor(Color.blue);
//		drawLines(objectIds, myHeight, ig2, heightMappersTargets, 10, fWidth);
//		ig2.setColor(Color.red);
//		drawLines(supportSet, myHeight, ig2, heightMappersTargets, 10, fWidth);
//
//		ig2.setColor(Color.blue);
//		drawLines(objectIds, myHeight, ig2, heightMappersSubgroup, 10 + (fWidth * heightMappersTargets.size()), fWidth);
//		ig2.setColor(Color.red);
//		drawLines(supportSet, myHeight, ig2, heightMappersSubgroup, 10 + (fWidth * heightMappersTargets.size()),
//				fWidth);
//
//		return bi;
//	}

}

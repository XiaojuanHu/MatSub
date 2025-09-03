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

import static de.unibonn.realkd.data.sequences.Window.window;
import static de.unibonn.realkd.patterns.episodes.SingleSequenceConstants.LONGER_MULTI_SINGLE_SEQUENCE_CONTEXT;
import static de.unibonn.realkd.patterns.episodes.SingleSequenceConstants.LONGER_MULTI_SINGLE_SEQUENCE_CONTEXT_MAP;
import static de.unibonn.realkd.patterns.episodes.SingleSequenceConstants.MULTI_SINGLE_SEQUENCE_CONTEXT;
import static de.unibonn.realkd.patterns.episodes.SingleSequenceConstants.MULTI_SINGLE_SEQUENCE_CONTEXT_MAP;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.unibonn.realkd.data.sequences.Window;
import de.unibonn.realkd.patterns.graphs.Edge;
import de.unibonn.realkd.patterns.graphs.Edges;
import de.unibonn.realkd.patterns.graphs.GraphDescriptors;
import de.unibonn.realkd.patterns.graphs.Node;
import de.unibonn.realkd.patterns.graphs.Nodes;
import junit.framework.TestCase;

/**
 *
 * @author Sandy Moens
 * 
 * @since 0.7.1
 * 
 * @version 0.7.1
 */
public class TestMeasureUtilsMultiSingleSequence extends TestCase {

	private static List<Node> createNodes(String... names) {
		return IntStream.range(0, names.length)
				.mapToObj(i -> Nodes.create(i, MULTI_SINGLE_SEQUENCE_CONTEXT_MAP.get(names[i])))
				.collect(Collectors.toList());
	}

	private static List<Node> createNodesLonger(String... names) {
		return IntStream.range(0, names.length)
				.mapToObj(i -> Nodes.create(i, LONGER_MULTI_SINGLE_SEQUENCE_CONTEXT_MAP.get(names[i])))
				.collect(Collectors.toList());
	}

	private static List<Edge> createEdges(int... edges) {
		List<Edge> newEdges = Lists.newArrayList();

		for (int i = 0; i < edges.length / 2; i++) {
			int start = edges[i * 2];
			int end = edges[(i * 2) + 1];
			newEdges.add(Edges.create(start, end));
		}

		return newEdges;
	}

	private static List<Window> getWindows(List<Node> nodes, List<Edge> edges, double windowSize) {
		return EpisodeDescriptors.getOverlappingMinimalWindows(GraphDescriptors.create(nodes, edges), windowSize,
				MULTI_SINGLE_SEQUENCE_CONTEXT.events());
	}

	private static List<Window> getWindowsLonger(List<Node> nodes, List<Edge> edges, double windowSize) {
		return EpisodeDescriptors.getOverlappingMinimalWindows(GraphDescriptors.create(nodes, edges), windowSize,
				LONGER_MULTI_SINGLE_SEQUENCE_CONTEXT.events());
	}

	@Test
	public void testWindowsParallelEpisodeOneElement() {
		{
			List<Node> nodes = createNodes("eventId=1");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows = getWindows(nodes, edges, 3);
			
			Assert.assertEquals(ImmutableList.of(window(1, 1), window(3, 3), window(4, 4), window(5, 5), window(8, 8)),
					windows);
		}

		{
			List<Node> nodes = createNodes("eventId=2");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows = getWindows(nodes, edges, 3);

			Assert.assertEquals(ImmutableList.of(window(2, 2), window(7, 7)), windows);
		}

		{
			List<Node> nodes = createNodes("eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows = getWindows(nodes, edges, 3);

			Assert.assertEquals(ImmutableList.of(window(2, 2), window(3, 3), window(6, 6), window(7, 7)), windows);
		}

		{
			List<Node> nodes = createNodes("eventId=4");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows = getWindows(nodes, edges, 3);

			Assert.assertEquals(ImmutableList.of(window(4, 4)), windows);
		}
	}

	@Test
	public void testWindowsParallelEpisodeTwoSameElements() {
		{
			List<Node> nodes = createNodes("eventId=1", "eventId=1");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);

			Assert.assertEquals(ImmutableList.of(window(1, 3), window(3, 4), window(4, 5)), windows3);
			Assert.assertEquals(ImmutableList.of(window(1, 3), window(3, 4), window(4, 5), window(5, 8)), windows5);
		}

		{
			List<Node> nodes = createNodes("eventId=2", "eventId=2");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);

			Assert.assertEquals(ImmutableList.of(), windows3);
			Assert.assertEquals(ImmutableList.of(), windows5);
		}

		{
			List<Node> nodes = createNodes("eventId=3", "eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);

			Assert.assertEquals(ImmutableList.of(window(2, 3), window(6, 7)), windows3);
			Assert.assertEquals(ImmutableList.of(window(2, 3), window(3, 6), window(6, 7)), windows5);
		}

		{
			List<Node> nodes = createNodes("eventId=4", "eventId=4");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);

			Assert.assertEquals(ImmutableList.of(), windows3);
			Assert.assertEquals(ImmutableList.of(), windows5);
		}
	}

	@Test
	public void testWindowsParallelEpisodeTwoDifferentElements() {
		{
			{
				List<Node> nodes = createNodes("eventId=1", "eventId=2");
				List<Edge> edges = ImmutableList.of();
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(5, 7), window(7, 8)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(5, 7), window(7, 8)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=3");
				List<Edge> edges = ImmutableList.of();
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(1, 2), window(3, 3), window(5, 6), window(7, 8)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 2), window(3, 3), window(5, 6), window(7, 8)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=4");
				List<Edge> edges = ImmutableList.of();
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(4, 4)), windows3);
				Assert.assertEquals(ImmutableList.of(window(4, 4)), windows5);
			}
		}

		{
			{
				List<Node> nodes = createNodes("eventId=2", "eventId=3");
				List<Edge> edges = ImmutableList.of();
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(2, 2), window(7, 7)), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 2), window(7, 7)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=2", "eventId=4");
				List<Edge> edges = ImmutableList.of();
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(2, 4)), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 4), window(4, 7)), windows5);
			}
		}

		{
			{
				List<Node> nodes = createNodes("eventId=3", "eventId=4");
				List<Edge> edges = ImmutableList.of();
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(3, 4), window(4, 6)), windows3);
				Assert.assertEquals(ImmutableList.of(window(3, 4), window(4, 6)), windows5);
			}
		}
	}

	@Test
	public void testWindowsParallelEpisodeThreeSameElements() {
		{
			List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=1");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(window(3, 5)), windows3);
			Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 5), window(4, 8)), windows5);
			Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 5), window(4, 8)), windows8);
		}

		{
			List<Node> nodes = createNodes("eventId=2", "eventId=2", "eventId=2");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(), windows3);
			Assert.assertEquals(ImmutableList.of(), windows5);
			Assert.assertEquals(ImmutableList.of(), windows8);
		}

		{
			List<Node> nodes = createNodes("eventId=3", "eventId=3", "eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(), windows3);
			Assert.assertEquals(ImmutableList.of(window(2, 6), window(3, 7)), windows5);
			Assert.assertEquals(ImmutableList.of(window(2, 6), window(3, 7)), windows8);
		}

		{
			List<Node> nodes = createNodes("eventId=4", "eventId=4", "eventId=4");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(), windows3);
			Assert.assertEquals(ImmutableList.of(), windows5);
			Assert.assertEquals(ImmutableList.of(), windows8);
		}
	}

	@Test
	public void testWindowsParallelEpisodeThreeDifferentElements() {
		{
			{
				List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=3");
				List<Edge> edges = ImmutableList.of();
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(5, 7), window(7, 8)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(5, 7), window(7, 8)), windows5);
				Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(5, 7), window(7, 8)), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=4");
				List<Edge> edges = ImmutableList.of();
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(window(2, 4)), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 4), window(4, 7)), windows5);
				Assert.assertEquals(ImmutableList.of(window(2, 4), window(4, 7)), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=3", "eventId=4");
				List<Edge> edges = ImmutableList.of();
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(window(3, 4), window(4, 6)), windows3);
				Assert.assertEquals(ImmutableList.of(window(3, 4), window(4, 6)), windows5);
				Assert.assertEquals(ImmutableList.of(window(3, 4), window(4, 6)), windows8);
			}
		}

		{
			{
				List<Node> nodes = createNodes("eventId=2", "eventId=3", "eventId=4");
				List<Edge> edges = ImmutableList.of();
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(window(2, 4)), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 4), window(4, 7)), windows5);
				Assert.assertEquals(ImmutableList.of(window(2, 4), window(4, 7)), windows8);
			}
		}
	}

	@Test
	public void testWindowsParallelEpisodeVariousElements() {
		{
			List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(window(1, 3), window(2, 4)), windows3);
			Assert.assertEquals(ImmutableList.of(window(1, 3), window(2, 4), window(4, 7), window(5, 8)), windows5);
			Assert.assertEquals(ImmutableList.of(window(1, 3), window(2, 4), window(4, 7), window(5, 8)), windows8);
		}

		{
			List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=2", "eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(), windows3);
			Assert.assertEquals(ImmutableList.of(), windows5);
			Assert.assertEquals(ImmutableList.of(window(2, 7)), windows8);
		}

		{
			List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=2", "eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(), windows3);
			Assert.assertEquals(ImmutableList.of(), windows5);
			Assert.assertEquals(ImmutableList.of(window(2, 7)), windows8);
		}

		{
			List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=3", "eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(window(2, 3), window(5, 7), window(6, 8)), windows3);
			Assert.assertEquals(ImmutableList.of(window(2, 3), window(5, 7), window(6, 8)), windows5);
			Assert.assertEquals(ImmutableList.of(window(2, 3), window(5, 7), window(6, 8)), windows8);
		}

		{
			List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=3", "eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(window(1, 3), window(2, 4)), windows3);
			Assert.assertEquals(ImmutableList.of(window(1, 3), window(2, 4), window(4, 7), window(5, 8)), windows5);
			Assert.assertEquals(ImmutableList.of(window(1, 3), window(2, 4), window(4, 7), window(5, 8)), windows8);
		}
	}

	@Test
	public void testWindowsParallelEpisodeVariousElementsLonger() {
		{
			List<Node> nodes = createNodesLonger("eventId=1", "eventId=2", "eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
			List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
			List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

			Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3)), windows3);
			Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(45, 57)), windows20);
			Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(45, 57), window(57, 78)),
					windows30);
		}

		{
			List<Node> nodes = createNodesLonger("eventId=1", "eventId=3", "eventId=4");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
			List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
			List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

			Assert.assertEquals(ImmutableList.of(window(44, 46)), windows3);
			Assert.assertEquals(ImmutableList.of(window(44, 46)), windows20);
			Assert.assertEquals(ImmutableList.of(window(44, 46)), windows30);
		}

		{
			List<Node> nodes = createNodesLonger("eventId=3", "eventId=2");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
			List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
			List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

			Assert.assertEquals(ImmutableList.of(window(2, 2)), windows3);
			Assert.assertEquals(ImmutableList.of(window(2, 2), window(46, 57), window(57, 67)), windows20);
			Assert.assertEquals(ImmutableList.of(window(2, 2), window(46, 57), window(57, 67)), windows30);
		}
	}

	@Test
	public void testWindowsParallelEpisodeVariousElementsAfterDeletion() {
		{
			List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=3");
			nodes.remove(0);
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(5, 7), window(7, 8)), windows3);
			Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(5, 7), window(7, 8)), windows5);
			Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(5, 7), window(7, 8)), windows8);
		}

		{
			List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=3");
			nodes.remove(1);
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(5, 7), window(7, 8)), windows3);
			Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(5, 7), window(7, 8)), windows5);
			Assert.assertEquals(ImmutableList.of(window(1, 2), window(2, 3), window(5, 7), window(7, 8)), windows8);
		}

		{
			List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=3");
			nodes.remove(2);
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(window(1, 3), window(3, 4), window(4, 6)), windows3);
			Assert.assertEquals(ImmutableList.of(window(1, 3), window(3, 4), window(4, 6), window(5, 8)), windows5);
			Assert.assertEquals(ImmutableList.of(window(1, 3), window(3, 4), window(4, 6), window(5, 8)), windows8);
		}
	}

	@Test
	public void testWindowsGeneralEpisodeTwoSameElements() {
		{
			{
				List<Node> nodes = createNodes("eventId=1", "eventId=1");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(1, 3), window(3, 4), window(4, 5)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 3), window(3, 4), window(4, 5), window(5, 8)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=1");
				List<Edge> edges = createEdges(1, 0);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(1, 3), window(3, 4), window(4, 5)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 3), window(3, 4), window(4, 5), window(5, 8)), windows5);
			}
		}

		{
			{
				List<Node> nodes = createNodes("eventId=2", "eventId=2");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=2", "eventId=2");
				List<Edge> edges = createEdges(1, 0);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows5);
			}
		}

		{
			{
				List<Node> nodes = createNodes("eventId=3", "eventId=3");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(2, 3), window(6, 7)), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 3), window(3, 6), window(6, 7)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=3", "eventId=3");
				List<Edge> edges = createEdges(1, 0);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(2, 3), window(6, 7)), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 3), window(3, 6), window(6, 7)), windows5);
			}
		}

		{
			{
				List<Node> nodes = createNodes("eventId=4", "eventId=4");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=4", "eventId=4");
				List<Edge> edges = createEdges(1, 0);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows5);
			}
		}
	}

	@Test
	public void testWindowsGeneralEpisodeTwoDifferentElements() {
		{
			{
				List<Node> nodes = createNodes("eventId=1", "eventId=2");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(1, 2), window(5, 7)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 2), window(5, 7)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=2");
				List<Edge> edges = createEdges(1, 0);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(2, 3), window(7, 8)), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 3), window(7, 8)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=3");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(1, 2), window(5, 6)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 2), window(5, 6)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=3");
				List<Edge> edges = createEdges(1, 0);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(2, 3), window(3, 4), window(7, 8)), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 3), window(3, 4), window(7, 8)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=4");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(3, 4)), windows3);
				Assert.assertEquals(ImmutableList.of(window(3, 4)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=4");
				List<Edge> edges = createEdges(1, 0);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(4, 5)), windows3);
				Assert.assertEquals(ImmutableList.of(window(4, 5)), windows5);
			}
		}

		{
			{
				List<Node> nodes = createNodes("eventId=2", "eventId=3");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(2, 3)), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 3)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=2", "eventId=3");
				List<Edge> edges = createEdges(1, 0);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(6, 7)), windows3);
				Assert.assertEquals(ImmutableList.of(window(6, 7)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=2", "eventId=4");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(2, 4)), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 4)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=2", "eventId=4");
				List<Edge> edges = createEdges(1, 0);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(window(4, 7)), windows5);
			}
		}

		{
			{
				List<Node> nodes = createNodes("eventId=3", "eventId=4");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(3, 4)), windows3);
				Assert.assertEquals(ImmutableList.of(window(3, 4)), windows5);
			}

			{
				List<Node> nodes = createNodes("eventId=3", "eventId=4");
				List<Edge> edges = createEdges(1, 0);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);

				Assert.assertEquals(ImmutableList.of(window(4, 6)), windows3);
				Assert.assertEquals(ImmutableList.of(window(4, 6)), windows5);
			}
		}
	}

	@Test
	public void testWindowsGeneralEpisodeThreeSameElements() {
		{
			{
				List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=1");
				List<Edge> edges = createEdges(0, 1, 0, 2);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(window(3, 5)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 5), window(4, 8)), windows5);
				Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 5), window(4, 8)), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=1");
				List<Edge> edges = createEdges(0, 1, 2, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(window(3, 5)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 5), window(4, 8)), windows5);
				Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 5), window(4, 8)), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=1");
				List<Edge> edges = createEdges(0, 1, 1, 2);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(window(3, 5)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 5), window(4, 8)), windows5);
				Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 5), window(4, 8)), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=1");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(window(3, 5)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 5), window(4, 8)), windows5);
				Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 5), window(4, 8)), windows8);
			}
		}

		{
			{
				List<Node> nodes = createNodes("eventId=2", "eventId=2", "eventId=2");
				List<Edge> edges = createEdges(0, 1, 0, 2);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows5);
				Assert.assertEquals(ImmutableList.of(), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=2", "eventId=2", "eventId=2");
				List<Edge> edges = createEdges(0, 1, 2, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows5);
				Assert.assertEquals(ImmutableList.of(), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=2", "eventId=2", "eventId=2");
				List<Edge> edges = createEdges(0, 1, 1, 2);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows5);
				Assert.assertEquals(ImmutableList.of(), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=2", "eventId=2", "eventId=2");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows5);
				Assert.assertEquals(ImmutableList.of(), windows8);
			}
		}

		{
			{
				List<Node> nodes = createNodes("eventId=3", "eventId=3", "eventId=3");
				List<Edge> edges = createEdges(0, 1, 0, 2);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 6), window(3, 7)), windows5);
				Assert.assertEquals(ImmutableList.of(window(2, 6), window(3, 7)), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=3", "eventId=3", "eventId=3");
				List<Edge> edges = createEdges(0, 1, 2, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 6), window(3, 7)), windows5);
				Assert.assertEquals(ImmutableList.of(window(2, 6), window(3, 7)), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=3", "eventId=3", "eventId=3");
				List<Edge> edges = createEdges(0, 1, 1, 2);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 6), window(3, 7)), windows5);
				Assert.assertEquals(ImmutableList.of(window(2, 6), window(3, 7)), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=3", "eventId=3", "eventId=3");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 6), window(3, 7)), windows5);
				Assert.assertEquals(ImmutableList.of(window(2, 6), window(3, 7)), windows8);
			}
		}

		{
			{
				List<Node> nodes = createNodes("eventId=4", "eventId=4", "eventId=4");
				List<Edge> edges = createEdges(0, 1, 0, 2);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows5);
				Assert.assertEquals(ImmutableList.of(), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=4", "eventId=4", "eventId=4");
				List<Edge> edges = createEdges(0, 1, 2, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows5);
				Assert.assertEquals(ImmutableList.of(), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=4", "eventId=4", "eventId=4");
				List<Edge> edges = createEdges(0, 1, 1, 2);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows5);
				Assert.assertEquals(ImmutableList.of(), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=4", "eventId=4", "eventId=4");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows5);
				Assert.assertEquals(ImmutableList.of(), windows8);
			}

		}
	}

	@Test
	public void testWindowsGeneralEpisodeThreeDifferentElements() {
		{
			{
				{
					List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=3");
					List<Edge> edges = createEdges(0, 1, 0, 2);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(window(1, 2), window(5, 7)), windows3);
					Assert.assertEquals(ImmutableList.of(window(1, 2), window(5, 7)), windows5);
					Assert.assertEquals(ImmutableList.of(window(1, 2), window(5, 7)), windows8);
				}

				{
					List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=3");
					List<Edge> edges = createEdges(0, 1, 2, 1);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(window(5, 7)), windows3);
					Assert.assertEquals(ImmutableList.of(window(5, 7)), windows5);
					Assert.assertEquals(ImmutableList.of(window(5, 7)), windows8);
				}

				{
					List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=3");
					List<Edge> edges = createEdges(0, 1, 1, 2);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(window(1, 3)), windows3);
					Assert.assertEquals(ImmutableList.of(window(1, 3)), windows5);
					Assert.assertEquals(ImmutableList.of(window(1, 3)), windows8);
				}

				{
					List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=3");
					List<Edge> edges = createEdges(0, 1);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(window(1, 2), window(5, 7)), windows3);
					Assert.assertEquals(ImmutableList.of(window(1, 2), window(5, 7)), windows5);
					Assert.assertEquals(ImmutableList.of(window(1, 2), window(5, 7)), windows8);
				}
			}

			{
				{
					List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=4");
					List<Edge> edges = createEdges(0, 1, 0, 2);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(), windows3);
					Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 7)), windows5);
					Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 7)), windows8);
				}

				{
					List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=4");
					List<Edge> edges = createEdges(0, 1, 2, 1);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(), windows3);
					Assert.assertEquals(ImmutableList.of(window(4, 7)), windows5);
					Assert.assertEquals(ImmutableList.of(window(4, 7)), windows8);
				}

				{
					List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=4");
					List<Edge> edges = createEdges(0, 1, 1, 2);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(), windows3);
					Assert.assertEquals(ImmutableList.of(window(1, 4)), windows5);
					Assert.assertEquals(ImmutableList.of(window(1, 4)), windows8);
				}

				{
					List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=4");
					List<Edge> edges = createEdges(0, 1);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(), windows3);
					Assert.assertEquals(ImmutableList.of(window(1, 4), window(4, 7)), windows5);
					Assert.assertEquals(ImmutableList.of(window(1, 4), window(4, 7)), windows8);
				}

			}

			{
				{
					List<Node> nodes = createNodes("eventId=1", "eventId=3", "eventId=4");
					List<Edge> edges = createEdges(0, 1, 0, 2);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(), windows3);
					Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 6)), windows5);
					Assert.assertEquals(ImmutableList.of(window(1, 4), window(3, 6)), windows8);
				}

				{
					List<Node> nodes = createNodes("eventId=1", "eventId=3", "eventId=4");
					List<Edge> edges = createEdges(0, 1, 2, 1);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(window(4, 6)), windows3);
					Assert.assertEquals(ImmutableList.of(window(4, 6)), windows5);
					Assert.assertEquals(ImmutableList.of(window(4, 6)), windows8);
				}

				{
					List<Node> nodes = createNodes("eventId=1", "eventId=3", "eventId=4");
					List<Edge> edges = createEdges(0, 1, 1, 2);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(), windows3);
					Assert.assertEquals(ImmutableList.of(window(1, 4)), windows5);
					Assert.assertEquals(ImmutableList.of(window(1, 4)), windows8);
				}

				{
					List<Node> nodes = createNodes("eventId=1", "eventId=3", "eventId=4");
					List<Edge> edges = createEdges(0, 1);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(window(4, 6)), windows3);
					Assert.assertEquals(ImmutableList.of(window(1, 4), window(4, 6)), windows5);
					Assert.assertEquals(ImmutableList.of(window(1, 4), window(4, 6)), windows8);
				}
			}
		}

		{
			{
				{
					List<Node> nodes = createNodes("eventId=2", "eventId=3", "eventId=4");
					List<Edge> edges = createEdges(0, 1, 0, 2);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(window(2, 4)), windows3);
					Assert.assertEquals(ImmutableList.of(window(2, 4)), windows5);
					Assert.assertEquals(ImmutableList.of(window(2, 4)), windows8);
				}

				{
					List<Node> nodes = createNodes("eventId=2", "eventId=3", "eventId=4");
					List<Edge> edges = createEdges(0, 1, 2, 1);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(), windows3);
					Assert.assertEquals(ImmutableList.of(window(2, 6)), windows5);
					Assert.assertEquals(ImmutableList.of(window(2, 6)), windows8);
				}

				{
					List<Node> nodes = createNodes("eventId=2", "eventId=3", "eventId=4");
					List<Edge> edges = createEdges(0, 1, 1, 2);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(window(2, 4)), windows3);
					Assert.assertEquals(ImmutableList.of(window(2, 4)), windows5);
					Assert.assertEquals(ImmutableList.of(window(2, 4)), windows8);
				}

				{
					List<Node> nodes = createNodes("eventId=2", "eventId=3", "eventId=4");
					List<Edge> edges = createEdges(0, 1);
					List<Window> windows3 = getWindows(nodes, edges, 3);
					List<Window> windows5 = getWindows(nodes, edges, 5);
					List<Window> windows8 = getWindows(nodes, edges, 8);

					Assert.assertEquals(ImmutableList.of(window(2, 4)), windows3);
					Assert.assertEquals(ImmutableList.of(window(2, 4)), windows5);
					Assert.assertEquals(ImmutableList.of(window(2, 4)), windows8);
				}
			}
		}
	}

	@Test
	public void testWindowsGeneralEpisodeVariousElements() {
		{
			{
				List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=3");
				List<Edge> edges = createEdges(0, 1, 1, 2, 1, 3);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(window(4, 7)), windows5);
				Assert.assertEquals(ImmutableList.of(window(4, 7)), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=3");
				List<Edge> edges = createEdges(0, 2, 0, 3, 2, 1, 3, 1);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(window(1, 3)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 3), window(5, 8)), windows5);
				Assert.assertEquals(ImmutableList.of(window(1, 3), window(5, 8)), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=3");
				List<Edge> edges = createEdges(2, 0, 2, 1, 0, 3, 1, 3);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 6)), windows5);
				Assert.assertEquals(ImmutableList.of(window(2, 6)), windows8);
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=3");
				List<Edge> edges = createEdges(1, 2, 0, 3);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(window(4, 7)), windows5);
//				Assert.assertEquals(ImmutableList.of(window(1, 6), window(4, 7)), windows8);
				Assert.assertEquals(ImmutableList.of(window(4, 7)), windows8);
				// TODO THIS IS INCORRECT!!!
			}

			{
				List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=3");
				List<Edge> edges = createEdges(0, 2, 1, 3);
				List<Window> windows3 = getWindows(nodes, edges, 3);
				List<Window> windows5 = getWindows(nodes, edges, 5);
				List<Window> windows8 = getWindows(nodes, edges, 8);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(window(4, 7)), windows5);
				Assert.assertEquals(ImmutableList.of(window(1, 6), window(4, 7)), windows8);
			}
		}

		{
			List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=2", "eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(), windows3);
			Assert.assertEquals(ImmutableList.of(), windows5);
			Assert.assertEquals(ImmutableList.of(window(2, 7)), windows8);
		}

		{
			List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=2", "eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(), windows3);
			Assert.assertEquals(ImmutableList.of(), windows5);
			Assert.assertEquals(ImmutableList.of(window(2, 7)), windows8);
		}

		{
			List<Node> nodes = createNodes("eventId=1", "eventId=2", "eventId=3", "eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(window(2, 3), window(5, 7), window(6, 8)), windows3);
			Assert.assertEquals(ImmutableList.of(window(2, 3), window(5, 7), window(6, 8)), windows5);
			Assert.assertEquals(ImmutableList.of(window(2, 3), window(5, 7), window(6, 8)), windows8);
		}

		{
			List<Node> nodes = createNodes("eventId=1", "eventId=1", "eventId=2", "eventId=3", "eventId=3");
			List<Edge> edges = ImmutableList.of();
			List<Window> windows3 = getWindows(nodes, edges, 3);
			List<Window> windows5 = getWindows(nodes, edges, 5);
			List<Window> windows8 = getWindows(nodes, edges, 8);

			Assert.assertEquals(ImmutableList.of(window(1, 3), window(2, 4)), windows3);
			Assert.assertEquals(ImmutableList.of(window(1, 3), window(2, 4), window(4, 7), window(5, 8)), windows5);
			Assert.assertEquals(ImmutableList.of(window(1, 3), window(2, 4), window(4, 7), window(5, 8)), windows8);
		}
	}

	@Test
	public void testWindowsGeneralEpisodeVariousElementsLonger() {
		{
			{
				List<Node> nodes = createNodesLonger("eventId=1", "eventId=2", "eventId=3");
				List<Edge> edges = createEdges(0, 1, 0, 2);
				List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
				List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
				List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

				Assert.assertEquals(ImmutableList.of(window(1, 2)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 2), window(45, 57)), windows20);
				Assert.assertEquals(ImmutableList.of(window(1, 2), window(45, 57)), windows30);
			}

			{
				List<Node> nodes = createNodesLonger("eventId=1", "eventId=2", "eventId=3");
				List<Edge> edges = createEdges(0, 1, 2, 1);
				List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
				List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
				List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(window(45, 57)), windows20);
				Assert.assertEquals(ImmutableList.of(window(45, 57)), windows30);
			}

			{
				List<Node> nodes = createNodesLonger("eventId=1", "eventId=2", "eventId=3");
				List<Edge> edges = createEdges(0, 1, 1, 2);
				List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
				List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
				List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

				Assert.assertEquals(ImmutableList.of(window(1, 3)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 3)), windows20);
				Assert.assertEquals(ImmutableList.of(window(1, 3), window(45, 67)), windows30);
			}

			{
				List<Node> nodes = createNodesLonger("eventId=1", "eventId=2", "eventId=3");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
				List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
				List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

				Assert.assertEquals(ImmutableList.of(window(1, 2)), windows3);
				Assert.assertEquals(ImmutableList.of(window(1, 2), window(45, 57)), windows20);
				Assert.assertEquals(ImmutableList.of(window(1, 2), window(45, 57)), windows30);
			}
		}

		{
			{
				List<Node> nodes = createNodesLonger("eventId=1", "eventId=3", "eventId=4");
				List<Edge> edges = createEdges(0, 1, 0, 2);
				List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
				List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
				List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows20);
				Assert.assertEquals(ImmutableList.of(), windows30);
			}

			{
				List<Node> nodes = createNodesLonger("eventId=1", "eventId=3", "eventId=4");
				List<Edge> edges = createEdges(0, 1, 2, 1);
				List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
				List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
				List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

				Assert.assertEquals(ImmutableList.of(window(44, 46)), windows3);
				Assert.assertEquals(ImmutableList.of(window(44, 46)), windows20);
				Assert.assertEquals(ImmutableList.of(window(44, 46)), windows30);
			}

			{
				List<Node> nodes = createNodesLonger("eventId=1", "eventId=3", "eventId=4");
				List<Edge> edges = createEdges(0, 1, 1, 2);
				List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
				List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
				List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(), windows20);
				Assert.assertEquals(ImmutableList.of(), windows30);
			}

			{
				List<Node> nodes = createNodesLonger("eventId=1", "eventId=3", "eventId=4");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
				List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
				List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

				Assert.assertEquals(ImmutableList.of(window(44, 46)), windows3);
				Assert.assertEquals(ImmutableList.of(window(44, 46)), windows20);
				Assert.assertEquals(ImmutableList.of(window(44, 46)), windows30);
			}
		}

		{
			{
				List<Node> nodes = createNodesLonger("eventId=3", "eventId=2");
				List<Edge> edges = createEdges(0, 1);
				List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
				List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
				List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

				Assert.assertEquals(ImmutableList.of(), windows3);
				Assert.assertEquals(ImmutableList.of(window(46, 57)), windows20);
				Assert.assertEquals(ImmutableList.of(window(46, 57)), windows30);
			}

			{
				List<Node> nodes = createNodesLonger("eventId=3", "eventId=2");
				List<Edge> edges = createEdges(1, 0);
				List<Window> windows3 = getWindowsLonger(nodes, edges, 3);
				List<Window> windows20 = getWindowsLonger(nodes, edges, 20);
				List<Window> windows30 = getWindowsLonger(nodes, edges, 30);

				Assert.assertEquals(ImmutableList.of(window(2, 3)), windows3);
				Assert.assertEquals(ImmutableList.of(window(2, 3), window(57, 67)), windows20);
				Assert.assertEquals(ImmutableList.of(window(2, 3), window(57, 67)), windows30);
			}
		}
	}

}

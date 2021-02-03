package org.verapdf.wcag.algorithms.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

class DFSTreeNodeIteratorTests {

	@Test
	void testWithEmptyTree() {
		ITree tree = new SemanticTree(null);
		List<Integer> actual = new ArrayList<>();
		for (INode iNode : tree) {
			actual.add(iNode.getPageNumber());
		}
		Assertions.assertEquals(new ArrayList<Integer>(), actual);
	}

	@Test
	void testWithSingleNodeTree() {
		List<Integer> expectedList = new ArrayList<>();

		INode root = new SemanticParagraph(1, new double[4], 1, null, null);
		expectedList.add(1);

		ITree tree = new SemanticTree(root);
		List<Integer> actualList = new ArrayList<>();
		for (INode iNode : tree) {
			actualList.add(iNode.getPageNumber());
		}
		Assertions.assertEquals(expectedList, actualList);
	}

	@Test
	void testWithDegenerateTree() {
		int[] expectedArray = {4, 3, 2, 1, 0};
		List<Integer> expectedList = new ArrayList<>(expectedArray.length);
		for (int number : expectedArray) {
			expectedList.add(number);
		}

		INode root = new SemanticParagraph(0, new double[4], 0, null, null);
		INode node = root;
		for (int i = 1; i < expectedArray.length; ++i) {
			INode child = new SemanticParagraph(i, new double[4], i, null, null);
			node.getChildren().add(child);
			node = child;
		}
		ITree tree = new SemanticTree(root);
		List<Integer> actualList = new ArrayList<>();
		for (INode iNode : tree) {
			actualList.add(iNode.getPageNumber());
		}
		Assertions.assertEquals(expectedList, actualList);
	}

	@Test
	void testWithNormalTree() {
		int[] expectedArray = {4, 1, 5, 8, 12, 13, 9, 6, 2, 14, 10, 11, 7, 3, 0};
		List<Integer> expectedList = new ArrayList<>(expectedArray.length);
		for (int number : expectedArray) {
			expectedList.add(number);
		}

		List<INode> nodeList = new ArrayList<>(expectedArray.length);
		for (int i = 0; i < expectedArray.length; ++i) {
			nodeList.add(new SemanticParagraph(i, new double[4], i, null, null));
		}
		nodeList.get(0).getChildren().addAll(nodeList.subList(1, 4));
		nodeList.get(1).getChildren().add(nodeList.get(4));
		nodeList.get(2).getChildren().addAll(nodeList.subList(5, 7));
		nodeList.get(3).getChildren().add(nodeList.get(7));
		nodeList.get(6).getChildren().addAll(nodeList.subList(8, 10));
		nodeList.get(7).getChildren().addAll(nodeList.subList(10, 12));
		nodeList.get(9).getChildren().addAll(nodeList.subList(12, 14));
		nodeList.get(10).getChildren().add(nodeList.get(14));
		INode root = nodeList.get(0);

		ITree tree = new SemanticTree(root);
		List<Integer> actualList = new ArrayList<>();
		for (INode iNode : tree) {
			actualList.add(iNode.getPageNumber());
		}
		Assertions.assertEquals(expectedList, actualList);
	}

	private List<INode> getDFSResult(INode root) {
		List<INode> result = new ArrayList<>();
		Set<INode> visited = new HashSet<>();
		Stack<INode> stack = new Stack<>();
		stack.push(root);

		while (!stack.empty()) {
			INode node = stack.peek();

			if (visited.contains(node)) {
				// postvisit
				result.add(node);
				stack.pop();
			} else {
				// previsit
				visited.add(node);
				for (INode child : node.getChildren()) {
					stack.push(child);
				}
			}
		}
		return result;
	}
}

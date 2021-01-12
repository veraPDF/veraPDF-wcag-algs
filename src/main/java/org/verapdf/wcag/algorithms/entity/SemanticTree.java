package org.verapdf.wcag.algorithms.entity;

import java.util.*;

public class SemanticTree implements ITree<INode> {

	private final INode root;

	public SemanticTree(INode root) {
		this.root = root;
	}

	public INode getRoot() {
		return root;
	}

	public Iterator<INode> iterator() {
		return new DFSTreeIterator(this.getRoot());
	}

	private static class DFSTreeIterator implements Iterator<INode> {

		private final Deque<Map.Entry<INode, Integer>> parentsStack;

		private INode next;

		public DFSTreeIterator(INode treeRoot) {
			parentsStack = new ArrayDeque<>();
			parentsStack.push(new AbstractMap.SimpleEntry<>(treeRoot, 0));
			next();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public INode next() {
			Map.Entry<INode, Integer> parentsStackTopItem;
			INode returnNode = next;
			next = null;

			while (true) {
				if (parentsStack.isEmpty()) {
					break;
				}

				parentsStackTopItem = parentsStack.peek();
				parentsStack.pop();

				if (parentsStackTopItem == null) {
					continue;
				}

				INode node = parentsStackTopItem.getKey();
				int nextChildIndex = parentsStackTopItem.getValue();

				if (nextChildIndex < node.numChildren()) {
					parentsStack.push(new AbstractMap.SimpleEntry<>(node, nextChildIndex + 1));
					parentsStack.push(new AbstractMap.SimpleEntry<>(node.getChildren().get(nextChildIndex), 0));
				} else {
					next = node;
					break;
				}
			}

			return returnNode;
		}
	}
}

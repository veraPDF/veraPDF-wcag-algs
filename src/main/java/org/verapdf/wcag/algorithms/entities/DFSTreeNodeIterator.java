package org.verapdf.wcag.algorithms.entities;

import java.util.Iterator;

class DFSTreeNodeIterator implements Iterator<INode> {

	private final INode root;
	private int childIndex;
	private DFSTreeNodeIterator innerCurrentIterator;

	public DFSTreeNodeIterator(INode root) {
		this.root = root;
		this.childIndex = -1;
		nextInnerIterator();
	}

	@Override
	public boolean hasNext() {
		return innerCurrentIterator != null;
	}

	@Override
	public INode next() {
		if (innerCurrentIterator == null) {
			return null;
		}
		if (!innerCurrentIterator.hasNext()) {
			nextInnerIterator();
			if (!innerCurrentIterator.hasNext()) {
				innerCurrentIterator = null;
				return root;
			}
		}

		return innerCurrentIterator.next();
	}

	private void nextInnerIterator() {
		if (root == null) {
			return;
		}
		++childIndex;
		if (childIndex < root.getChildren().size()) {
			innerCurrentIterator = new DFSTreeNodeIterator(root.getChildren().get(childIndex));
		} else {
			innerCurrentIterator = new DFSTreeNodeIterator(null);
		}
	}
}

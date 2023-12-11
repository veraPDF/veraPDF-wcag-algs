package org.verapdf.wcag.algorithms.entities;

import java.util.Iterator;

public class SemanticTree implements ITree {

	private final INode root;

	public SemanticTree(INode root) {
		this.root = root;
	}

	@Override
	public INode getRoot() {
		return root;
	}

	@Override
	public Iterator<INode> iterator() {
		return new DFSTreeNodeIterator(this.getRoot());
	}

}

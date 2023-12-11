package org.verapdf.wcag.algorithms.entities;

import java.util.Iterator;

public interface ITree extends Iterable<INode> {

	INode getRoot();

	@Override
	default Iterator<INode> iterator() {
		return new DFSTreeNodeIterator(getRoot());
	}
}

package org.verapdf.wcag.algorithms.implementation;

import org.verapdf.wcag.algorithms.entity.INode;
import org.verapdf.wcag.algorithms.entity.ITree;

public interface ISemanticsChecker {
	void checkSemanticTree(ITree<INode> tree);
}

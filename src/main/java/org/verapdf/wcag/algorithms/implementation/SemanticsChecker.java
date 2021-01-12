package org.verapdf.wcag.algorithms.implementation;

import org.verapdf.wcag.algorithms.entity.INode;
import org.verapdf.wcag.algorithms.entity.ITree;

import java.util.function.Consumer;

public class SemanticsChecker implements ISemanticsChecker {
    public void checkSemanticTree(ITree<INode> tree) {
        Consumer<INode> v = new AccumulatedNodeBuilder();

        tree.forEach(v);
    }
}

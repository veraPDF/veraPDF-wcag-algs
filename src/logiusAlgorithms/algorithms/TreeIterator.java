package logiusAlgorithms.algorithms;

import logiusAlgorithms.tree.Node;
import logiusAlgorithms.tree.Tree;

public abstract class TreeIterator {
    protected Tree tree;

    public TreeIterator(Tree tree) {
        this.tree = tree;
    }

    public abstract Node first();

    public abstract Node current();

    public abstract Node next();
}

package logiusAlgorithms.tree;

import logiusAlgorithms.algorithms.DFSTreeIterator;
import logiusAlgorithms.algorithms.TreeIterator;

public abstract class Tree {
    protected Node root;

    public Tree(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public TreeIterator getDFSIterator() {
        return new DFSTreeIterator(this);
    }
}

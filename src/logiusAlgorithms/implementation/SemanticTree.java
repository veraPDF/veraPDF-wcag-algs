package logiusAlgorithms.implementation;

import logiusAlgorithms.implementation.algorithms.DFSTreeIterator;
import logiusAlgorithms.interfaces.Node;
import logiusAlgorithms.interfaces.Tree;

import java.util.Iterator;

public class SemanticTree implements Tree<Node> {
    protected Node root;

    public SemanticTree(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public Iterator<Node> iterator() {
        return new DFSTreeIterator(this);
    }
}

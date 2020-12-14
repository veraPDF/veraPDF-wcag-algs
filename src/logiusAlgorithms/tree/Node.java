package logiusAlgorithms.tree;

import logiusAlgorithms.algorithms.Visitor;

import java.util.List;

public abstract class Node {
    protected List<Node> children;

    public List<Node> getChildren() {
        return children;
    }

    public int getNumChildren() {
        return children != null ? children.size() : 0;
    }

    public void acceptVisitor(Visitor visitor) {
        visitor.visitNode(this);
    }
}

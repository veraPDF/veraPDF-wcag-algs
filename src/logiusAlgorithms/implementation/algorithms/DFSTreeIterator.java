package logiusAlgorithms.implementation.algorithms;

import javafx.util.Pair;
import java.util.Iterator;
import java.util.Stack;

import logiusAlgorithms.interfaces.Node;
import logiusAlgorithms.interfaces.Tree;

public class DFSTreeIterator implements Iterator<Node> {
    protected Stack<Pair<Node, Integer>> parentsStack;
    Node next;

    public DFSTreeIterator(Tree<Node> tree) {
        parentsStack = new Stack<>();
        parentsStack.push(new Pair<>(tree.getRoot(), 0));
        next();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Node next() {
        Pair<Node, Integer> parentsStackTopItem;
        Node returnNode = next;
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

            Node node = parentsStackTopItem.getKey();
            int nextChildIndex = parentsStackTopItem.getValue();

            if (nextChildIndex < node.numChildren()) {
                parentsStack.push(new Pair<>(node, nextChildIndex + 1));
                parentsStack.push(new Pair<>(node.getChildren().get(nextChildIndex), 0));
            } else {
                next = node;
                break;
            }
        }

        return returnNode;
    }
}

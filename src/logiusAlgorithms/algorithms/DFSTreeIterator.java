package logiusAlgorithms.algorithms;

import javafx.util.Pair;
import logiusAlgorithms.tree.Node;
import logiusAlgorithms.tree.Tree;

import java.util.Stack;

public class DFSTreeIterator extends TreeIterator {
    protected Stack<Pair<Node, Integer>> parentsStack;

    public DFSTreeIterator(Tree tree) {
        super(tree);
        parentsStack = new Stack<>();
        parentsStack.push(new Pair<>(tree.getRoot(), 0));
    }

    @Override public Node first() {
        return next();
    }

    @Override
    public Node current() {
        return parentsStack.isEmpty() ? null : parentsStack.peek().getKey();
    }

    @Override
    public Node next() {
        Pair<Node, Integer> parentsStackTopItem;

        while (true) {
            if (parentsStack.isEmpty()) {
                break;
            }

            parentsStackTopItem = parentsStack.peek();
            parentsStack.pop();

            Node node = parentsStackTopItem.getKey();
            int nextChildIndex = parentsStackTopItem.getValue();

            if (nextChildIndex < node.getNumChildren()) {
                parentsStack.push(new Pair<>(node, nextChildIndex + 1));
                parentsStack.push(new Pair<>(node.getChildren().get(nextChildIndex), 0));
            } else {
                return node;
            }
        }

        return null;
    }
}

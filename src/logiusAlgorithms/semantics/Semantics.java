package logiusAlgorithms.semantics;

import logiusAlgorithms.algorithms.SemanticChecksVisitor;
import logiusAlgorithms.algorithms.TreeIterator;
import logiusAlgorithms.PDTree.PDTree;
import logiusAlgorithms.tree.Node;

public class Semantics {
    public static void checkPDTree(PDTree tree) {
        TreeIterator dfsIterator = tree.getDFSIterator();
        SemanticChecksVisitor visitor = new SemanticChecksVisitor();

        for (Node node = dfsIterator.first(); node != null; node = dfsIterator.next()) {
            node.acceptVisitor(visitor);
        }
    }
}

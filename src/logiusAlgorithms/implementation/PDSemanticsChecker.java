package logiusAlgorithms.implementation;

import logiusAlgorithms.implementation.algorithms.AccumulatedNodeBuilder;
import logiusAlgorithms.interfaces.SemanticsChecker;
import logiusAlgorithms.interfaces.Tree;

import logiusAlgorithms.implementation.algorithms.SemanticChecksConsumer;

import java.util.function.Consumer;

public class PDSemanticsChecker implements SemanticsChecker {
    public void checkSemanticTree(Tree tree) {
        Consumer v = new AccumulatedNodeBuilder();
        tree.forEach(v);
    }
}

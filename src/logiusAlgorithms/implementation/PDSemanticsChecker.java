package logiusAlgorithms.implementation;

import logiusAlgorithms.interfaces.SemanticsChecker;
import logiusAlgorithms.interfaces.Tree;

import logiusAlgorithms.implementation.algorithms.SemanticChecksVisitor;

import java.util.function.Consumer;

public class PDSemanticsChecker implements SemanticsChecker {
    public void checkSemanticTree(Tree tree) {
        Consumer v = new SemanticChecksVisitor();
        tree.forEach(v);
    }
}

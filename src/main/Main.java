package main;

import littleDirtySecret.__JsonToPdfTree;
import logiusAlgorithms.implementation.PDSemanticsChecker;
import logiusAlgorithms.implementation.SemanticTree;
import logiusAlgorithms.interfaces.Node;
import logiusAlgorithms.interfaces.SemanticsChecker;
import logiusAlgorithms.interfaces.Tree;

public class Main {
    public static void main(String[] args) {
        SemanticsChecker semanticsChecker = new PDSemanticsChecker();
        Node root = __JsonToPdfTree.getPdfTreeRoot("3.json");
        Tree<Node> tree = new SemanticTree(root);
        semanticsChecker.checkSemanticTree(tree);
        System.out.println("Correct semantic score is " + tree.getRoot().getCorrectSemanticScore());
    }
}

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
        Node root = __JsonToPdfTree.getPdfTreeRoot("0.json"); // 0, 3 -- tests
        Tree<Node> tree = new SemanticTree(root);
        semanticsChecker.checkSemanticTree(tree);
        System.out.println("Correct semantic score is " + tree.getRoot().getCorrectSemanticScore());
    }
}

// rewrite simple hash function for Node
// replace "SPAN" "PARAGRAPH" with constants
// rename accumulatedNodeBuilder
// rename lineBoundaries
// rename ChunksMergeUtils if static functions only "Utils" ChunksMergeUtils
// rename thresholdProbability, check probabilities
// make log number of products in to***MergeProbability
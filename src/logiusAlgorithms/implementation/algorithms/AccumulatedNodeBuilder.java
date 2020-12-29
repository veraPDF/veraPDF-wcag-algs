package logiusAlgorithms.implementation.algorithms;

import logiusAlgorithms.implementation.ChunksMergeUtils;
import logiusAlgorithms.implementation.SemanticNode;
import logiusAlgorithms.implementation.SemanticTextChunk;
import logiusAlgorithms.interfaces.Chunk;
import logiusAlgorithms.interfaces.Node;
import logiusAlgorithms.interfaces.TextChunk;

import java.util.*;
import java.util.function.Consumer;

public class AccumulatedNodeBuilder implements Consumer<Node> {
    private class Pair<F extends Comparable<F>, S extends Comparable<S>> implements Comparable<Pair<F, S>> {
        public F first;
        public S second;

        Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public int compareTo(Pair<F, S> o) {
            int firstComparison = first.compareTo(o.first);
            if (firstComparison != 0) {
                return firstComparison;
            } else {
                return second.compareTo(o.second);
            }
        }
    }

    protected Map<Node, Node> nodeToAccumulatedNodeMap;
    protected double MERGE_PROBABILITY_THRESHOLD = 0.75;

    public AccumulatedNodeBuilder() {
        nodeToAccumulatedNodeMap = new HashMap<>();
    }

    public Map<Node, Node> getNodeToAccumulatedNodeMap() { return nodeToAccumulatedNodeMap; }

    @Override
    public void accept(Node node) {
        List<Node> children = node.getChildren();
        final int numChildren = node.numChildren();

        if (numChildren < 2) {
            Node accNode = node;
            double correctSemanticScore = 1;

            if (numChildren == 1) {
                Node firstChild = node.getChildren().get(0);
                accNode = nodeToAccumulatedNodeMap.get(firstChild);
                correctSemanticScore = firstChild.getCorrectSemanticScore();
            }

            nodeToAccumulatedNodeMap.put(node, accNode);
            node.setCorrectSemanticScore(correctSemanticScore);
            return;
        }

        // Each entry (first, second) says that chunks in range [first, second) are considered to be a one line
        List<Pair<Integer, Integer>> linesBoundaries = new ArrayList<>();
        List<Double> mergeProbabilities = new ArrayList<>(); // Gives probabilities of merge adjacent nodes
        findLines(node, linesBoundaries, mergeProbabilities);

        if (linesBoundaries.get(0).compareTo(new Pair<>(0, numChildren)) == 0) {
            // All children can be merged in one line
            double lineProbability = 1;
            for (int i = 0; i < numChildren - 1; ++i) {
                lineProbability *= mergeProbabilities.get(i);
            }
            nodeToAccumulatedNodeMap.put(node, buildLineFromChildren(node, new Pair<>(0, numChildren)));
            node.setCorrectSemanticScore(lineProbability);
            return;
        }

//        double paragraphProbability = 1;
//        for (int i = 0; i < linesBoundaries.size(); ++i) {
//            double baseLine = -1;
//            Pair<Integer, Integer> lineBoundary = linesBoundaries.get(i);
////            for (int j = lineBoundary.first; j < lineBoundary.second; ++j) {
////            }
//            baseLine = ((TextChunk) nodeToAccumulatedNodeMap.get(children.get(lineBoundary.first))).getBaseLine();
//            double fontSize = ((TextChunk) nodeToAccumulatedNodeMap.get(children.get(lineBoundary.first))).getFontSize();
//            Node prevChild = new SemanticTextChunk("", new double[4], fontSize, baseLine, 0);
//
//            if (lineBoundary.second > numChildren - 1) {
//                continue;
//            }
//            TextChunk nextChild = (TextChunk) nodeToAccumulatedNodeMap.get(children.get(lineBoundary.second));
//
//            double mergeProbability = ChunksMergeUtils.toParagraphMergeProbability((TextChunk) prevChild, nextChild);
//            mergeProbabilities.set(lineBoundary.second - 1, mergeProbability);
//
//            paragraphProbability *= mergeProbability;
//        }
//        nodeToAccumulatedNodeMap.put(node, buildLineFromChildren(node));
//        node.setCorrectSemanticScore(paragraphProbability);

        double paragraphProbability = 1;
        for (int i = 0; i < linesBoundaries.size() - 1; ++i) {
            Pair<Integer, Integer> lineBoundary = linesBoundaries.get(i);
            int numChunks = lineBoundary.second - lineBoundary.first;

            Node currentText;
            if (numChunks == 1) {
                currentText = children.get(i);
            } else {
                currentText = buildLineFromChildren(node, lineBoundary);
            }

            Chunk nextText = (Chunk) nodeToAccumulatedNodeMap.get(children.get(lineBoundary.second));

            double mergeProbability = 1;

            if (currentText.getSemanticType().equals("SPAN") && nextText.getSemanticType().equals("SPAN")) {
                mergeProbability = ChunksMergeUtils.toLineMergeProbability((TextChunk) currentText, (TextChunk) nextText);
            } else if (true) {

            }

            mergeProbabilities.set(lineBoundary.second - 1, mergeProbability);
            paragraphProbability *= mergeProbability;
        }
        nodeToAccumulatedNodeMap.put(node, buildParagraphFromChildren(node));
        node.setCorrectSemanticScore(paragraphProbability);
    }


    private void findLines(Node node, List<Pair<Integer, Integer>> linesBoundaries, List<Double> mergeProbabilities) {
        List<Node> children = node.getChildren();
        int numChildren = node.numChildren();

        // Use two pointers method to build linesBoundaries
        Integer currentLineLeftBoundary = -1; // left pointer and i of for-loop is the right pointer
        for (int i = 0; i < numChildren - 1; ++i) {
            mergeProbabilities.add(i, 0.);

            TextChunk currentChild = (TextChunk) nodeToAccumulatedNodeMap.get(children.get(i));
            TextChunk nextChild = (TextChunk) nodeToAccumulatedNodeMap.get(children.get(i + 1));

            if (!currentChild.getSemanticType().equals("SPAN")) {
                linesBoundaries.add(new Pair<>(i, i + 1));
                continue;
            }

            if (currentLineLeftBoundary == -1) {
                currentLineLeftBoundary = i;
            }

            if (!nextChild.getSemanticType().equals("SPAN")) {
                linesBoundaries.add(new Pair<>(currentLineLeftBoundary, i + 1));
                currentLineLeftBoundary = -1;
                continue;
            }

            // now: currentChild.semanticType == nextChild.semanticType == "SPAN"

            double mergeProbability = ChunksMergeUtils.toLineMergeProbability(currentChild, nextChild);
            mergeProbabilities.set(i, mergeProbability);
            if (mergeProbability < MERGE_PROBABILITY_THRESHOLD) {
                linesBoundaries.add(new Pair<>(currentLineLeftBoundary, i + 1));
                currentLineLeftBoundary = -1;
            }
        }
        if (currentLineLeftBoundary == -1) {
            currentLineLeftBoundary = numChildren - 1;
        }
        linesBoundaries.add(new Pair<>(currentLineLeftBoundary, numChildren));
    }

    private String leastPossibleChunkType(Node node) {
        String chunkType = "SPAN";
        for (int i = 0; i < node.numChildren(); ++i) {
            Node accNode = nodeToAccumulatedNodeMap.get(node.getChildren().get(i));
            if (!accNode.getSemanticType().equals("SPAN")) {
                chunkType = "PARAGRAPH";
                break;
            }
        }
        return chunkType;
    }

    private Node buildLineFromChildren(Node node, Pair<Integer, Integer> lineBoundary) {
        List<Node> children = node.getChildren();

        double fontSize = 0;
        double baseLine = 0;
        double[] accumulatedBoundingBox = { 1e9, 1e9, -1e9, -1e9 };

        for (int j = lineBoundary.first; j < lineBoundary.second; ++j) {
            TextChunk chunk = (TextChunk) nodeToAccumulatedNodeMap.get(children.get(j));
            if (chunk.getFontSize() > fontSize) {
                fontSize = chunk.getFontSize();
                baseLine = chunk.getBaseLine();
            }

            accumulatedBoundingBox[0] = Math.min(accumulatedBoundingBox[0], chunk.getBoundingBox()[0]);
            accumulatedBoundingBox[1] = Math.min(accumulatedBoundingBox[1], chunk.getBoundingBox()[1]);
            accumulatedBoundingBox[2] = Math.max(accumulatedBoundingBox[2], chunk.getBoundingBox()[2]);
            accumulatedBoundingBox[3] = Math.max(accumulatedBoundingBox[3], chunk.getBoundingBox()[3]);
        }

        return new SemanticTextChunk("", accumulatedBoundingBox, fontSize, baseLine, -1);
    }

    private Node buildParagraphFromChildren(Node node) {
        return new SemanticNode();
    }
}



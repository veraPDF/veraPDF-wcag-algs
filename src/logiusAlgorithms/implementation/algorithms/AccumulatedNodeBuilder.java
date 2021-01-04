package logiusAlgorithms.implementation.algorithms;

import logiusAlgorithms.implementation.ChunksMergeUtils;
import logiusAlgorithms.implementation.SemanticParagraph;
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

    private final static String SEMANTIC_TYPE_SPAN = "SPAN";
    private final static String SEMANTIC_TYPE_PARAGRAPH = "PARAGRAPH";
    private Map<Node, Node> nodeToAccumulatedNodeMap;
    private double MERGE_PROBABILITY_THRESHOLD = 0.75;

    public AccumulatedNodeBuilder() {
        nodeToAccumulatedNodeMap = new HashMap<>();
    }

    public Map<Node, Node> getNodeToAccumulatedNodeMap() { return nodeToAccumulatedNodeMap; }

    @Override
    public void accept(Node node) {
        final int numChildren = node.numChildren();

        if (numChildren <= 1) {
            acceptNodeWithLeq1Child(node);
            return;
        }

        // Pair <first, second> means that [first, second) chunks in range construct the longest possible line
        // or this chunk is a paragraph
        List<Pair<Integer, Integer>> semanticChunksBoundaries = new ArrayList<>();
        List<Double> mergeProbabilities = new ArrayList<>(); // Gives merge score of two adjacent nodes
        findSemanticChunksBoundaries(node, semanticChunksBoundaries, mergeProbabilities);

        if (semanticChunksBoundaries.get(0).compareTo(new Pair<>(0, numChildren)) == 0) {
            // All children can be merged into one line
            double lineProbability = 1;
            for (int i = 0; i < numChildren - 1; ++i) {
                lineProbability *= mergeProbabilities.get(i);
            }
            addAccumulatedNode(node, buildLineFromChildren(node, new Pair<>(0, numChildren)),
                    lineProbability, SEMANTIC_TYPE_SPAN);
            return;
        }

        // Not a line -> build paragraph

        double paragraphProbability = 1;
        SemanticParagraph paragraph = null;
        for (int i = 0; i < semanticChunksBoundaries.size(); ++i) {
            final Node currentText = buildSemanticChunkFromChildren(node, semanticChunksBoundaries.get(i));

            if (paragraph == null) {
                paragraph = buildParagraphFromNode(currentText);
            } else {
                paragraphProbability *= toParagraphMergeProbability(paragraph, currentText);
            }
        }
        addAccumulatedNode(node, buildParagraphFromChildren(node), paragraphProbability, SEMANTIC_TYPE_PARAGRAPH);
    }

    private void acceptNodeWithLeq1Child(Node node) {
        if (node.numChildren() < 1) {
            addAccumulatedNode(node, node, 1, node.getSemanticType());
        } else {
            Node child = node.getChildren().get(0);
            addAccumulatedNode(node, nodeToAccumulatedNodeMap.get(child), child.getCorrectSemanticScore(),
                    child.getSemanticType());
        }
    }

    private void addAccumulatedNode(Node node, Node accumulatedNode, double correctSemanticScore, String semanticType) {
        nodeToAccumulatedNodeMap.put(node, accumulatedNode);
        node.setCorrectSemanticScore(correctSemanticScore);
        node.setSemanticType(semanticType);
    }

    private Node buildSemanticChunkFromChildren(Node node, Pair<Integer, Integer> semanticChunkBoundaries) {
        final int numChunks = semanticChunkBoundaries.second - semanticChunkBoundaries.first;
        if (numChunks == 1) {
            return nodeToAccumulatedNodeMap.get(node.getChildren().get(semanticChunkBoundaries.first));
        } else {
            return buildLineFromChildren(node, semanticChunkBoundaries);
        }
    }

    private void findSemanticChunksBoundaries(Node node, List<Pair<Integer, Integer>> semanticChunksBoundaries, List<Double> mergeProbabilities) {
        final List<Node> children = node.getChildren();
        final int numChildren = node.numChildren();

        // Use two pointers method to build linesBoundaries
        Integer currentLineLeftBoundary = -1; // left pointer
        for (int i = 0; i < numChildren - 1; ++i) { // i is the right pointer
            mergeProbabilities.add(i, 0.);

            Node currentChild = nodeToAccumulatedNodeMap.get(children.get(i));
            Node nextChild = nodeToAccumulatedNodeMap.get(children.get(i + 1));

            if (!currentChild.getSemanticType().equals(SEMANTIC_TYPE_SPAN)) {
                semanticChunksBoundaries.add(new Pair<>(i, i + 1));
                continue;
            }

            if (currentLineLeftBoundary == -1) {
                currentLineLeftBoundary = i;
            }

            if (!nextChild.getSemanticType().equals(SEMANTIC_TYPE_SPAN)) {
                semanticChunksBoundaries.add(new Pair<>(currentLineLeftBoundary, i + 1));
                currentLineLeftBoundary = -1;
                continue;
            }

            // now: currentChild.semanticType == nextChild.semanticType == SEMANTIC_TYPE_SPAN

            final double mergeProbability = ChunksMergeUtils.toLineMergeProbability((TextChunk) currentChild, (TextChunk) nextChild);
            mergeProbabilities.set(i, mergeProbability);
            
            if (mergeProbability < MERGE_PROBABILITY_THRESHOLD) {
                semanticChunksBoundaries.add(new Pair<>(currentLineLeftBoundary, i + 1));
                currentLineLeftBoundary = -1;
            }
        }
        if (currentLineLeftBoundary == -1) {
            currentLineLeftBoundary = numChildren - 1;
        }
        semanticChunksBoundaries.add(new Pair<>(currentLineLeftBoundary, numChildren));
    }

    private SemanticParagraph buildParagraphFromNode(Node node) {
        switch (node.getSemanticType()) {
            case SEMANTIC_TYPE_SPAN:
                TextChunk textChunk = (TextChunk) node;
                return new SemanticParagraph(textChunk, textChunk, textChunk.getBoundingBox(),
                        textChunk.getPageNumber(), textChunk.getPageNumber());
            case SEMANTIC_TYPE_PARAGRAPH:
                return (SemanticParagraph) node;
            default:
                return null;
        }
    }

    private double toParagraphMergeProbability(SemanticParagraph paragraph, Node node) {
        switch (node.getSemanticType()) {
            case SEMANTIC_TYPE_SPAN:
                return toParagraphMergeProbability(paragraph, (TextChunk) node);
            case SEMANTIC_TYPE_PARAGRAPH:
                return toParagraphMergeProbability(paragraph, (SemanticParagraph) node);
            default:
                return 0;
        }
    }

    private double toParagraphMergeProbability(SemanticParagraph paragraph, TextChunk textChunk) {
        if (isOneLineParagraph(paragraph)) {
            paragraph.setBoundingBox(minMaxBoundingBox(paragraph, textChunk));
            paragraph.setLastLine(textChunk);
            paragraph.setLastPageNumber(textChunk.getPageNumber());

            return ChunksMergeUtils.mergeLeadingProbability(paragraph.getFirstLine(), textChunk);
        }

        return ChunksMergeUtils.toParagraphMergeProbability(paragraph.getLastLine(),
                textChunk,
                paragraph.getIndentation());
    }

    private double toParagraphMergeProbability(SemanticParagraph paragraph1, SemanticParagraph paragraph2) {
        if (isOneLineParagraph(paragraph1)) {
            paragraph1.setBoundingBox(minMaxBoundingBox(paragraph1, paragraph2));
            paragraph1.setLastLine(paragraph2.getLastLine());
            paragraph1.setLastPageNumber(paragraph2.getLastPageNumber());

            return ChunksMergeUtils.mergeLeadingProbability(paragraph1.getFirstLine(),
                    paragraph2.getFirstLine());
        }

        return ChunksMergeUtils.toParagraphMergeProbability(paragraph1.getLastLine(),
                paragraph2.getFirstLine(),
                paragraph1.getIndentation());
    }

    private boolean isOneLineParagraph(SemanticParagraph paragraph) {
        return paragraph.getFirstLine() == paragraph.getLastLine();
    }

    private int getIndentation(Chunk x, Chunk y) {
        double[] differences = new double[3];

        differences[0] = Math.abs(x.getLeftX() - y.getLeftX());
        differences[1] = Math.abs(x.getRightX() - y.getRightX());
        differences[2] = (differences[0] + differences[1]) / 2;

        int iMin = 0;
        double minDifference = differences[0];
        for (int i = 1; i < 3; ++i) {
            if (minDifference > differences[i]) {
                iMin = i;
                minDifference = differences[i];
            }
        }

        return iMin;
    }

    private double[] minMaxBoundingBox(Node node, Pair<Integer, Integer> lineBoundary) {
        final List<Node> children = node.getChildren();

        final double max = Double.MAX_VALUE;
        double[] minMaxBoundingBox = { max, max, -max, -max };

        for (int i = lineBoundary.first; i < lineBoundary.second; ++i) {
            Chunk chunk = (Chunk) nodeToAccumulatedNodeMap.get(children.get(i));

            minMaxBoundingBox[0] = Math.min(minMaxBoundingBox[0], chunk.getBoundingBox()[0]);
            minMaxBoundingBox[1] = Math.min(minMaxBoundingBox[1], chunk.getBoundingBox()[1]);
            minMaxBoundingBox[2] = Math.max(minMaxBoundingBox[2], chunk.getBoundingBox()[2]);
            minMaxBoundingBox[3] = Math.max(minMaxBoundingBox[3], chunk.getBoundingBox()[3]);
        }

        return minMaxBoundingBox;
    }

    private double[] minMaxBoundingBox(Chunk chunk1, Chunk chunk2) {
        return new double[] {
                Math.min(chunk1.getLeftX(), chunk2.getLeftX()),
                Math.min(chunk1.getBottomY(), chunk2.getBottomY()),
                Math.max(chunk1.getRightX(), chunk2.getRightX()),
                Math.max(chunk1.getTopY(), chunk2.getTopY())
        };
    }

    private Node buildLineFromChildren(Node node, Pair<Integer, Integer> lineBoundary) {
        final List<Node> children = node.getChildren();

        double fontSize = 0;
        double baseLine = 0;

        for (int i = lineBoundary.first; i < lineBoundary.second; ++i) {
            final TextChunk chunk = (TextChunk) nodeToAccumulatedNodeMap.get(children.get(i));
            if (chunk.getFontSize() > fontSize) {
                fontSize = chunk.getFontSize();
                baseLine = chunk.getBaseLine();
            }
        }

        int pageNumber = ((TextChunk) nodeToAccumulatedNodeMap.get(children.get(lineBoundary.first))).getPageNumber();

        return new SemanticTextChunk("", minMaxBoundingBox(node, lineBoundary),
                fontSize, baseLine, pageNumber);
    }

    private Node buildParagraphFromChildren(Node node) {
        final Node firstChild = nodeToAccumulatedNodeMap.get(node.getChildren().get(0));
        final Node lastChild = nodeToAccumulatedNodeMap.get(node.getChildren().get(node.numChildren() - 1));
        final TextChunk firstLine = firstChild.getSemanticType().equals(SEMANTIC_TYPE_SPAN) ?
                (TextChunk) firstChild : ((SemanticParagraph) firstChild).getFirstLine();
        final TextChunk lastLine = lastChild.getSemanticType().equals(SEMANTIC_TYPE_SPAN) ?
                (TextChunk) lastChild : ((SemanticParagraph) lastChild).getLastLine();

        return new SemanticParagraph(firstLine, lastLine, minMaxBoundingBox(node, new Pair<>(0, node.numChildren())),
                ((Chunk) firstChild).getPageNumber(), ((Chunk) lastChild).getPageNumber());
    }
}



package logiusAlgorithms.implementation.algorithms;

import logiusAlgorithms.implementation.TextSemanticsChecker;
import logiusAlgorithms.interfaces.Chunk;
import logiusAlgorithms.interfaces.Node;
import logiusAlgorithms.interfaces.TextChunk;
import logiusAlgorithms.interfaces.Tree;

import java.util.Map;
import java.util.function.Consumer;

public class SemanticChecksConsumer implements Consumer<Node> {
    protected TextSemanticsChecker textSemanticsChecker;
    protected Tree tree;
    protected Map<Node, Node> nodeToAccumulatedNodeMap;

    public SemanticChecksConsumer(Tree tree) {
        this.tree = tree;
        textSemanticsChecker = new TextSemanticsChecker();

        AccumulatedNodeBuilder accumulatedNodeBuilder = new AccumulatedNodeBuilder();
        tree.forEach(accumulatedNodeBuilder);
        nodeToAccumulatedNodeMap = accumulatedNodeBuilder.getNodeToAccumulatedNodeMap();
    }

    @Override
    public void accept(Node node) {
        if (node.isLeaf())
            return;

//        ChunkType accumulatedNodeChunkType = getMaxChildChunkType(node);
//        double accumulatedNodeProbability = 1;
//
//        for (int i = 1; i < node.getNumChildren(); ++i) {
//            Chunk previousChildAccumulatedChunk = node.getChildren().get(i - 1).getAccumulatedChunk();
//            Chunk currentChildAccumulatedChunk = node.getChildren().get(i).getAccumulatedChunk();
//
//            if (previousChildAccumulatedChunk == null || currentChildAccumulatedChunk == null)
//                continue; // correct with lastChild: [ a, null, b ] -> probability = 1
//
//            accumulatedNodeProbability *= textSemanticsChecker.mergeProbability(
//                    (TextChunk) previousChildAccumulatedChunk,
//                    (TextChunk) currentChildAccumulatedChunk,
//                    accumulatedNodeChunkType);
//        }

//        if (accumulatedNodeProbability > 0.75) {
//            double[] accumulatedBoundingBox = { 1e9, 1e9, -1e9, -1e9 };
//            for (Node child : node.getChildren()) {
//                Chunk childAccumulatedChunk = child.getAccumulatedChunk();
//                if (childAccumulatedChunk == null)
//                    continue;
//
//                accumulatedBoundingBox[0] = Math.min(accumulatedBoundingBox[0], childAccumulatedChunk.getBoundingBox()[0]);
//                accumulatedBoundingBox[1] = Math.min(accumulatedBoundingBox[1], childAccumulatedChunk.getBoundingBox()[1]);
//                accumulatedBoundingBox[2] = Math.max(accumulatedBoundingBox[2], childAccumulatedChunk.getBoundingBox()[2]);
//                accumulatedBoundingBox[3] = Math.max(accumulatedBoundingBox[3], childAccumulatedChunk.getBoundingBox()[3]);
//            }
//
//            node.setAccumulatedChunk(
//                    new Chunk("",
//                            pChild0.getFontName(),
//                            pChild0.getFontSize(),
//                            pChild0.getFontWeight(),
//                            pChild0.getItalicAngle(),
//                            pChild0.getFontColor(),
//                            accumulatedBoundingBox,
//                            pChild0.getBaseLine(),
//                            pChild0.getPageNumber()));
//        } else {
//            semanticNode.accumulatedNode = new SemanticTextChunk();
//        }
    }
}
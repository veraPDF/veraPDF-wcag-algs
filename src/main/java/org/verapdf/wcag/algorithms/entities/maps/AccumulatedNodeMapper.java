package org.verapdf.wcag.algorithms.entities.maps;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticParagraph;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccumulatedNodeMapper {
    private final Map<INode, INode> nodeToAccumulatedNodeMap;

    public AccumulatedNodeMapper() {
        nodeToAccumulatedNodeMap = new HashMap<>();
    }

    public INode get(INode node) {
        if (node == null) {
            return null;
        }
        if (!nodeToAccumulatedNodeMap.containsKey(node)) {
            nodeToAccumulatedNodeMap.put(node, calculateAccumulatedNode(node));
        }
        return nodeToAccumulatedNodeMap.get(node);
    }

    private INode calculateAccumulatedNode(INode node) {
        List<INode> children = node.getChildren();
        if (children.isEmpty()) {
            return node;
        } else if (children.size() == 1) {
            return get(children.get(0));
        } else {
            switch (node.getSemanticType()) {
                case SPAN:
                    return buildSpanFromChildren(node);
                case PARAGRAPH:
                    return buildParagraphFromChildren(node);
                default:
                    break;
            }
        }
        // Accumulation principle is not defined
        return node;
    }

    private INode buildSpanFromChildren(INode node) {
        List<INode> children = node.getChildren();
        double fontSize = 0;
        double baseLine = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (INode child : children) {
            SemanticSpan span = (SemanticSpan) get(child);
            TextChunk chunk = span.getTextChunks().get(0);
            stringBuilder.append(chunk.getValue());
            if (chunk.getFontSize() > fontSize) {
                fontSize = chunk.getFontSize();
                baseLine = chunk.getBaseLine();
            }
        }

        TextChunk textChunk = new TextChunk(childrenBoundingBox(node),
                                            stringBuilder.toString(),
                                            fontSize, baseLine);
        INode semanticSpan = new SemanticSpan(textChunk);
        semanticSpan.setCorrectSemanticScore(node.getCorrectSemanticScore());
        return semanticSpan;
    }

    private INode buildParagraphFromChildren(INode node) {
        List<INode> children = node.getChildren();
        INode firstChild = get(children.get(0));
        INode lastChild = get(children.get(children.size() - 1));

        TextChunk firstLine = SemanticType.SPAN.equals(firstChild.getSemanticType())
                ? ((SemanticSpan) firstChild).getTextChunks().get(0)
                : ((SemanticParagraph) firstChild).getFirstLine();
        TextChunk lastLine = SemanticType.SPAN.equals(lastChild.getSemanticType())
                ? ((SemanticSpan) lastChild).getTextChunks().get(0)
                : ((SemanticParagraph) lastChild).getLastLine();

        INode semanticParagraph = new SemanticParagraph(childrenBoundingBox(node), firstLine, lastLine);
        semanticParagraph.setCorrectSemanticScore(node.getCorrectSemanticScore());
        return semanticParagraph;
    }

    public BoundingBox childrenBoundingBox(INode node) {
        List<INode> children = node.getChildren();
        BoundingBox bbox = new BoundingBox();

        for (INode child : children) {
            INode accumulatedChild = get(child);
            bbox.union(accumulatedChild.getBoundingBox());
        }
        return bbox;
    }
}

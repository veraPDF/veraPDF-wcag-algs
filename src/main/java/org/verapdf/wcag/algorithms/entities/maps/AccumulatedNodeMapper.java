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

        SemanticSpan semanticSpan = new SemanticSpan();
        for (INode child : children) {
            SemanticSpan childSpan = (SemanticSpan) get(child);
            semanticSpan.addAll(childSpan.getTextChunks());
        }
        return semanticSpan;
    }

    private INode buildParagraphFromChildren(INode node) {
        List<INode> children = node.getChildren();
        INode firstChild = get(children.get(0));
        INode lastChild = get(children.get(children.size() - 1));

        TextChunk firstLine = SemanticType.SPAN.equals(firstChild.getSemanticType())
                              ? ((SemanticSpan) firstChild).getTextChunks().get(0)
                              : SemanticType.PARAGRAPH.equals(firstChild.getSemanticType()) ? ((SemanticParagraph) firstChild).getFirstLine() : new TextChunk();
        TextChunk lastLine = SemanticType.SPAN.equals(lastChild.getSemanticType())
                             ? ((SemanticSpan) lastChild).getTextChunks().get(0)
                             : SemanticType.PARAGRAPH.equals(lastChild.getSemanticType()) ? ((SemanticParagraph) lastChild).getLastLine() : new TextChunk();

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

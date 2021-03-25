package org.verapdf.wcag.algorithms.entities.maps;

import org.verapdf.wcag.algorithms.entities.INode;

import java.util.HashMap;
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
            return node;
        }
        return nodeToAccumulatedNodeMap.get(node);
    }

    public void put(INode node, INode accumulatedNode) {
        if (node == null) {
            return;
        }
        nodeToAccumulatedNodeMap.put(node, accumulatedNode);
    }
}

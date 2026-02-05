/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.wcag.algorithms.entities.maps;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

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

    public void updateNode(INode node, INode accumulatedNode, double correctSemanticScore, SemanticType semanticType) {
        if (accumulatedNode == null) {
            node.setCorrectSemanticScore(0.0d);
            return;
        }
        node.setCorrectSemanticScore(correctSemanticScore);
        node.setSemanticType(semanticType);
        accumulatedNode.setCorrectSemanticScore(correctSemanticScore);
        put(node, accumulatedNode);
    }
}

package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

/**
 * Allowed semantic types: PART, SECT, DIV, ASIDE, NON_STRUCT
 */
public class SemanticGroupingNode extends SemanticNode {

    public SemanticGroupingNode() {
    }

    public SemanticGroupingNode(SemanticType semanticType) {
        setSemanticType(semanticType);
    }
}

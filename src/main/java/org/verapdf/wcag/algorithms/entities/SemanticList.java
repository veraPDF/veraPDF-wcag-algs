package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

public class SemanticList extends SemanticNode {

	public SemanticList(INode node) {
		super(node.getBoundingBox());
		setSemanticType(SemanticType.LIST);
	}

}

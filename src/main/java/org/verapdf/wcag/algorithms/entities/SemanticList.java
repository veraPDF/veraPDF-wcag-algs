package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

public class SemanticList extends SemanticTextNode {

	public SemanticList(SemanticTextNode node) {
		super(node);
		setSemanticType(SemanticType.LIST);
	}

}

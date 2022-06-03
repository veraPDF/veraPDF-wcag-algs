package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.List;

public class SemanticPart extends SemanticTextNode {

	public SemanticPart(BoundingBox bbox, List<TextColumn> columns) {
		super(bbox, columns);
		setSemanticType(SemanticType.PART);
	}

	public SemanticPart() {
		setSemanticType(SemanticType.PART);
	}
}

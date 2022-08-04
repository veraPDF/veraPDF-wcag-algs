package org.verapdf.wcag.algorithms.entities.lists.info;

import org.verapdf.wcag.algorithms.entities.content.LineArtChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

public class ListItemLineArtInfo extends ListItemInfo {
	private LineArtChunk lineArt;

	public ListItemLineArtInfo(int index, SemanticType semanticType, LineArtChunk lineArt) {
		super(index, semanticType);
		this.lineArt = lineArt;
	}

	@Override
	public LineArtChunk getListItemValue() {
		return lineArt;
	}
}

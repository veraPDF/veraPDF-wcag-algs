package org.verapdf.wcag.algorithms.entities.lists.info;

import org.verapdf.wcag.algorithms.entities.content.LineArtChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

public class ListItemLineArtInfo extends ListItemInfo {
	private final LineArtChunk lineArt;

	public ListItemLineArtInfo(ListItemLineArtInfo info) {
		super(info);
		this.lineArt = info.lineArt;
	}

	public ListItemLineArtInfo(int index, SemanticType semanticType, LineArtChunk lineArt) {
		super(index, semanticType);
		this.lineArt = lineArt;
	}

	@Override
	public LineArtChunk getListItemValue() {
		return lineArt;
	}
}

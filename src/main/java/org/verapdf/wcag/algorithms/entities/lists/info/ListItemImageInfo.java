package org.verapdf.wcag.algorithms.entities.lists.info;

import org.verapdf.wcag.algorithms.entities.content.ImageChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

public class ListItemImageInfo extends ListItemInfo {
	private final ImageChunk image;

	public ListItemImageInfo(ListItemImageInfo info) {
		super(info);
		this.image = info.image;
	}

	public ListItemImageInfo(int index, SemanticType semanticType, ImageChunk image) {
		super(index, semanticType);
		this.image = image;
	}

	@Override
	public ImageChunk getListItemValue() {
		return image;
	}
}

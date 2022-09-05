package org.verapdf.wcag.algorithms.entities.lists.info;

import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

public class ListItemTextInfo extends ListItemInfo {
	private final TextLine firstLine;
	private final String listItem;

	public ListItemTextInfo(ListItemTextInfo info) {
		super(info);
		this.firstLine = info.firstLine;
		this.listItem = info.listItem;
	}

	public ListItemTextInfo(int index, SemanticType semanticType, TextLine firstLine, String listItem) {
		super(index, semanticType);
		this.firstLine = firstLine;
		this.listItem = listItem;
	}

	@Override
	public TextLine getListItemValue() {
		return firstLine;
	}

	public String getListItem() {
		return listItem;
	}
}

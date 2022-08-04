package org.verapdf.wcag.algorithms.entities.lists.info;

import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

public class ListItemTextInfo extends ListItemInfo {
	private TextLine firstLine;
	private String listItem;

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

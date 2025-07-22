package org.verapdf.wcag.algorithms.entities.lists.info;

import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

public class ListItemTextInfo extends ListItemInfo {
	private final TextLine firstLine;
	private final String listItem;
	private final boolean hasOneLine;
	private String prefix;
	private String numberedPart;
	private Integer number;
	private String suffix;
	
	public ListItemTextInfo(ListItemTextInfo info) {
		super(info);
		this.firstLine = info.firstLine;
		this.listItem = info.listItem;
		this.hasOneLine = info.hasOneLine;
	}

	public ListItemTextInfo(int index, SemanticType semanticType, TextLine firstLine, String listItem, boolean hasOneLine) {
		super(index, semanticType);
		this.firstLine = firstLine;
		this.listItem = listItem;
		this.hasOneLine = hasOneLine;
	}

	@Override
	public TextLine getListItemValue() {
		return firstLine;
	}

	public String getListItem() {
		return listItem;
	}

	public boolean hasOneLine() {
		return hasOneLine;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getNumberedPart() {
		return numberedPart;
	}

	public void setNumberedPart(String numberedPart) {
		this.numberedPart = numberedPart;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	public String getBody() {
		return getListItem().substring(getPrefix().length() + getNumberedPart().length() + getSuffix().length());
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}
}


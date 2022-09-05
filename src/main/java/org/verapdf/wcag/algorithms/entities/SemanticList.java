package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;

public class SemanticList extends SemanticTextNode {

	private final ListInterval listInterval;

	public SemanticList(SemanticTextNode node, ListInterval listInterval) {
		super(node);
		setSemanticType(SemanticType.LIST);
		this.listInterval = listInterval;
	}

	public int getNumberOfListColumns() {
		return listInterval.getNumberOfColumns();
	}

	public int getNumberOfListItems() {
		return listInterval.getNumberOfListItems();
	}

	public int getNumberOfLists() {
		return listInterval.getNumberOfLists();
	}

	public ListInterval getListInterval() {
		return listInterval;
	}

	public int getNumberOfListItemsAndLists() {
		return listInterval.getNumberOfListItems() + listInterval.getNumberOfLists();
	}
}

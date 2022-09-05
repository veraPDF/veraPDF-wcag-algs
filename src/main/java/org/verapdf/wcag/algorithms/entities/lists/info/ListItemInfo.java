package org.verapdf.wcag.algorithms.entities.lists.info;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.Objects;

public class ListItemInfo {
	private int index;
	private SemanticType semanticType;

	public ListItemInfo(ListItemInfo info) {
		this(info.getIndex(), info.getSemanticType());
	}

	public ListItemInfo(int index) {
		this.index = index;
	}

	protected ListItemInfo(int index, SemanticType semanticType) {
		this.index = index;
		this.semanticType = semanticType;
	}

	public int getIndex() {
		return index;
	}

	public static ListItemInfo createListItemInfo(ListItemInfo info) {
		if (info instanceof ListItemTextInfo) {
			return new ListItemTextInfo((ListItemTextInfo) info);
		} else if (info instanceof ListItemLineArtInfo) {
			return new ListItemLineArtInfo((ListItemLineArtInfo) info);
		} else if (info instanceof ListItemImageInfo) {
			return new ListItemImageInfo((ListItemImageInfo) info);
		}
		return new ListItemInfo(info);
	}

	public SemanticType getSemanticType() {
		return semanticType;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public InfoChunk getListItemValue() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ListItemInfo that = (ListItemInfo) o;
		return index == that.index && semanticType == that.semanticType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(index, semanticType);
	}

	@Override
	public String toString() {
		return "ListItemInfo{" +
		       "index=" + index +
		       ", semanticType=" + semanticType +
		       '}';
	}
}

package org.verapdf.wcag.algorithms.entities.lists.info;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.Objects;

public abstract class ListItemInfo {
	private int index;
	private SemanticType semanticType;

	protected ListItemInfo() {

	}

	protected ListItemInfo(int index, SemanticType semanticType) {
		this.index = index;
		this.semanticType = semanticType;
	}

	public int getIndex() {
		return index;
	}

	public SemanticType getSemanticType() {
		return semanticType;
	}

	public abstract InfoChunk getListItemValue();

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

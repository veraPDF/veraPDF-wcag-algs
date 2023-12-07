package org.verapdf.wcag.algorithms.entities;

import java.util.Objects;

public class JsonAttributes {

	private long colSpan;
	private long rowSpan;

	public long getColSpan() {
		return colSpan;
	}

	public void setColSpan(long colSpan) {
		this.colSpan = colSpan;
	}

	public long getRowSpan() {
		return rowSpan;
	}

	public void setRowSpan(long rowSpan) {
		this.rowSpan = rowSpan;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JsonAttributes jsonAttributes = (JsonAttributes) o;
		return jsonAttributes.colSpan == colSpan &&
				jsonAttributes.rowSpan == rowSpan;
	}

	@Override
	public int hashCode() {
		return Objects.hash(colSpan, rowSpan);
	}

	@Override
	public String toString() {
		return "JsonNode{" +
				", colSpan=" + colSpan +
				", rowSpan=" + rowSpan +
				'}';
	}
}

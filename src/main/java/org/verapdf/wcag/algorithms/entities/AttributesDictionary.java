package org.verapdf.wcag.algorithms.entities;

public class AttributesDictionary implements IAttributesDictionary {

	private long rowSpan;
	private long colSpan;

	public AttributesDictionary(long rowSpan, long colSpan) {
		this.rowSpan = rowSpan;
		this.colSpan = colSpan;
	}

	public void setRowSpan(long rowSpan) {
		this.rowSpan = rowSpan;
	}

	public void setColSpan(long colSpan) {
		this.colSpan = colSpan;
	}

	public long getRowSpan() {
		return rowSpan;
	}

	public long getColSpan() {
		return colSpan;
	}
}

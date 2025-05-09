package org.verapdf.wcag.algorithms.entities;

public class AttributesDictionary implements IAttributesDictionary {

	private long rowSpan;
	private long colSpan;

	public AttributesDictionary() {
		this.rowSpan = 1;
		this.colSpan = 1;
	}

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

	@Override
	public long getRowSpan() {
		return rowSpan;
	}

	@Override
	public long getColSpan() {
		return colSpan;
	}
}

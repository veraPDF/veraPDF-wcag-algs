package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;

public class SemanticTable extends SemanticNode {

	private final TableBorder tableBorder;

	public SemanticTable(TableBorder tableBorder) {
		super(tableBorder.getBoundingBox());
		this.tableBorder = tableBorder;
		setSemanticType(SemanticType.TABLE);
	}

	public TableBorder getTableBorder() {
		return tableBorder;
	}
}

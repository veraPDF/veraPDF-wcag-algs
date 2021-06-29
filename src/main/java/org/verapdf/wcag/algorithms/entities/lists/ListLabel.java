package org.verapdf.wcag.algorithms.entities.lists;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.tables.TableCell;

public class ListLabel extends ListElement {

    public ListLabel(TableCell cell) {
        super(cell, SemanticType.LIST_LABEL);
    }

}

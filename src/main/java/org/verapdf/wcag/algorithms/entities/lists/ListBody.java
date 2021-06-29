package org.verapdf.wcag.algorithms.entities.lists;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.tables.TableCell;

public class ListBody extends ListElement {

    public ListBody(TableCell cell) {
        super(cell, SemanticType.LIST_BODY);
    }

}

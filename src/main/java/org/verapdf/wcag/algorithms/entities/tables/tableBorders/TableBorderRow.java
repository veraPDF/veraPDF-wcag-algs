/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.wcag.algorithms.entities.tables.tableBorders;

import org.verapdf.wcag.algorithms.entities.BaseObject;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public class TableBorderRow extends BaseObject {
    private final int rowNumber;
    protected final TableBorderCell[] cells;
    private SemanticType semanticType;
    private INode node;

    public TableBorderRow(int rowNumber, int numberOfColumns, Long id) {
        super(new BoundingBox());
        this.rowNumber = rowNumber;
        cells = new TableBorderCell[numberOfColumns];
        setRecognizedStructureId(id);
    }

    public SemanticType getSemanticType() {
        return semanticType;
    }

    public void setSemanticType(SemanticType semanticType) {
        this.semanticType = semanticType;
    }

    public INode getNode() {
        return node;
    }

    public void setNode(INode node) {
        this.node = node;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public int getNumberOfCellWithContent() {
        int numberOfCellsWithContent = 0;
        for (int colNumber = 0; colNumber < cells.length; colNumber++) {
            TableBorderCell cell = cells[colNumber];
            if (cell.colNumber == colNumber && cell.rowNumber == rowNumber && !cell.getContent().isEmpty()) {
                numberOfCellsWithContent++;
            }
        }
        return numberOfCellsWithContent;
    }

    public int getNumberOfCells() {
        int numberOfCells = 0;
        for (int colNumber = 0; colNumber < cells.length; colNumber++) {
            TableBorderCell cell = cells[colNumber];
            if (cell.colNumber == colNumber && cell.rowNumber == rowNumber) {
                numberOfCells++;
            }
        }
        return numberOfCells;
    }

    public TableBorderCell[] getCells() {
        return cells;
    }

    public TableBorderCell getCell(int colNumber) {
        return cells[colNumber];
    }
}

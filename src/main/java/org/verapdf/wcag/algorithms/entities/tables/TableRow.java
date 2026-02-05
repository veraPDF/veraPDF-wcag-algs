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
package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.content.TextInfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;

import java.util.ArrayList;
import java.util.List;

public class TableRow extends TextInfoChunk {
    private Long id;
    private final List<TableCell> cells;
    private SemanticType semanticType = null;

    public TableRow() {
        super(new MultiBoundingBox());
        cells = new ArrayList<>();
    }

    public TableRow(SemanticType semanticType, Long id) {
        this();
        this.semanticType = semanticType;
        this.id = id;
    }

    public void setSemanticType(SemanticType semanticType) {
        this.semanticType = semanticType;
    }

    public SemanticType getSemanticType() {
        return semanticType;
    }

    public void add(TableCell cell) {
        cells.add(cell);
        super.add(cell);
    }

    public List<TableCell> getCells() {
        return cells;
    }

    public int getNumberOfCellsWithContent() {
        int numberOfCellsWithContent = 0;
        for (TableCell cell : cells) {
            if (!cell.getContent().isEmpty()) {
                numberOfCellsWithContent++;
            }
        }
        return numberOfCellsWithContent;
    }

    public void merge(TableRow other) {
        List<TableCell> otherCells = other.getCells();
        for (int i = 0; i < otherCells.size(); ++i) {
            if (i < cells.size()) {
                cells.get(i).merge(otherCells.get(i));
            } else {
                cells.add(otherCells.get(i));
            }
        }
        super.add(other);
    }

    public Long getId() {
        return id;
    }
}

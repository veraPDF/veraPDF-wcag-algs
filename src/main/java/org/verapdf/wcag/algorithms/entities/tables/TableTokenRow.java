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

import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableClusterGap;

import java.util.Objects;

public class TableTokenRow extends TextLine {
    private Integer rowNumber = null;
    private TableClusterGap leftGap = null;
    private TableClusterGap rightGap = null;
    private TableToken.TableTokenType type;

    public TableTokenRow() {
    }

    public TableTokenRow(TableToken token) {
        super(token);
        type = token.getType();
    }

    public TableTokenRow(TableTokenRow row) {
        super(row);
        rowNumber = row.getRowNumber();
        leftGap = row.getLeftGap();
        rightGap = row.getRightGap();
        type = row.getType();
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setLeftGap(TableClusterGap gap) {
        leftGap = gap;
    }

    public TableClusterGap getLeftGap() {
        return leftGap;
    }

    public void setRightGap(TableClusterGap gap) {
        rightGap = gap;
    }

    public TableClusterGap getRightGap() {
        return rightGap;
    }

    public String getString() {
        return super.toString();
    }

    public TableToken.TableTokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(Objects.toString(rowNumber));
        result.append(" : ");
        result.append(super.toString());
        return result.toString();
    }
}

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
package org.verapdf.wcag.algorithms.entities.lists;

import org.verapdf.wcag.algorithms.entities.content.TextInfoChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.tables.TableCell;
import org.verapdf.wcag.algorithms.entities.tables.TableTokenRow;

import java.util.ArrayList;
import java.util.List;

public class ListElement extends TextInfoChunk {

    private final List<TableTokenRow> content;
    private SemanticType semanticType;

    public ListElement(TableCell cell, SemanticType semanticType) {
        super(cell.getBoundingBox(), cell.getFontSize(), cell.getBaseLine());
        this.semanticType = semanticType;
        content = new ArrayList<>();
        content.addAll(cell.getContent());
    }

    public ListElement(TextLine textLine, SemanticType semanticType) {
        super(textLine.getBoundingBox(), textLine.getFontSize(), textLine.getBaseLine());
        this.semanticType = semanticType;
        content = new ArrayList<>();
    }

    public void setSemanticType(SemanticType semanticType) {
        this.semanticType = semanticType;
    }

    public SemanticType getSemanticType() {
        return semanticType;
    }

    public void add(TableTokenRow row) {
        content.add(row);
        super.add(row);
    }

    public void add(TableCell cell) {
        for (TableTokenRow tableTokenRow : cell.getContent()) {
            add(tableTokenRow);
        }
    }

    public List<TableTokenRow> getContent() {
        return content;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public String getString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (TableTokenRow tokenRow : content) {
            stringBuilder.append(tokenRow.getString());
        }
        return stringBuilder.toString();
    }

}

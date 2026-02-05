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
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableCluster;

import java.util.ArrayList;
import java.util.List;

public class TableCell extends TextInfoChunk {
    private final List<TableTokenRow> content;
    private SemanticType semanticType = null;

    public TableCell() {
        super(new MultiBoundingBox());
        content = new ArrayList<>();
    }

    public TableCell(SemanticType semanticType) {
        this();
        this.semanticType = semanticType;
    }

    public TableCell(TableTokenRow row, SemanticType semanticType) {
        super(row.getBoundingBox(), row.getFontSize(), row.getBaseLine());
        content = new ArrayList<>();
        content.add(row);
        this.semanticType = semanticType;
    }

    public TableCell(TableCluster cluster, SemanticType semanticType) {
        super(cluster.getBoundingBox(), cluster.getFontSize(), cluster.getBaseLine());
        content = new ArrayList<>();
        content.addAll(cluster.getRows());
        this.semanticType = semanticType;
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

    public List<TableTokenRow> getContent() {
        return content;
    }

    public TableTokenRow getFirstTokenRow() {
        if (content.isEmpty()) {
            return null;
        }
        return content.get(0);
    }

    public TableTokenRow getLastTokenRow() {
        if (content.isEmpty()) {
            return null;
        }
        return content.get(content.size() - 1);
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public void merge(TableCell other) {
        if (other.isEmpty()) {
            return;
        }
        content.addAll(other.getContent());
        super.add(other);
    }

    public boolean isTextCell() {
        for (TableTokenRow tableTokenRow : content) {
            if (tableTokenRow.getType() != TableToken.TableTokenType.TEXT) {
                return false;
            }
        }
        return true;
    }

    public String getString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (TableTokenRow tokenRow : content) {
            stringBuilder.append(tokenRow.getString());
        }
        return stringBuilder.toString();
    }
}

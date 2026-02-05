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

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.content.ImageChunk;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;

public class TableToken extends TextChunk {
    private final INode node;
    private final TableTokenType type;

    public TableToken(TextChunk textChunk, INode node) {
        super(textChunk);
        this.node = node;
        type = TableTokenType.TEXT;
    }

    public TableToken(TableBorder tableBorder) {
        super(tableBorder.getBoundingBox(), "", tableBorder.getBoundingBox().getHeight(),
                tableBorder.getBoundingBox().getBottomY());
        this.node = tableBorder.getNode();
        type = TableTokenType.TABLE;
    }

    public TableToken(ImageChunk imageChunk, INode node) {
        super(imageChunk.getBoundingBox(), "", imageChunk.getBoundingBox().getHeight(),
                imageChunk.getBoundingBox().getBottomY());
        this.node = node;
        type = TableTokenType.IMAGE;
    }

    public INode getNode() {
        return node;
    }

    public TableTokenType getType() {
        return type;
    }

    public enum TableTokenType {
        IMAGE,
        TEXT,
        TABLE
    }
}

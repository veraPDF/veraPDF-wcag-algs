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
import org.verapdf.wcag.algorithms.entities.IObject;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;
import org.verapdf.wcag.algorithms.entities.tables.TableToken;

import java.util.LinkedList;
import java.util.List;

public class TableBorderCell extends BaseObject {
    protected int rowNumber;
    protected int colNumber;
    protected int rowSpan;
    protected int colSpan;
    private final List<TableToken> content;
    private List<IObject> contents;
    private SemanticType semanticType;
    private INode node;

    public TableBorderCell(int rowNumber, int colNumber, int rowSpan, int colSpan, Long id) {
        super(new BoundingBox());
        this.rowNumber = rowNumber;
        this.colNumber = colNumber;
        this.rowSpan = rowSpan;
        this.colSpan = colSpan;
        content = new LinkedList<>();
        contents = new LinkedList<>();
        setRecognizedStructureId(id);
    }

    public TableBorderCell(INode node, int rowNumber, int colNumber) {
        super(node.getBoundingBox());
        this.node = node;
        this.rowSpan = (int) node.getAttributesDictionary().getRowSpan();
        this.colSpan = (int) node.getAttributesDictionary().getColSpan();
        this.rowNumber = rowNumber;
        this.colNumber = colNumber;
        content = new LinkedList<>();
        contents = new LinkedList<>();
    }

    public void addContent(TableToken token) {
        content.add(token);
    }

    public List<TableToken> getContent() {
        return content;
    }

    public void addContentObject(IObject object) {
        contents.add(object);
    }

    public List<IObject> getContents() {
        return contents;
    }

    public void setContents(List<IObject> contents) {
        this.contents = contents;
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

    public int getColNumber() {
        return colNumber;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public int getColSpan() {
        return colSpan;
    }

    public BoundingBox getContentBoundingBox() {
        BoundingBox boundingBox = new MultiBoundingBox();
        for (TableToken token : content) {
            boundingBox.union(token.getBoundingBox());
        }
        return boundingBox;
    }
}

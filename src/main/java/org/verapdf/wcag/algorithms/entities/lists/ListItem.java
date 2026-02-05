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

import org.verapdf.wcag.algorithms.entities.IObject;
import org.verapdf.wcag.algorithms.entities.content.TextBlock;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.tables.TableRow;

import java.util.ArrayList;
import java.util.List;

public class ListItem extends TextBlock {
    private final ListLabel label;
    private final ListBody body;
    private SemanticType semanticType;
    private List<IObject> contents = new ArrayList<>();

    public ListItem(TableRow tableRow) {
        super(tableRow.getBoundingBox(), tableRow.getFontSize(), tableRow.getBaseLine());
        semanticType = SemanticType.LIST_ITEM;
        label = new ListLabel(tableRow.getCells().get(0));
        body = new ListBody(tableRow.getCells().get(1));
        setRecognizedStructureId(tableRow.getId());
    }

    public ListItem(BoundingBox boundingBox, Long id) {
        super(boundingBox);
        label = null;
        body = null;
        setRecognizedStructureId(id);
    }

    public void add(TableRow tableRow) {
        label.add(tableRow.getCells().get(0));
        body.add(tableRow.getCells().get(1));
        super.add(tableRow);
    }

    public void setSemanticType(SemanticType semanticType) {
        this.semanticType = semanticType;
    }

    public SemanticType getSemanticType() {
        return semanticType;
    }

    public ListLabel getLabel() {
        return label;
    }

    public ListBody getBody() {
        return body;
    }

    public List<IObject> getContents() {
        return contents;
    }

    public void setContents(List<IObject> contents) {
        this.contents = contents;
    }
}

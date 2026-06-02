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
package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;

import java.util.ArrayList;
import java.util.List;

public class SemanticTOC extends InfoChunk {
    
    private final List<IObject> tocItems;
    
    private SemanticTOC previousTOC = null;
    private SemanticTOC nextTOC = null;

    public SemanticTOC(Long id) {
        super();
        setRecognizedStructureId(id);
        tocItems = new ArrayList<>();
    }

    public SemanticTOC() {
        super();
        tocItems = new ArrayList<>();
    }

    public List<IObject> getTOCItems() {
        return tocItems;
    }

    public void add(SemanticTOCI tocItem) {
        tocItems.add(tocItem);
        getBoundingBox().union(tocItem.getBoundingBox());
    }

    public void add(int index, SemanticTOCI tocItem) {
        tocItems.add(index, tocItem);
        getBoundingBox().union(tocItem.getBoundingBox());
    }

    public void add(SemanticTOC toc) {
        tocItems.add(toc);
        getBoundingBox().union(toc.getBoundingBox());
    }

    public Long getPreviousTOCId() {
        return previousTOC != null ? previousTOC.getRecognizedStructureId() : null;
    }

    public Long getNextTOCId() {
        return nextTOC != null ? nextTOC.getRecognizedStructureId() : null;
    }

    public SemanticTOC getPreviousTOC() {
        return previousTOC;
    }

    public void setPreviousTOC(SemanticTOC previousTOC) {
        this.previousTOC = previousTOC;
    }

    public SemanticTOC getNextTOC() {
        return nextTOC;
    }

    public void setNextTOC(SemanticTOC nextTOC) {
        this.nextTOC = nextTOC;
    }

    public static void setTOCConnected(SemanticTOC firstTOC, SemanticTOC secondTOC) {
        firstTOC.setNextTOC(secondTOC);
        secondTOC.setPreviousTOC(firstTOC);
    }
}

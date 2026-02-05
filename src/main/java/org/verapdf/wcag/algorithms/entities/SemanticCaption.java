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

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public class SemanticCaption extends SemanticTextNode {
    
    private Long linkedContentId = null; 

    public SemanticCaption(SemanticCaption caption) {
        super(caption);
    }

    public SemanticCaption(SemanticTextNode textNode) {
        super(textNode);
        setSemanticType(SemanticType.CAPTION);
    }

    public SemanticCaption() {
        setSemanticType(SemanticType.CAPTION);
    }

    public SemanticCaption(SemanticType initialSemanticType) {
        super(initialSemanticType);
        setSemanticType(SemanticType.CAPTION);
    }

    public SemanticCaption(BoundingBox bbox) {
        super(bbox);
    }

    public SemanticCaption(BoundingBox bbox, SemanticType initialSemanticType) {
        super(bbox, initialSemanticType);
        setSemanticType(SemanticType.CAPTION);
    }

    public SemanticCaption(TextChunk textChunk) {
        setSemanticType(SemanticType.CAPTION);
        add(new TextLine(textChunk));
    }

    public SemanticCaption(TextChunk textChunk, SemanticType initialSemanticType) {
        this(initialSemanticType);
        add(new TextLine(textChunk));
    }

    public Long getLinkedContentId() {
        return linkedContentId;
    }

    public void setLinkedContentId(Long linkedContentId) {
        this.linkedContentId = linkedContentId;
    }
}

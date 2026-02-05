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

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.LinkedList;
import java.util.List;

public class SemanticAnnot extends SemanticNode {

	protected final List<AnnotationNode> annotationNodes = new LinkedList<>();
    
    public SemanticAnnot(SemanticAnnot annotationNode) {
        this.addAnnots(annotationNode.getAnnots());
        setSemanticType(SemanticType.ANNOT);
    }

    public SemanticAnnot(AnnotationNode annotationNode) {
        super(annotationNode.getBoundingBox());
        this.annotationNodes.add(annotationNode);
        setSemanticType(SemanticType.ANNOT);
    }

    public void addAnnots(List<AnnotationNode> annots) {
        this.annotationNodes.addAll(annots);
        for (AnnotationNode annotationNode : annots) {
            getBoundingBox().union(annotationNode.getBoundingBox());
        }
    }

	public List<AnnotationNode> getAnnots() {
		return annotationNodes;
	}
    
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (!(o instanceof SemanticAnnot)) {
            return false;
        }
        SemanticAnnot that = (SemanticAnnot) o;
        return this.annotationNodes.equals(that.getAnnots());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + annotationNodes.size();
        for (AnnotationNode annotationNode : annotationNodes) {
            result = 31 * result + annotationNode.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("SemanticAnnot{");
        result.append("pageNumber=");
        result.append(getBoundingBox().getPageNumber());
        result.append(", boundingBox=");
        result.append(getBoundingBox());
        result.append("}");
        return result.toString();
    }
}

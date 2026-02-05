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

import org.verapdf.wcag.algorithms.entities.content.ImageChunk;
import org.verapdf.wcag.algorithms.entities.content.LineArtChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.LinkedList;
import java.util.List;

public class SemanticFigure extends SemanticNode {

	protected final List<ImageChunk> images = new LinkedList<>();
    protected final List<LineArtChunk> lineArts = new LinkedList<>();

    public SemanticFigure(SemanticFigure imageNode) {
        this.addImages(imageNode.getImages());
        this.addLineArts(imageNode.getLineArts());
        setSemanticType(SemanticType.FIGURE);
    }

    public SemanticFigure(ImageChunk image) {
        super(image.getBoundingBox());
        this.images.add(image);
        setSemanticType(SemanticType.FIGURE);
    }

    public SemanticFigure(LineArtChunk lineArtChunk) {
        super(lineArtChunk.getBoundingBox());
        this.lineArts.add(lineArtChunk);
        setSemanticType(SemanticType.FIGURE);
    }

    public void addImages(List<ImageChunk> images) {
        this.images.addAll(images);
        for (ImageChunk imageChunk : images) {
            getBoundingBox().union(imageChunk.getBoundingBox());
        }
    }

    public void addLineArts(List<LineArtChunk> lineArts) {
        this.lineArts.addAll(lineArts);
        for (LineArtChunk lineArtChunk : lineArts) {
            getBoundingBox().union(lineArtChunk.getBoundingBox());
        }
    }

	public List<ImageChunk> getImages() {
		return images;
	}

	public List<LineArtChunk> getLineArts() {
        return lineArts;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (!(o instanceof SemanticFigure)) {
            return false;
        }
        SemanticFigure that = (SemanticFigure) o;
        return this.images.equals(that.getImages()) && this.lineArts.equals(that.getLineArts());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + images.size();
        for (ImageChunk imageChunk : images) {
            result = 31 * result + imageChunk.hashCode();
        }
        result = 31 * result + lineArts.size();
        for (LineArtChunk lineArtChunk : lineArts) {
            result = 31 * result + lineArtChunk.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("SemanticFigure{");
        result.append("pageNumber=");
        result.append(getBoundingBox().getPageNumber());
        result.append(", boundingBox=");
        result.append(getBoundingBox());
        result.append("}");
        return result.toString();
    }
}

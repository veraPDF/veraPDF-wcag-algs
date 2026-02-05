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
package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.LinkedList;
import java.util.List;

public class LineArtChunk extends InfoChunk {
	
	private static double LINE_ART_SIZE_EPSILON = 0.002;

	private List<LineChunk> lineChunks;

	public LineArtChunk() {
	}

	public LineArtChunk(BoundingBox bbox) {
		this(bbox, new LinkedList<>());
	}

	public LineArtChunk(BoundingBox bbox, List<LineChunk> lineChunks) {
		super(bbox);
		this.lineChunks = lineChunks;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("LineArtChunk{");
		result.append("pageNumber=");
		result.append(getBoundingBox().getPageNumber());
		result.append(", boundingBox=");
		result.append(getBoundingBox());
		result.append("}");
		return result.toString();
	}

	public List<LineChunk> getLineChunks() {
		return lineChunks;
	}

	public void setLineChunks(List<LineChunk> lineChunks) {
		this.lineChunks = lineChunks;
	}
	
	public static boolean areHaveSameSizes(LineArtChunk lineArt1, LineArtChunk lineArt2) {
		return NodeUtils.areCloseNumbers(lineArt1.getHeight(), lineArt2.getHeight(), LINE_ART_SIZE_EPSILON) &&
				NodeUtils.areCloseNumbers(lineArt1.getWidth(), lineArt2.getWidth(), LINE_ART_SIZE_EPSILON);
	}
}

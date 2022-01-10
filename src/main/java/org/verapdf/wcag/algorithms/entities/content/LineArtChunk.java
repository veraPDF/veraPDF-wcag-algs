package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public class LineArtChunk extends InfoChunk {

	public LineArtChunk(BoundingBox bbox) {
		super(bbox);
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
}

package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.LinkedList;
import java.util.List;

public class LineArtChunk extends InfoChunk {

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
}

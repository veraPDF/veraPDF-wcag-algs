package org.verapdf.wcag.algorithms.entities;

import java.util.List;

public class JsonLineArtChunk extends JsonNode {

	private List<JsonLineChunk> lines;

	public List<JsonLineChunk> getLines() {
		return lines;
	}

	public void setLines(List<JsonLineChunk> lines) {
		this.lines = lines;
	}
}

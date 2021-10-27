package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.IChunk;

import java.util.LinkedList;
import java.util.List;

public class Page implements IPage {

	private final int pageNumber;
	private final List<IChunk> artifacts;

	public Page(int pageNumber) {
		this.pageNumber = pageNumber;
		artifacts = new LinkedList<>();
	}

	public List<IChunk> getArtifacts() {
		return artifacts;
	}

	public int getPageNumber() {
		return pageNumber;
	}

}

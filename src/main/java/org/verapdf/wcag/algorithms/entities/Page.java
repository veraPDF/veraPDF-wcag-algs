package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.IChunk;

import java.util.LinkedList;
import java.util.List;

public class Page implements IPage {

	private final int pageNumber;
	private final String pageLabel;
	private final List<IChunk> artifacts;

	public Page(int pageNumber, String pageLabel) {
		this.pageNumber = pageNumber;
		this.pageLabel = pageLabel;
		artifacts = new LinkedList<>();
	}

	@Override
	public List<IChunk> getArtifacts() {
		return artifacts;
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

	@Override
	public String getPageLabel() {
		return pageLabel;
	}

}

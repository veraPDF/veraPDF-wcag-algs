package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.IChunk;

import java.util.List;

public interface IPage {

	public List<IChunk> getArtifacts();

	public int getPageNumber();

}

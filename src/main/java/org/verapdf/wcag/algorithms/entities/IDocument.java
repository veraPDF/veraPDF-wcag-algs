package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.IChunk;

import java.util.List;

public interface IDocument {

	public ITree getTree();

	public List<IChunk> getArtifacts(Integer pageNumber);

	public List<IPage> getPages();

	public int getNumberOfPages();

	public IPage getPage(Integer pageNumber);

	public List<IChunk> getArtifacts();

}

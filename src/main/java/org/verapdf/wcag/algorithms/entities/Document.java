package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.IChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Document implements IDocument {

	private final ITree tree;
	private final List<IPage> pages;

	public Document(ITree tree) {
		this.tree = tree;
		pages = new LinkedList<>();
	}

	@Override
	public ITree getTree(){
		return tree;
	}

	@Override
	public List<IChunk> getArtifacts(Integer pageNumber) {
		if (pageNumber < pages.size()) {
			return pages.get(pageNumber).getArtifacts();
		}
		return Collections.emptyList();
	}

	@Override
	public List<IPage> getPages() {
		return pages;
	}

	@Override
	public int getNumberOfPages() {
		return pages.size();
	}

	@Override
	public IPage getPage(Integer pageNumber) {
		if (pageNumber < pages.size()) {
			return pages.get(pageNumber);
		}
		return null;
	}

	@Override
	public List<IChunk> getArtifacts() {
		List<IChunk> artifacts = new ArrayList<>();
		for (IPage page : pages) {
			artifacts.addAll(page.getArtifacts());
		}
		return artifacts;
	}

}

package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.IDocument;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.SortedSet;

public class LinesCollection {
	private final Map<Integer, SortedSet<LineChunk>> horizontalLines;
	private final Map<Integer, SortedSet<LineChunk>> verticalLines;

	private final IDocument document;

	public LinesCollection(IDocument document) {
		horizontalLines = new HashMap<>();
		verticalLines = new HashMap<>();
		this.document = document;
	}

	public SortedSet<LineChunk> getHorizontalLines(Integer pageNumber) {
		SortedSet<LineChunk> lines = horizontalLines.get(pageNumber);
		if (lines == null) {
			lines = parseHorizontalLines(pageNumber);
		}
		return lines;
	}

	private SortedSet<LineChunk> parseHorizontalLines(Integer pageNumber) {
		SortedSet<LineChunk> lines = new TreeSet<>(new LineChunk.HorizontalLineComparator());
		for (IChunk chunk : document.getArtifacts(pageNumber)) {
			if (chunk instanceof LineChunk) {
				LineChunk lineChunk = (LineChunk) chunk;
				if (lineChunk.isHorizontalLine()) {
					lines.add(lineChunk);
				}
			}
		}
		horizontalLines.put(pageNumber, lines);
		return lines;
	}

	public SortedSet<LineChunk> getVerticalLines(Integer pageNumber) {
		SortedSet<LineChunk> lines = verticalLines.get(pageNumber);
		if (lines == null) {
			lines = parseVerticalLines(pageNumber);
		}
		return lines;
	}

	private SortedSet<LineChunk> parseVerticalLines(Integer pageNumber) {
		SortedSet<LineChunk> lines = new TreeSet<>(new LineChunk.VerticalLineComparator());
		for (IChunk chunk : document.getArtifacts(pageNumber)) {
			if (chunk instanceof LineChunk) {
				LineChunk lineChunk = (LineChunk) chunk;
				if (lineChunk.isVerticalLine()) {
					lines.add(lineChunk);
				}
			}
		}
		verticalLines.put(pageNumber, lines);
		return lines;
	}
}

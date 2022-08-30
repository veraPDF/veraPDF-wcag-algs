package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.SortedSet;

public class LinesCollection {
	private final Map<Integer, SortedSet<LineChunk>> horizontalLines;
	private final Map<Integer, SortedSet<LineChunk>> verticalLines;
	private final Map<Integer, SortedSet<LineChunk>> squares;

	public LinesCollection() {
		horizontalLines = new HashMap<>();
		verticalLines = new HashMap<>();
		squares = new HashMap<>();
	}

	public SortedSet<LineChunk> getHorizontalLines(Integer pageNumber) {
		SortedSet<LineChunk> horizontalLines = this.horizontalLines.get(pageNumber);
		if (horizontalLines == null) {
			parseLines(pageNumber);
			horizontalLines = this.horizontalLines.get(pageNumber);
		}
		return horizontalLines;
	}

	private void parseLines(Integer pageNumber) {
		SortedSet<LineChunk> horizontalLines = new TreeSet<>(new LineChunk.HorizontalLineComparator());
		SortedSet<LineChunk> verticalLines = new TreeSet<>(new LineChunk.VerticalLineComparator());
		SortedSet<LineChunk> squares = new TreeSet<>(new LineChunk.VerticalLineComparator());
		for (IChunk chunk : StaticContainers.getDocument().getArtifacts(pageNumber)) {
			if (chunk instanceof LineChunk) {
				LineChunk lineChunk = (LineChunk) chunk;
				if (lineChunk.isHorizontalLine()) {
					horizontalLines.add(lineChunk);
				} else if (lineChunk.isVerticalLine()) {
					verticalLines.add(lineChunk);
				} else if (lineChunk.isSquare()) {
					squares.add(lineChunk);
				}
			}
		}
		this.horizontalLines.put(pageNumber, horizontalLines);
		this.verticalLines.put(pageNumber, verticalLines);
		this.squares.put(pageNumber, squares);
	}

	public SortedSet<LineChunk> getVerticalLines(Integer pageNumber) {
		SortedSet<LineChunk> verticalLines = this.verticalLines.get(pageNumber);
		if (verticalLines == null) {
			parseLines(pageNumber);
			verticalLines = this.verticalLines.get(pageNumber);
		}
		return verticalLines;
	}

	public SortedSet<LineChunk> getSquares(Integer pageNumber) {
		SortedSet<LineChunk> squares = this.squares.get(pageNumber);
		if (squares == null) {
			parseLines(pageNumber);
			squares = this.squares.get(pageNumber);
		}
		return squares;
	}
}

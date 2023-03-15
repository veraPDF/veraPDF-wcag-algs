package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticFigure;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.util.*;

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
			parseLines();
			horizontalLines = this.horizontalLines.get(pageNumber);
			if (horizontalLines == null) {
				this.horizontalLines.put(pageNumber, new TreeSet<>(new LineChunk.HorizontalLineComparator()));
				horizontalLines = this.horizontalLines.get(pageNumber);
			}
		}
		return horizontalLines;
	}

	private void parseLines() {
		for (int pageNumber = 0; pageNumber < StaticContainers.getDocument().getNumberOfPages(); pageNumber++) {
			parseLines(pageNumber);
		}
		parseLines(StaticContainers.getDocument().getTree().getRoot());
	}

	private void parseLines(INode node) {
		for (INode child : node.getChildren()) {
			if (child.getInitialSemanticType() != SemanticType.FIGURE) {
				parseLines(child);
			}
			if (child instanceof SemanticFigure) {
				LineArtChunk lineArt = ((SemanticFigure)child).getLineArt();
				if (lineArt == null) {
					continue;
				}
				for (LineChunk lineChunk : lineArt.getLineChunks()) {
					if (lineChunk.isHorizontalLine()) {
						this.horizontalLines.get(lineChunk.getPageNumber()).add(lineChunk);
					} else if (lineChunk.isVerticalLine()) {
						this.verticalLines.get(lineChunk.getPageNumber()).add(lineChunk);
					} else if (lineChunk.isSquare()) {
						this.squares.get(lineChunk.getPageNumber()).add(lineChunk);
					}
				}
			}
		}
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
			parseLines();
			verticalLines = this.verticalLines.get(pageNumber);
			if (verticalLines == null) {
				this.verticalLines.put(pageNumber, new TreeSet<>(new LineChunk.VerticalLineComparator()));
				verticalLines = this.verticalLines.get(pageNumber);
			}
		}
		return verticalLines;
	}

	public SortedSet<LineChunk> getSquares(Integer pageNumber) {
		SortedSet<LineChunk> squares = this.squares.get(pageNumber);
		if (squares == null) {
			parseLines();
			squares = this.squares.get(pageNumber);
			if (squares == null) {
				this.squares.put(pageNumber, new TreeSet<>(new LineChunk.VerticalLineComparator()));
				squares = this.squares.get(pageNumber);
			}
		}
		return squares;
	}
}

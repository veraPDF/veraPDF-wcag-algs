package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.Vertex;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.Comparator;
import java.util.Objects;

public class LineChunk extends InfoChunk {

	private final Vertex start;
	private final Vertex end;
	private final double width;

	public LineChunk(Integer pageNumber, double startX, double startY, double endX, double endY) {
		this(pageNumber, startX, startY, endX, endY, 1.0);
	}

	public LineChunk(Integer pageNumber, double startX, double startY, double endX, double endY, double width) {
		super(new BoundingBox(pageNumber, Math.min(startX, endX) - 0.5 * width,
				Math.min(startY, endY) - 0.5 * width, Math.max(startX, endX) + 0.5 * width,
				Math.max(startY, endY) + 0.5 * width));
		this.start = new Vertex(pageNumber, startX, startY, 0.5 * width);
		this.end = new Vertex(pageNumber, endX, endY, 0.5 * width);
		this.width = width;
	}

	public double getStartX() {
		return start.getX();
	}

	public double getStartY() {
		return start.getY();
	}

	public double getEndX() {
		return end.getX();
	}

	public double getEndY() {
		return end.getY();
	}

	public boolean isHorizontalLine() {
		return NodeUtils.areCloseNumbers(start.getY(), end.getY());
	}

	public boolean isVerticalLine() {
		return NodeUtils.areCloseNumbers(start.getX(), end.getX());
	}

	public double getWidth() {
		return width;
	}

	public static class HorizontalLineComparator implements Comparator<LineChunk> {

		public int compare(LineChunk line1, LineChunk line2){
			int res = Double.compare(line2.getCenterY(), line1.getCenterY());
			if (res != 0) {
				return res;
			}
			return Double.compare(line1.getCenterX(), line2.getCenterX());
		}
	}

	public static class VerticalLineComparator implements Comparator<LineChunk> {

		public int compare(LineChunk line1, LineChunk line2){
			int res = Double.compare(line1.getCenterX(), line2.getCenterX());
			if (res != 0) {
				return res;
			}
			return Double.compare(line2.getCenterY(), line1.getCenterY());
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(start.getX(), start.getY(), end.getX(), end.getY(), width);
	}

	@Override
	public boolean equals(Object o) {
		if (!super.equals(o)) {
			return false;
		}
		LineChunk that = (LineChunk) o;
		return Double.compare(that.start.getX(), start.getX()) == 0
				&& Double.compare(that.start.getY(), start.getY()) == 0
				&& Double.compare(that.end.getX(), end.getX()) == 0
				&& Double.compare(that.end.getY(), end.getY()) == 0
				&& Double.compare(that.width, width) == 0;
	}

	@Override
	public String toString() {
		return "LineChunk{" +
				"startX=" + start.getX() +
				", startY=" + start.getY() +
				", endX=" + end.getX() +
				", endY=" + end.getY() +
				", width=" + width +
				'}';
	}

	public static Vertex getIntersectionVertex(LineChunk horizontalLine, LineChunk verticalLine) {
		if (verticalLine.getCenterX() < horizontalLine.getBoundingBox().getLeftX() ||
				verticalLine.getCenterX() > horizontalLine.getBoundingBox().getRightX()) {
			return null;//epsilon
		}
		if (horizontalLine.getCenterY() < verticalLine.getBoundingBox().getBottomY() ||
				horizontalLine.getCenterY() > verticalLine.getBoundingBox().getTopY()) {
			return null;
		}
		return new Vertex(verticalLine.getBoundingBox().getPageNumber(),
				verticalLine.getCenterX(), horizontalLine.getCenterY(),
				Math.max(0.5 * verticalLine.width, 0.5 * horizontalLine.width));//min?
	}

}

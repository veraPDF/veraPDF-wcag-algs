package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.Vertex;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.Comparator;
import java.util.Objects;

public class LineChunk extends InfoChunk {

	public static int BUTT_CAP_STYLE = 0;
	public static int ROUND_CAP_STYLE = 1;
	public static int PROJECTING_SQUARE_CAP_STYLE = 2;

	private final Vertex start;
	private final Vertex end;
	private final double width;
	private final boolean isHorizontalLine;
	private final boolean isVerticalLine;
	private final boolean isSquare;

	public LineChunk(Integer pageNumber, double startX, double startY, double endX, double endY) {
		this(pageNumber, startX, startY, endX, endY, 1.0);
	}

	public LineChunk(Integer pageNumber, double startX, double startY, double endX, double endY, double width) {
		super(new BoundingBox(pageNumber, Math.min(startX, endX) - 0.5 * width,
				Math.min(startY, endY) - 0.5 * width, Math.max(startX, endX) + 0.5 * width,
				Math.max(startY, endY) + 0.5 * width));//fix
		this.start = new Vertex(pageNumber, startX, startY, 0.5 * width);
		this.end = new Vertex(pageNumber, endX, endY, 0.5 * width);
		this.width = width;//one vertex case
		boolean hasCloseX = NodeUtils.areCloseNumbers(startX, endX, Math.abs(startY - endY) / 100);
		boolean hasCloseY = NodeUtils.areCloseNumbers(startY, endY, Math.abs(startX - endX) / 100);
		isSquare = hasCloseX && hasCloseY;
		isVerticalLine = hasCloseX && !hasCloseY;
		isHorizontalLine = !hasCloseX && hasCloseY;
	}

	public double getStartX() {
		return start.getX();
	}

	public double getStartY() {
		return start.getY();
	}

	public Vertex getStart() {
		return start;
	}

	public double getEndX() {
		return end.getX();
	}

	public double getEndY() {
		return end.getY();
	}

	public Vertex getEnd() {
		return end;
	}

	public boolean isHorizontalLine() {
		return isHorizontalLine;
	}

	public boolean isVerticalLine() {
		return isVerticalLine;
	}

	public boolean isSquare() {
		return isSquare;
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
		if (haveIntersection(horizontalLine, verticalLine)) {
			return new Vertex(verticalLine.getBoundingBox().getPageNumber(),
					verticalLine.getCenterX(), horizontalLine.getCenterY(),
					Math.max(0.5 * verticalLine.width, 0.5 * horizontalLine.width));
		}
		return null;
	}

	public static boolean haveIntersection(LineChunk horizontalLine, LineChunk verticalLine) {
		if (verticalLine.getCenterX() < horizontalLine.getBoundingBox().getLeftX() ||
				verticalLine.getCenterX() > horizontalLine.getBoundingBox().getRightX()) {
			return false;
		}
		if (horizontalLine.getCenterY() < verticalLine.getBoundingBox().getBottomY() ||
				horizontalLine.getCenterY() > verticalLine.getBoundingBox().getTopY()) {
			return false;
		}
		return true;
	}

	public static LineChunk createLineChunk(Integer pageNumber, double startX, double startY, double endX, double endY,
											double width, int cap) {
		if (cap == ROUND_CAP_STYLE || cap == PROJECTING_SQUARE_CAP_STYLE) {
			return new LineChunk(pageNumber, startX, startY, endX, endY, width);
		}
		double length = Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
		if (width > length) {
			double centerX = 0.5 * (startX + endX);
			double centerY = 0.5 * (startY + endY);
			double deltaX = (centerY - startY) * width / length;
			double deltaY = (centerX - startX) * width / length;
			return createLineChunk(pageNumber, centerX + deltaX, centerY - deltaY,
					centerX - deltaX, centerY + deltaY, length, BUTT_CAP_STYLE);
		}
		double deltaX = (endX - startX) * 0.5 * width / length;
		double deltaY = (endY - startY) * 0.5 * width / length;
		return createLineChunk(pageNumber, startX + deltaX, startY + deltaY,
				endX - deltaX, endY - deltaY, width, PROJECTING_SQUARE_CAP_STYLE);
	}
}

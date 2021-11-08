package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.Objects;

public class LineChunk extends InfoChunk {

	private double startX;
	private double startY;
	private double endX;
	private double endY;
	private double width;

	public LineChunk() {
	}

	public LineChunk(Integer pageNumber, double startX, double startY, double endX, double endY) {
		super(new BoundingBox(pageNumber, Math.min(startX, endX), Math.min(startY, endY),
				Math.max(startX, endX), Math.max(startY, endY)));
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}

	public LineChunk(Integer pageNumber, double startX, double startY, double endX, double endY, double width) {
		super(new BoundingBox(pageNumber, Math.min(startX, endX), Math.min(startY, endY),
				Math.max(startX, endX), Math.max(startY, endY)));
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.width = width;
	}

	public double getStartX() {
		return startX;
	}

	public void setStartX(double startX) {
		this.startX = startX;
	}

	public double getStartY() {
		return startY;
	}

	public void setStartY(double startY) {
		this.startY = startY;
	}

	public double getEndX() {
		return endX;
	}

	public void setEndX(double endX) {
		this.endX = endX;
	}

	public double getEndY() {
		return endY;
	}

	public double getCenterX() {
		return 0.5 * (startX + endX);
	}

	public double getCenterY() {
		return 0.5 * (startY + endY);
	}

	public boolean isHorizontalLine() {
		return NodeUtils.areCloseNumbers(startY, endY);
	}

	public boolean isVerticalLine() {
		return NodeUtils.areCloseNumbers(startX, endX);
	}

	public void setEndY(double endY) {
		this.endY = endY;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	@Override
	public int hashCode() {
		return Objects.hash(startX, startY, endX, endY, width);
	}

	@Override
	public boolean equals(Object o) {
		if (!super.equals(o)) {
			return false;
		}
		LineChunk that = (LineChunk) o;
		return Double.compare(that.startX, startX) == 0
				&& Double.compare(that.startY, startY) == 0
				&& Double.compare(that.endX, endX) == 0
				&& Double.compare(that.endY, endY) == 0
				&& Double.compare(that.width, width) == 0;
	}

	@Override
	public String toString() {
		return "LineChunk{" +
				"startX=" + startX +
				", startY=" + startY +
				", endX=" + endX +
				", endY=" + endY +
				", width=" + width +
				'}';
	}
}

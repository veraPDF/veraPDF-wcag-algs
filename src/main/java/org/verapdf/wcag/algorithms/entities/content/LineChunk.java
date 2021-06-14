package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public class LineChunk extends InfoChunk {

	private double startX;
	private double startY;
	private double endX;
	private double endY;

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

	public void setEndY(double endY) {
		this.endY = endY;
	}
}

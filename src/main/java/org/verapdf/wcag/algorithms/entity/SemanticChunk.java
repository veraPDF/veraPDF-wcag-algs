package org.verapdf.wcag.algorithms.entity;

import org.verapdf.wcag.algorithms.entity.enums.SemanticType;

import java.util.Arrays;
import java.util.Objects;

public class SemanticChunk extends SemanticNode implements IChunk {

	protected int pageNumber;
	protected double[] boundingBox;

	public SemanticChunk(SemanticType semanticType, double[] boundingBox, int pageNumber) {
		super(semanticType);
		this.pageNumber = pageNumber;
		this.boundingBox = boundingBox;
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

	@Override
	public double[] getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(double[] boundingBox) {
		this.boundingBox = boundingBox;
	}

	@Override
	public double getLeftX() {
		return boundingBox[0];
	}

	@Override
	public double getBottomY() {
		return boundingBox[1];
	}

	@Override
	public double getRightX() {
		return boundingBox[2];
	}

	@Override
	public double getTopY() {
		return boundingBox[3];
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		SemanticChunk that = (SemanticChunk) o;
		return pageNumber == that.pageNumber && Arrays.equals(boundingBox, that.boundingBox);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(super.hashCode(), pageNumber);
		result = 31 * result + Arrays.hashCode(boundingBox);
		return result;
	}

	@Override
	public String toString() {
		return "SemanticChunk{" +
		       "pageNumber=" + pageNumber +
		       ", boundingBox=" + Arrays.toString(boundingBox) +
		       '}';
	}
}

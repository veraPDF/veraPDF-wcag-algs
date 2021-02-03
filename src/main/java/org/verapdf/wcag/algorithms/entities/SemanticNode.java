package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class SemanticNode implements INode {

	private Double correctSemanticScore;
	private Integer pageNumber;
	private Integer lastPageNumber;
	private double[] boundingBox;
	private SemanticType semanticType;
	private final List<INode> children;

	public SemanticNode() {
		this.children = new ArrayList<>();
	}

	public SemanticNode(Integer pageNumber, double[] boundingBox, SemanticType semanticType) {
		this.pageNumber = pageNumber;
		this.boundingBox = boundingBox;
		this.semanticType = semanticType;

		this.children = new ArrayList<>();
	}

	public SemanticNode(Integer pageNumber, Integer lastPageNumber, double[] boundingBox, SemanticType semanticType) {
		this(pageNumber, boundingBox, semanticType);
		this.lastPageNumber = lastPageNumber;
	}

	@Override
	public Double getCorrectSemanticScore() {
		return correctSemanticScore;
	}

	@Override
	public void setCorrectSemanticScore(Double correctSemanticScore) {
		this.correctSemanticScore = correctSemanticScore;
	}

	@Override
	public Integer getPageNumber() {
		return pageNumber;
	}

	@Override
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	@Override
	public Integer getLastPageNumber() {
		return lastPageNumber != null ? lastPageNumber : pageNumber;
	}

	@Override
	public void setLastPageNumber(Integer lastPageNumber) {
		this.lastPageNumber = lastPageNumber;
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
	public double[] getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(double[] boundingBox) {
		this.boundingBox = boundingBox;
	}

	@Override
	public SemanticType getSemanticType() {
		return semanticType;
	}

	@Override
	public void setSemanticType(SemanticType semanticType) {
		this.semanticType = semanticType;
	}

	@Override
	public List<INode> getChildren() {
		return children;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SemanticNode that = (SemanticNode) o;
		return Objects.equals(pageNumber, that.pageNumber)
		       && Objects.equals(lastPageNumber, that.lastPageNumber)
		       && Arrays.equals(boundingBox, that.boundingBox)
		       && Objects.equals(children, that.children);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(pageNumber, lastPageNumber, children);
		result = 31 * result + Arrays.hashCode(boundingBox);
		return result;
	}

	@Override
	public String toString() {
		return "SemanticNode{" +
		       "correctSemanticScore=" + correctSemanticScore +
		       ", pageNumber=" + pageNumber +
		       ", boundingBox=" + Arrays.toString(boundingBox) +
		       ", semanticType=" + semanticType +
		       ", children=" + children +
		       '}';
	}
}

package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class SemanticNode implements INode {

	private Double correctSemanticScore;
	private BoundingBox boundingBox;
	private SemanticType semanticType;
	private final List<INode> children;

	public SemanticNode() {
		boundingBox = new BoundingBox();
		this.children = new ArrayList<>();
	}

	public SemanticNode(BoundingBox bbox, SemanticType semanticType) {
		this.boundingBox = new BoundingBox(bbox);
		this.semanticType = semanticType;
		this.children = new ArrayList<>();
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
		return boundingBox.getPageNumber();
	}

	@Override
	public void setPageNumber(Integer pageNumber) {
		boundingBox.setPageNumber(pageNumber);
	}

	@Override
	public Integer getLastPageNumber() {
		return boundingBox.getLastPageNumber();
	}

	@Override
	public void setLastPageNumber(Integer lastPageNumber) {
		boundingBox.setLastPageNumber(lastPageNumber);
	}

	@Override
	public double getLeftX() {
		return boundingBox.getLeftX();
	}

	@Override
	public double getBottomY() {
		return boundingBox.getBottomY();
	}

	@Override
	public double getRightX() {
		return boundingBox.getRightX();
	}

	@Override
	public double getTopY() {
		return boundingBox.getTopY();
	}

	@Override
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public void setBoundingBox(BoundingBox bbox) {
		boundingBox.init(bbox);
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
	public double getContrastRatio() {
		return this.boundingBox.getContrastRatio();
	}

	@Override
	public void setContrastRatio(double contrastRatio) {
		this.boundingBox.setContrastRatio(contrastRatio);
	}

	@Override
	public List<INode> getChildren() {
		return children;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(boundingBox);
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
		return that.boundingBox.equals(boundingBox);
	}

	@Override
	public String toString() {
		return "SemanticNode{" +
		       "correctSemanticScore=" + correctSemanticScore +
		       ", pageNumber=" + boundingBox.getPageNumber() +
		       ", boundingBox=" + boundingBox +
		       ", semanticType=" + semanticType +
		       ", children=" + children +
		       '}';
	}
}

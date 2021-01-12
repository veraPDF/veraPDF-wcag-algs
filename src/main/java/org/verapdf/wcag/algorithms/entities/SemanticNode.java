package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SemanticNode implements INode {

	private double correctSemanticScore;
	private int pageNumber;
	private double[] boundingBox;
	private SemanticType semanticType;
	private final List<INode> children;

	public SemanticNode() {
		this.children = new ArrayList<>();
	}

	public SemanticNode(int pageNumber, double[] boundingBox, SemanticType semanticType) {
		this.pageNumber = pageNumber;
		this.boundingBox = boundingBox;
		this.semanticType = semanticType;

		this.children = new ArrayList<>();
	}

	@Override
	public double getCorrectSemanticScore() {
		return correctSemanticScore;
	}

	@Override
	public void setCorrectSemanticScore(double correctSemanticScore) {
		this.correctSemanticScore = correctSemanticScore;
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
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

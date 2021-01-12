package org.verapdf.wcag.algorithms.entity;

import org.verapdf.wcag.algorithms.entity.enums.SemanticType;

import java.util.ArrayList;
import java.util.List;

public class SemanticNode implements INode {

	private final List<INode> children;

	private SemanticType semanticType;
	private double correctSemanticScore;

	public SemanticNode() {
		this.children = new ArrayList<>();
	}

	public SemanticNode(SemanticType semanticType) {
		this.semanticType = semanticType;

		this.children = new ArrayList<>();
	}

	@Override
	public List<INode> getChildren() {
		return children;
	}

	@Override
	public int numChildren() {
		return children.size();
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
	public double getCorrectSemanticScore() {
		return correctSemanticScore;
	}

	@Override
	public void setCorrectSemanticScore(double correctSemanticScore) {
		this.correctSemanticScore = correctSemanticScore;
	}

	@Override
	public boolean isLeaf() {
		return numChildren() < 1;
	}

	//todo doesn't work if add equals and hash code.
}

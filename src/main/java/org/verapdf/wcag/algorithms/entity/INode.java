package org.verapdf.wcag.algorithms.entity;

import org.verapdf.wcag.algorithms.entity.enums.SemanticType;

import java.util.List;

public interface INode {

	List<INode> getChildren();

	int numChildren();

	boolean isLeaf();

	SemanticType getSemanticType();

	void setSemanticType(SemanticType semanticType);

	double getCorrectSemanticScore();

	void setCorrectSemanticScore(double correctSemanticScore);
}

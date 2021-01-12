package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.List;

public interface INode {

	void setSemanticType(SemanticType semanticType);

	double getCorrectSemanticScore();

	void setCorrectSemanticScore(double correctSemanticScore);

	int getPageNumber();

	double getLeftX();

	double getRightX();

	double getBottomY();

	double getTopY();

	double[] getBoundingBox();

	SemanticType getSemanticType();

	List<INode> getChildren();
}

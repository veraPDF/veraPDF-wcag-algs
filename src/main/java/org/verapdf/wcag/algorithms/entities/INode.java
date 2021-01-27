package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.List;

public interface INode {

	void setSemanticType(SemanticType semanticType);

	Double getCorrectSemanticScore();

	void setCorrectSemanticScore(Double correctSemanticScore);

	Integer getPageNumber();

	void setPageNumber(Integer pageNumber);

	Integer getLastPageNumber();

	void setLastPageNumber(Integer lastPageNumber);

	double getLeftX();

	double getRightX();

	double getBottomY();

	double getTopY();

	double[] getBoundingBox();

	void setBoundingBox(double[] boundingBox);

	SemanticType getSemanticType();

	List<INode> getChildren();
}

package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.List;

public interface INode {

	SemanticType getSemanticType();

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

	BoundingBox getBoundingBox();

	void setBoundingBox(BoundingBox boundingBox);

	double getContrastRatio();

	void setContrastRatio(double contrastRatio);

	List<INode> getChildren();
}

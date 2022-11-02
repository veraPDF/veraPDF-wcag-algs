package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.List;

public interface IObject {

	Integer getPageNumber();

	void setPageNumber(Integer pageNumber);

	Integer getLastPageNumber();

	void setLastPageNumber(Integer lastPageNumber);

	double getLeftX();

	double getRightX();

	double getBottomY();

	double getTopY();

	double getWidth();

	double getHeight();

	BoundingBox getBoundingBox();

	void setBoundingBox(BoundingBox boundingBox);

	public List<Integer> getErrorCodes();

	public List<List<Object>> getErrorArguments();

	void setRecognizedStructureId(Long id);

	Long getRecognizedStructureId();

	double getCenterX();

	double getCenterY();
}

package org.verapdf.wcag.algorithms.entity;

public interface IChunk extends INode {

	int getPageNumber();

	double[] getBoundingBox();

	double getLeftX();

	double getRightX();

	double getBottomY();

	double getTopY();
}

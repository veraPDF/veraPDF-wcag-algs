package org.verapdf.wcag.algorithms.entity;

public interface ITextChunk extends IChunk {

	String getText();

	String getFontName();

	double getFontSize();

	double[] getFontColor();

	int getFontWeight();

	double getItalicAngle();

	double getBaseLine();
}

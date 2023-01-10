package org.verapdf.wcag.algorithms.entities;

public interface IAnnotation extends INode {
	String getAnnotationType();

	Integer getDestinationPageNumber();

	Integer getDestinationObjectKeyNumber();
}

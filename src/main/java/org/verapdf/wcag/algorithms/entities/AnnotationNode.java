package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public class AnnotationNode extends SemanticNode implements IAnnotation {
	private final String annotationType;
	private final Integer destinationPageNumber;

	public AnnotationNode(String annotationType, BoundingBox boundingBox, Integer destinationPageNumber) {
		super(boundingBox, SemanticType.ANNOT, SemanticType.ANNOT);
		this.annotationType = annotationType;
		this.destinationPageNumber = destinationPageNumber;
	}

	public String getAnnotationType() {
		return this.annotationType;
	}

	public Integer getDestinationPageNumber() {
		return this.destinationPageNumber;
	}
}

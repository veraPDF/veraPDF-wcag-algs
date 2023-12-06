package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public class AnnotationNode extends InfoChunk implements IAnnotation {
	private final String annotationType;
	private final Integer destinationPageNumber;
	private final Integer destinationObjectKeyNumber;

	public AnnotationNode(String annotationType, BoundingBox boundingBox, Integer destinationPageNumber,
						  Integer destinationObjectKeyNumber) {
		super(boundingBox);
		this.annotationType = annotationType;
		this.destinationPageNumber = destinationPageNumber;
		this.destinationObjectKeyNumber = destinationObjectKeyNumber;
	}

	public String getAnnotationType() {
		return this.annotationType;
	}

	public Integer getDestinationPageNumber() {
		return this.destinationPageNumber;
	}

	public Integer getDestinationObjectKeyNumber() {
		return destinationObjectKeyNumber;
	}
}

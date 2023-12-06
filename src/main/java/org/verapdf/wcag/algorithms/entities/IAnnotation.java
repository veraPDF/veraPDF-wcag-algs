package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.IChunk;

public interface IAnnotation extends IChunk {
	String getAnnotationType();

	Integer getDestinationPageNumber();

	Integer getDestinationObjectKeyNumber();
}

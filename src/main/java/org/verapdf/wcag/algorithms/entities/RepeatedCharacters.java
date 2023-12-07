package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public class RepeatedCharacters {

	private final boolean nonSpace;
	private final Integer numberOfElements;
	private BoundingBox boundingBox;

	public RepeatedCharacters(boolean nonSpace, Integer numberOfElements, BoundingBox boundingBox) {
		this.nonSpace = nonSpace;
		this.numberOfElements = numberOfElements;
		this.boundingBox = boundingBox;
	}

	public RepeatedCharacters(boolean nonSpace, Integer numberOfElements) {
		this.nonSpace = nonSpace;
		this.numberOfElements = numberOfElements;
	}

	public boolean isNonSpace() {
		return nonSpace;
	}

	public Integer getNumberOfElements() {
		return numberOfElements;
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
}

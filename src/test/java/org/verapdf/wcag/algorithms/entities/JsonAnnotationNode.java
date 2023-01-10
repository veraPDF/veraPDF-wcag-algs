package org.verapdf.wcag.algorithms.entities;

public class JsonAnnotationNode extends JsonNode {

	private String annotationType;
	private Integer destinationPageNumber;
	private Integer destinationObjectKeyNumber;

	public String getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(String annotationType) {
		this.annotationType = annotationType;
	}

	public Integer getDestinationPageNumber() {
		return destinationPageNumber;
	}

	public void setDestinationPageNumber(Integer destinationPageNumber) {
		this.destinationPageNumber = destinationPageNumber;
	}

	public Integer getDestinationObjectKeyNumber() {
		return destinationObjectKeyNumber;
	}

	public void setDestinationObjectKeyNumber(Integer destinationObjectKeyNumber) {
		this.destinationObjectKeyNumber = destinationObjectKeyNumber;
	}
}

package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class BaseObject implements IObject {

	private BoundingBox boundingBox;
	private final List<Integer> errorCodes;
	private final List<List<Object>> errorArguments;
	private Long recognizedStructureId = null;

	public BaseObject(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
		this.errorCodes = new LinkedList<>();
		this.errorArguments = new LinkedList<>();
	}

	public BaseObject(BoundingBox boundingBox, List<Integer> errorCodes, List<List<Object>> errorArguments) {
		this.boundingBox = boundingBox;
		this.errorCodes = errorCodes;
		this.errorArguments = errorArguments;
	}

	@Override
	public Integer getPageNumber() {
		return boundingBox.getPageNumber();
	}

	@Override
	public void setPageNumber(Integer pageNumber) {
		boundingBox.setPageNumber(pageNumber);
	}

	@Override
	public Integer getLastPageNumber() {
		return boundingBox.getLastPageNumber();
	}

	@Override
	public void setLastPageNumber(Integer lastPageNumber) {
		boundingBox.setLastPageNumber(lastPageNumber);
	}

	@Override
	public double getLeftX() {
		return boundingBox.getLeftX();
	}

	@Override
	public double getBottomY() {
		return boundingBox.getBottomY();
	}

	@Override
	public double getRightX() {
		return boundingBox.getRightX();
	}

	@Override
	public double getTopY() {
		return boundingBox.getTopY();
	}

	@Override
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public void setBoundingBox(BoundingBox bbox) {
		if (bbox instanceof MultiBoundingBox) {
			boundingBox = new MultiBoundingBox(bbox);
		} else {
			boundingBox = new BoundingBox(bbox);
		}
	}

	@Override
	public List<Integer> getErrorCodes() {
		return errorCodes;
	}

	@Override
	public List<List<Object>> getErrorArguments() {
		return errorArguments;
	}

	@Override
	public Long getRecognizedStructureId() {
		return recognizedStructureId;
	}

	@Override
	public void setRecognizedStructureId(Long id) {
		recognizedStructureId = id;
	}

	public double getWidth() {
		return boundingBox.getWidth();
	}

	public double getHeight() {
		return boundingBox.getHeight();
	}

	@Override
	public double getCenterX() {
		return getBoundingBox().getCenterX();
	}

	@Override
	public double getCenterY() {
		return getBoundingBox().getCenterY();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getBoundingBox());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BaseObject that = (BaseObject) o;
		return that.getBoundingBox().equals(getBoundingBox());
	}
}

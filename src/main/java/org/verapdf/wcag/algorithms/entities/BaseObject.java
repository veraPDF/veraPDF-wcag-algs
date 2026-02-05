/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
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
	protected Integer index = null;
	protected String level = null;

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

	public BaseObject(BaseObject baseObject) {
		this.boundingBox = baseObject.boundingBox;
		this.errorCodes = baseObject.errorCodes;
		this.errorArguments = baseObject.errorArguments;
		this.recognizedStructureId = baseObject.recognizedStructureId;
		this.index = baseObject.index;
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

	@Override
	public double getWidth() {
		return boundingBox.getWidth();
	}

	@Override
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
	public Integer getIndex() {
		return index;
	}

	@Override
	public void setIndex(Integer index) {
		this.index = index;
	}

	@Override
	public String getLevel() {
		return level;
	}

	@Override
	public void setLevel(String level) {
		this.level = level;
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

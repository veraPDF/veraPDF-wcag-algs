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
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.StreamInfo;

import java.util.List;

public interface IObject {

	Integer getPageNumber();

	void setPageNumber(Integer pageNumber);

	Integer getLastPageNumber();

	void setLastPageNumber(Integer lastPageNumber);

	double getLeftX();

	double getRightX();

	double getBottomY();

	double getTopY();

	double getWidth();

	double getHeight();

	BoundingBox getBoundingBox();

	void setBoundingBox(BoundingBox boundingBox);

	public List<Integer> getErrorCodes();

	public List<List<Object>> getErrorArguments();

	public List<StreamInfo> getStreamInfos();

	void setRecognizedStructureId(Long id);

	Long getRecognizedStructureId();

	double getCenterX();

	double getCenterY();
	
	Integer getIndex();

	void setIndex(Integer index);

	String getLevel();

	void setLevel(String level);

}

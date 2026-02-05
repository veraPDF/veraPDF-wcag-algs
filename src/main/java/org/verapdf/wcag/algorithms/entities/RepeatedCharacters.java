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

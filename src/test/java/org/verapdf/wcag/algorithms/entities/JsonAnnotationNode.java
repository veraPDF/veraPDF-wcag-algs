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

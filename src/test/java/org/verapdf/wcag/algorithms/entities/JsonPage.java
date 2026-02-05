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

import java.util.List;
import java.util.Objects;

public class JsonPage {

	private int pageNumber;
	private String pageLabel;
	private List<JsonNode> artifacts;

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public String getPageLabel() {
		return pageLabel;
	}

	public void setPageLabel(String pageLabel) {
		this.pageLabel = pageLabel;
	}

	public List<JsonNode> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<JsonNode> artifacts) {
		this.artifacts = artifacts;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JsonPage jsonPage = (JsonPage) o;
		return Objects.equals(artifacts, jsonPage.artifacts) &&
				Objects.equals(pageLabel, jsonPage.pageLabel) && jsonPage.pageNumber == pageNumber;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pageNumber, pageLabel, artifacts);
	}

	@Override
	public String toString() {
		return "JsonPage{" +
		       ", pageNumber=" + pageNumber +
				", pageLabel=" + pageLabel +
				", artifacts=" + artifacts +
				'}';
	}
}

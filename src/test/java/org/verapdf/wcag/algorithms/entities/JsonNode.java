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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		property = "type",
		visible = true)
@JsonTypeIdResolver(NodeIdResolver.class)
public class JsonNode {

	private String type;
	private Integer objectKeyNumber;
	private int pageNumber;
	private double[] boundingBox;
	private List<JsonNode> children;
	private List<JsonPage> pages;
	private JsonAttributes attributes;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getObjectKeyNumber() {
		return objectKeyNumber;
	}

	public void setObjectKeyNumber(Integer objectKeyNumber) {
		this.objectKeyNumber = objectKeyNumber;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public double[] getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(double[] boundingBox) {
		this.boundingBox = boundingBox;
	}

	public List<JsonNode> getChildren() {
		return children;
	}

	public void setChildren(List<JsonNode> children) {
		this.children = children;
	}

	public List<JsonPage> getPages() {
		return pages;
	}

	public void setPages(List<JsonPage> pages) {
		this.pages = pages;
	}

	public JsonAttributes getAttributes() {
		return attributes;
	}

	public void setAttributes(JsonAttributes attributes) {
		this.attributes = attributes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JsonNode jsonNode = (JsonNode) o;
		return pageNumber == jsonNode.pageNumber &&
		       Objects.equals(objectKeyNumber, jsonNode.objectKeyNumber) &&
		       type.equals(jsonNode.type) &&
		       Arrays.equals(boundingBox, jsonNode.boundingBox) &&
		       children.equals(jsonNode.children) &&
		       Objects.equals(pages, jsonNode.pages) &&
		       Objects.equals(attributes, jsonNode.attributes);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(type, objectKeyNumber, pageNumber, children, pages, attributes);
		result = 31 * result + Arrays.hashCode(boundingBox);
		return result;
	}

	@Override
	public String toString() {
		return "JsonNode{" +
		       "type='" + type + '\'' +
		       ", objectKeyNumber=" + objectKeyNumber +
		       ", pageNumber=" + pageNumber +
		       ", boundingBox=" + Arrays.toString(boundingBox) +
		       ", children=" + children +
		       ", pages=" + pages +
		       ", attributes=" + attributes +
		       '}';
	}
}

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
		       type.equals(jsonNode.type) &&
		       Arrays.equals(boundingBox, jsonNode.boundingBox) &&
		       children.equals(jsonNode.children) &&
		       Objects.equals(pages, jsonNode.pages) &&
		       Objects.equals(attributes, jsonNode.attributes);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(type, pageNumber, children, pages, attributes);
		result = 31 * result + Arrays.hashCode(boundingBox);
		return result;
	}

	@Override
	public String toString() {
		return "JsonNode{" +
		       "type='" + type + '\'' +
		       ", pageNumber=" + pageNumber +
		       ", boundingBox=" + Arrays.toString(boundingBox) +
		       ", children=" + children +
		       ", pages=" + pages +
		       ", attributes=" + attributes +
		       '}';
	}
}

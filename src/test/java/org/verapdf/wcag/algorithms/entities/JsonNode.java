package org.verapdf.wcag.algorithms.entities;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class JsonNode {

	private String type;
	private String value;
	private String fontName;
	private double fontSize;
	private double fontWeight;
	private double italicAngle;
	private double baseLine;
	private int pageNumber;
	private double[] color;
	private double[] boundingBox;
	private List<JsonNode> children;

	public JsonNode() {
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	public double getFontSize() {
		return fontSize;
	}

	public void setFontSize(double fontSize) {
		this.fontSize = fontSize;
	}

	public double getFontWeight() {
		return fontWeight;
	}

	public void setFontWeight(double fontWeight) {
		this.fontWeight = fontWeight;
	}

	public double getItalicAngle() {
		return italicAngle;
	}

	public void setItalicAngle(double italicAngle) {
		this.italicAngle = italicAngle;
	}

	public double getBaseLine() {
		return baseLine;
	}

	public void setBaseLine(double baseLine) {
		this.baseLine = baseLine;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public double[] getColor() {
		return color;
	}

	public void setColor(double[] color) {
		this.color = color;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JsonNode jsonNode = (JsonNode) o;
		return Double.compare(jsonNode.fontSize, fontSize) == 0 && fontWeight == jsonNode.fontWeight && Double.compare(jsonNode.italicAngle, italicAngle) == 0 && Double.compare(jsonNode.baseLine, baseLine) == 0 && pageNumber == jsonNode.pageNumber && Objects.equals(type, jsonNode.type) && Objects.equals(value, jsonNode.value) && Objects.equals(fontName, jsonNode.fontName) && Arrays.equals(color, jsonNode.color) && Arrays.equals(boundingBox, jsonNode.boundingBox) && Objects.equals(children, jsonNode.children);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(type, value, fontName, fontSize, fontWeight, italicAngle, baseLine, pageNumber, children);
		result = 31 * result + Arrays.hashCode(color);
		result = 31 * result + Arrays.hashCode(boundingBox);
		return result;
	}

	@Override
	public String toString() {
		return "JsonNode{" +
		       "type='" + type + '\'' +
		       ", value='" + value + '\'' +
		       ", fontName='" + fontName + '\'' +
		       ", fontSize=" + fontSize +
		       ", fontWeight=" + fontWeight +
		       ", italicAngle=" + italicAngle +
		       ", baseLine=" + baseLine +
		       ", pageNumber=" + pageNumber +
		       ", color=" + Arrays.toString(color) +
		       ", boundingBox=" + Arrays.toString(boundingBox) +
		       ", children=" + children +
		       '}';
	}
}

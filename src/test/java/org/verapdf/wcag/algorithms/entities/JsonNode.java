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
	private double startX;
	private double startY;
	private double endX;
	private double endY;
	private double width;
	private String fontColorSpace;
	private String annotationType;
	private Integer destinationPageNumber;
	private double slantDegree;
	private double[] boundingBox;
	private List<Double> symbolEnds;
	private List<JsonNode> children;
	private List<JsonPage> pages;

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

	public Integer getDestinationPageNumber() {
		return destinationPageNumber;
	}

	public void setDestinationPageNumber(Integer destinationPageNumber) {
		this.destinationPageNumber = destinationPageNumber;
	}

	public double[] getColor() {
		return color;
	}

	public void setColor(double[] color) {
		this.color = color;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public String getFontColorSpace() {
		return fontColorSpace;
	}

	public void setFontColorSpace(String fontColorSpace) {
		this.fontColorSpace = fontColorSpace;
	}

	public double[] getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(double[] boundingBox) {
		this.boundingBox = boundingBox;
	}

	public List<Double> getSymbolEnds() {
		return symbolEnds;
	}

	public void setSymbolEnds(List<Double> symbolEnds) {
		this.symbolEnds = symbolEnds;
	}

	public List<JsonNode> getChildren() {
		return children;
	}

	public void setChildren(List<JsonNode> children) {
		this.children = children;
	}

	public double getStartX() {
		return startX;
	}

	public void setStartX(double startX) {
		this.startX = startX;
	}

	public double getStartY() {
		return startY;
	}

	public void setStartY(double startY) {
		this.startY = startY;
	}

	public double getEndX() {
		return endX;
	}

	public void setEndX(double endX) {
		this.endX = endX;
	}

	public double getEndY() {
		return endY;
	}

	public void setEndY(double endY) {
		this.endY = endY;
	}

	public List<JsonPage> getPages() {
		return pages;
	}

	public void setPages(List<JsonPage> pages) {
		this.pages = pages;
	}

	public double getSlantDegree() {
		return slantDegree;
	}

	public void setSlantDegree(double slantDegree) {
		this.slantDegree = slantDegree;
	}

	public String getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(String annotationType) {
		this.annotationType = annotationType;
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
		return Double.compare(jsonNode.fontSize, fontSize) == 0 &&
				Double.compare(jsonNode.italicAngle, italicAngle) == 0 && Objects.equals(children, jsonNode.children) &&
				Double.compare(jsonNode.baseLine, baseLine) == 0 && Objects.equals(value, jsonNode.value) &&
				Objects.equals(type, jsonNode.type) && jsonNode.pageNumber == pageNumber &&
				Arrays.equals(boundingBox, jsonNode.boundingBox) && Arrays.equals(color, jsonNode.color) &&
				Double.compare(jsonNode.slantDegree, slantDegree) == 0 && Objects.equals(pages, jsonNode.pages) &&
				Objects.equals(fontName, jsonNode.fontName) && Double.compare(jsonNode.fontWeight, fontWeight) == 0 &&
				Double.compare(jsonNode.startX, startX) == 0 && Double.compare(jsonNode.startY, startY) == 0 &&
				Double.compare(jsonNode.endX, endX) == 0 && Double.compare(jsonNode.endY, endY) == 0 &&
				Objects.equals(jsonNode.annotationType, annotationType) && Double.compare(jsonNode.width, width) == 0 &&
				Objects.equals(jsonNode.destinationPageNumber, destinationPageNumber);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(type, value, fontName, fontSize, fontWeight, italicAngle, baseLine, pageNumber,
				children, pages, startX, startY, endX, endY, width, slantDegree, annotationType, destinationPageNumber);
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
				", startX=" + startX +
				", startY=" + startY +
				", endX=" + endX +
				", endY=" + endY +
				", width=" + width +
				", annotationType=" + annotationType +
				", destinationPageNumber=" + destinationPageNumber +
				", slantDegree=" + slantDegree +
				", color=" + Arrays.toString(color) +
				", boundingBox=" + Arrays.toString(boundingBox) +
				", children=" + children +
				", pages=" + pages +
				'}';
	}
}

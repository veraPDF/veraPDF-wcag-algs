package org.verapdf.wcag.algorithms.entities;

import java.util.List;

public class JsonTextChunk extends JsonNode {

	private String value;
	private String fontName;
	private double fontSize;
	private double fontWeight;
	private double italicAngle;
	private double baseLine;
	private double[] color;
	private String fontColorSpace;
	private double slantDegree;
	private List<Double> symbolEnds;

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

	public double[] getColor() {
		return color;
	}

	public void setColor(double[] color) {
		this.color = color;
	}

	public String getFontColorSpace() {
		return fontColorSpace;
	}

	public void setFontColorSpace(String fontColorSpace) {
		this.fontColorSpace = fontColorSpace;
	}

	public double getSlantDegree() {
		return slantDegree;
	}

	public void setSlantDegree(double slantDegree) {
		this.slantDegree = slantDegree;
	}

	public List<Double> getSymbolEnds() {
		return symbolEnds;
	}

	public void setSymbolEnds(List<Double> symbolEnds) {
		this.symbolEnds = symbolEnds;
	}
}

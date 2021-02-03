package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.Arrays;
import java.util.Objects;

public class SemanticTextChunk extends SemanticNode {

	private String text;
	private String fontName;
	private double fontSize;
	private double fontWeight;
	private double italicAngle;
	private double baseLine;
	private double[] fontColor;

	public SemanticTextChunk() {
	}

	public SemanticTextChunk(int pageNumber, double[] boundingBox, String text, double fontSize, double baseLine) {
		super(pageNumber, boundingBox, SemanticType.SPAN);
		this.text = text;
		this.fontSize = fontSize;
		this.baseLine = baseLine;
	}

	public SemanticTextChunk(int pageNumber, double[] boundingBox, String text, String fontName, double fontSize,
							 double fontWeight, double italicAngle, double baseLine, double[] fontColor) {
		super(pageNumber, boundingBox, SemanticType.SPAN);
		this.text = text;
		this.fontName = fontName;
		this.fontSize = fontSize;
		this.fontWeight = fontWeight;
		this.italicAngle = italicAngle;
		this.baseLine = baseLine;
		this.fontColor = fontColor.clone();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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

	public double[] getFontColor() {
		return fontColor;
	}

	public void setFontColor(double[] fontColor) {
		this.fontColor = fontColor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		SemanticTextChunk that = (SemanticTextChunk) o;
		return Double.compare(that.fontSize, fontSize) == 0
		       && Double.compare(that.fontWeight, fontWeight) == 0
		       && Double.compare(that.italicAngle, italicAngle) == 0
		       && Double.compare(that.baseLine, baseLine) == 0
		       && Objects.equals(text, that.text)
		       && Objects.equals(fontName, that.fontName)
		       && Arrays.equals(fontColor, that.fontColor);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(super.hashCode(), text, fontName, fontSize, fontWeight, italicAngle, baseLine);
		result = 31 * result + Arrays.hashCode(fontColor);
		return result;
	}

	@Override
	public String toString() {
		return "SemanticTextChunk{" +
		       "text='" + text + '\'' +
		       ", fontName='" + fontName + '\'' +
		       ", fontSize=" + fontSize +
		       ", fontWeight=" + fontWeight +
		       ", italicAngle=" + italicAngle +
		       ", baseLine=" + baseLine +
		       ", fontColor=" + Arrays.toString(fontColor) +
		       '}';
	}
}

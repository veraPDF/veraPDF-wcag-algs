package org.verapdf.wcag.algorithms.entity;

import org.verapdf.wcag.algorithms.entity.enums.SemanticType;

import java.util.Arrays;
import java.util.Objects;

public class SemanticTextChunk extends SemanticChunk implements ITextChunk {

	private String text;
	private String fontName;
	private double fontSize;
	private int fontWeight;
	private double italicAngle;
	private double baseLine;
	private double[] fontColor;

	public SemanticTextChunk(SemanticType semanticType, double[] boundingBox, int pageNumber) {
		super(semanticType, boundingBox, pageNumber);
	}

	public SemanticTextChunk(double[] boundingBox, int pageNumber, String text, double fontSize, double baseLine) {
		super(SemanticType.SPAN, boundingBox, pageNumber);
		this.text = text;
		this.fontSize = fontSize;
		this.baseLine = baseLine;
	}

	public SemanticTextChunk(double[] boundingBox, int pageNumber, String text, String fontName, double fontSize,
	                         int fontWeight, double italicAngle, double baseLine, double[] fontColor) {
		super(SemanticType.SPAN, boundingBox, pageNumber);
		this.text = text;
		this.fontName = fontName;
		this.fontSize = fontSize;
		this.fontWeight = fontWeight;
		this.italicAngle = italicAngle;
		this.baseLine = baseLine;
		this.fontColor = fontColor.clone();
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getFontName() {
		return fontName;
	}

	@Override
	public double getFontSize() {
		return fontSize;
	}

	@Override
	public int getFontWeight() {
		return fontWeight;
	}

	@Override
	public double getItalicAngle() {
		return italicAngle;
	}

	@Override
	public double getBaseLine() {
		return baseLine;
	}

	@Override
	public double[] getFontColor() {
		return fontColor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SemanticTextChunk that = (SemanticTextChunk) o;
		return Double.compare(that.fontSize, fontSize) == 0 && fontWeight == that.fontWeight
		       && Double.compare(that.italicAngle, italicAngle) == 0
		       && Double.compare(that.baseLine, baseLine) == 0
		       && Objects.equals(text, that.text)
		       && Objects.equals(fontName, that.fontName)
		       && Arrays.equals(fontColor, that.fontColor);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(text, fontName, fontSize, fontWeight, italicAngle, baseLine);
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

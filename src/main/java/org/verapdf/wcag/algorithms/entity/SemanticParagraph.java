package org.verapdf.wcag.algorithms.entity;

import org.verapdf.wcag.algorithms.entity.enums.SemanticType;

import java.util.Objects;

public class SemanticParagraph extends SemanticChunk {

    private boolean enclosedTop;
    private boolean enclosedBottom;
    private ITextChunk firstLine;
    private ITextChunk lastLine;
    private int lastPageNumber;
    private int indentation; // 0 - left, 1 - right, 2 - center

    public SemanticParagraph(SemanticType semanticType, double[] boundingBox, int pageNumber) {
        super(semanticType, boundingBox, pageNumber);
    }

    public SemanticParagraph(double[] boundingBox, int pageNumber, ITextChunk firstLine,
                             ITextChunk lastLine, int lastPageNumber) {
        super(SemanticType.PARAGRAPH, boundingBox, pageNumber);
        this.firstLine = firstLine;
        this.lastLine = lastLine;
        this.lastPageNumber = lastPageNumber;
    }

    public boolean isEnclosedTop() {
        return enclosedTop;
    }

    public void setEnclosedTop(boolean enclosedTop) {
        this.enclosedTop = enclosedTop;
    }

    public boolean isEnclosedBottom() {
        return enclosedBottom;
    }

    public void setEnclosedBottom(boolean enclosedBottom) {
        this.enclosedBottom = enclosedBottom;
    }

    public ITextChunk getFirstLine() {
        return firstLine;
    }

    public void setFirstLine(ITextChunk firstLine) {
        this.firstLine = firstLine;
    }

    public ITextChunk getLastLine() {
        return lastLine;
    }

    public void setLastLine(ITextChunk lastLine) {
        this.lastLine = lastLine;
    }

    public int getLastPageNumber() {
        return lastPageNumber;
    }

    public void setLastPageNumber(int lastPageNumber) {
        this.lastPageNumber = lastPageNumber;
    }

    public int getIndentation() {
        return indentation;
    }

    public void setIndentation(int indentation) {
        this.indentation = indentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SemanticParagraph that = (SemanticParagraph) o;
        return enclosedTop == that.enclosedTop
               && enclosedBottom == that.enclosedBottom
               && lastPageNumber == that.lastPageNumber
               && indentation == that.indentation
               && Objects.equals(firstLine, that.firstLine)
               && Objects.equals(lastLine, that.lastLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enclosedTop, enclosedBottom, firstLine, lastLine, lastPageNumber, indentation);
    }

    @Override
    public String toString() {
        return "SemanticParagraph{" +
               "enclosedTop=" + enclosedTop +
               ", enclosedBottom=" + enclosedBottom +
               ", firstLine=" + firstLine +
               ", lastLine=" + lastLine +
               ", lastPageNumber=" + lastPageNumber +
               ", indentation=" + indentation +
               '}';
    }
}

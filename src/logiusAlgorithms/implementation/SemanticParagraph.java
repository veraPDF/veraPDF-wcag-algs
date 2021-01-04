package logiusAlgorithms.implementation;

import logiusAlgorithms.interfaces.TextChunk;

public class SemanticParagraph extends SemanticChunk {
    public final static String SEMANTIC_TYPE = "PARAGRAPH";
    protected boolean enclosedTop;
    protected TextChunk firstLine;
    protected boolean enclosedBottom;
    protected TextChunk lastLine;
    protected int lastPageNumber;
    protected int indentation; // 0 - left, 1 - right, 2 - center

    public SemanticParagraph(TextChunk firstLine, TextChunk lastLine, double[] boundingBox,
                             int pageNumber, int lastPageNumber) {
        super(SEMANTIC_TYPE, boundingBox, pageNumber);
        this.firstLine = firstLine;
        this.lastLine = lastLine;
        this.lastPageNumber = lastPageNumber;
    }

    public TextChunk getFirstLine() {
        return firstLine;
    }

    public TextChunk getLastLine() {
        return lastLine;
    }

    public int getLastPageNumber() {
        return lastPageNumber;
    }

    public int getIndentation() { return indentation; }

    public void setBoundingBox(double[] boundingBox) { this.boundingBox = boundingBox; }

    public void setLastPageNumber(int lastPageNumber) { this.lastPageNumber = lastPageNumber; }

    public void setFirstLine(TextChunk firstLine) { this.firstLine = firstLine; }

    public void setLastLine(TextChunk lastLine) { this.lastLine = lastLine; }

}

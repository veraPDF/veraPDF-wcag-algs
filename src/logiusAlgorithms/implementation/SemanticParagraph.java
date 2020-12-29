package logiusAlgorithms.implementation;

import logiusAlgorithms.interfaces.TextChunk;

public class SemanticParagraph extends SemanticChunk {
    public final static String SEMANTIC_TYPE = "PARAGRAPH";

    protected boolean enclosedTop;
    protected TextChunk firstTextLine;
    protected boolean enclosedBottom;
    protected TextChunk lastTextLine;
    protected int lastPageNumber;

    public SemanticParagraph(TextChunk firstTextLine, TextChunk lastTextLine, double[] boundingBox,
                             int pageNumber, int lastPageNumber) {
        super(SEMANTIC_TYPE, boundingBox, pageNumber);
        this.firstTextLine = firstTextLine;
        this.lastTextLine = lastTextLine;
        this.lastPageNumber = lastPageNumber;

        setEnclosedTop(firstTextLine);
        setEnclosedBottom(lastTextLine);
    }

    // todo add fontSize ane rewrite
    private void setEnclosedTop(TextChunk firstTextLine) {
        enclosedTop = Math.abs(getLeftX() - firstTextLine.getLeftX()) > 1e-5;
    }

    private void setEnclosedBottom(TextChunk lastTextLine) {
        enclosedBottom = Math.abs(getRightX() - lastTextLine.getRightX()) > 1e-5;
    }

}

package logiusAlgorithms.implementation;

import logiusAlgorithms.interfaces.ChunkTypes;
import logiusAlgorithms.interfaces.TextChunk;

public class SemanticTextChunk extends SemanticChunk implements TextChunk {
    public final static ChunkTypes CHUNK_TYPE = ChunkTypes.TEXT_CHUNK;

    private String text;
    private String fontName;
    private double fontSize;
    private int fontWeight;
    private double italicAngle;
    private double[] fontColor;
    private double baseLine;

    public SemanticTextChunk(String text, String fontName, double fontSize, int fontWeight, double italicAngle,
                             double[] fontColor, double[] boundingBox, double baseLine, int pageNumber) {
        super(pageNumber, boundingBox);
        this.accumulatedChunk = this;
        this.text = text;
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.fontWeight = fontWeight;
        this.italicAngle = italicAngle;
        this.fontColor = fontColor.clone();
        this.baseLine = baseLine;
    }

    @Override
    public String getText() { return text; }

    @Override
    public String getFontName() { return fontName; }

    @Override
    public double getFontSize() { return fontSize; }

    @Override
    public double[] getFontColor() { return fontColor; }

    @Override
    public int getFontWeight() { return fontWeight; }

    @Override
    public double getItalicAngle() { return italicAngle; }

    @Override
    public double getBaseLine() { return baseLine; }

    @Override
    public ChunkTypes getChunkType() { return CHUNK_TYPE; }

//    @Override
//    public int compareTo(SemanticTextChunk o) {
//        double baseLinesDifference = Math.abs(baseLine - o.getBaseLine());
//        if (baseLinesDifference > EPS)
//            return Double.compare(baseLine, o.getBaseLine());
//
//        return Double.compare(boundingBox[0], o.getBoundingBox()[0]);
//    }
}

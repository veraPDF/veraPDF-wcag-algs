package logiusAlgorithms.interfaces;

public interface TextChunk extends Chunk {
    String getText();
    String getFontName();
    double getFontSize();
    double[] getFontColor();
    int getFontWeight();
    double getItalicAngle();
    double getBaseLine();
}

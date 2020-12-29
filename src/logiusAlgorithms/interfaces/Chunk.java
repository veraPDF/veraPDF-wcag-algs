package logiusAlgorithms.interfaces;

public interface Chunk extends Node {
    int getPageNumber();
    double[] getBoundingBox();
    double getLeftX();
    double getRightX();
    double getBottomY();
    double getTopY();
}

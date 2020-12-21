package logiusAlgorithms.implementation;

import logiusAlgorithms.interfaces.Chunk;

public abstract class SemanticChunk extends SemanticNode implements Chunk {
    protected int pageNumber;
    protected double[] boundingBox;

    public SemanticChunk(int pageNumber, double[] boundingBox) {
        this.pageNumber = pageNumber;
        this.boundingBox = boundingBox;
    }

    @Override
    public int getPageNumber() { return pageNumber; }

    @Override
    public double[] getBoundingBox() { return boundingBox; }

    @Override
    public double getLeftX() { return boundingBox[0]; }

    @Override
    public double getBottomY() { return boundingBox[1]; }

    @Override
    public double getRightX() { return boundingBox[2]; }

    @Override
    public double getTopY() { return boundingBox[3]; }
}

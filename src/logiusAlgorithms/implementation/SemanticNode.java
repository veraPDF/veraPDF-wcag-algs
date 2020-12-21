package logiusAlgorithms.implementation;

import logiusAlgorithms.interfaces.Chunk;
import logiusAlgorithms.interfaces.Node;

import java.util.ArrayList;
import java.util.List;

public class SemanticNode implements Node {
    protected List<Node> children;
    protected Chunk accumulatedChunk;

    @Override
    public List<Node> getChildren() { return children; }

    @Override
    public int getNumChildren() { return children == null ? 0 : children.size(); }

    @Override
    public boolean isLeaf() { return getNumChildren() < 1; }

    public void initChildren() { children = new ArrayList<>(); }

    public void addChild(SemanticNode child) { children.add(child); }

    @Override
    public Chunk getAccumulatedChunk() { return accumulatedChunk; }

    @Override
    public void setAccumulatedChunk(Chunk accumulatedChunk) { this.accumulatedChunk = accumulatedChunk; }
}

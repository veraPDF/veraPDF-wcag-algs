package logiusAlgorithms.implementation;

import logiusAlgorithms.interfaces.Node;

import java.util.ArrayList;
import java.util.List;

public class SemanticNode implements Node {
    protected List<Node> children;
    protected String semanticType;
    protected double correctSemanticScore;

    public SemanticNode() {}
    public SemanticNode(String semanticType) {
        this.semanticType = semanticType;
    }

    @Override
    public String getSemanticType() { return semanticType; }

    @Override
    public double getCorrectSemanticScore() { return correctSemanticScore; }

    @Override
    public void setCorrectSemanticScore(double correctSemanticScore) {
        this.correctSemanticScore = correctSemanticScore;
    }

    @Override
    public List<Node> getChildren() { return children; }

    @Override
    public int numChildren() { return children == null ? 0 : children.size(); }

    @Override
    public boolean isLeaf() { return numChildren() < 1; }

    public void initChildren() { children = new ArrayList<>(); }

    public void addChild(SemanticNode child) { children.add(child); }
}

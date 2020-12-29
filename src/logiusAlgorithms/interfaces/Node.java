package logiusAlgorithms.interfaces;

import java.util.List;

public interface Node {
    List<Node> getChildren();
    int numChildren();
    boolean isLeaf();
    String getSemanticType();
    double getCorrectSemanticScore();
    void setCorrectSemanticScore(double correctSemanticScore);
}

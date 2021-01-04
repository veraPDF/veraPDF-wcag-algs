package logiusAlgorithms.interfaces;

import java.util.List;

public interface Node {
    List<Node> getChildren();
    int numChildren();
    boolean isLeaf();
    String getSemanticType();
    void setSemanticType(String semanticType);
    double getCorrectSemanticScore();
    void setCorrectSemanticScore(double correctSemanticScore);
}

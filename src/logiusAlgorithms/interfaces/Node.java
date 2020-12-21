package logiusAlgorithms.interfaces;

import java.util.List;

// make iterable?
public interface Node {
    List<Node> getChildren();
    int getNumChildren();
    boolean isLeaf();
    Chunk getAccumulatedChunk();
    void setAccumulatedChunk(Chunk accumulatedChunk);
}

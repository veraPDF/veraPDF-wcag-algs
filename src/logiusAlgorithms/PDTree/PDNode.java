package logiusAlgorithms.PDTree;

import logiusAlgorithms.tree.Node;

import java.util.ArrayList;

public class PDNode extends Node {
    private String type;

    public PDNode(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void initChildren() {
        children = new ArrayList<>();
    }

    public void addChild(PDNode child) {
        children.add(child);
    }
}

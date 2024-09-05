package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.ArrayList;
import java.util.List;

public class SemanticHeaderOrFooter extends SemanticNode {
    private List<IObject> contents = new ArrayList<>();
    
    public SemanticHeaderOrFooter(SemanticType semanticType) {
        setSemanticType(semanticType);
    }

    public List<IObject> getContents() {
        return contents;
    }

    public void setContents(List<IObject> contents) {
        this.contents = contents;
    }

    public void addContents(List<IObject> contents) {
        for (IObject content : contents) {
            this.contents.add(content);
            getBoundingBox().union(content.getBoundingBox());
        }
    }
}

package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.LinkedList;
import java.util.List;

public class SemanticAnnot extends SemanticNode {

	protected final List<AnnotationNode> annotationNodes = new LinkedList<>();
    
    public SemanticAnnot(SemanticAnnot annotationNode) {
        this.addAnnots(annotationNode.getAnnots());
        setSemanticType(SemanticType.ANNOT);
    }

    public SemanticAnnot(AnnotationNode annotationNode) {
        super(annotationNode.getBoundingBox());
        this.annotationNodes.add(annotationNode);
        setSemanticType(SemanticType.ANNOT);
    }

    public void addAnnots(List<AnnotationNode> annots) {
        this.annotationNodes.addAll(annots);
        for (AnnotationNode annotationNode : annots) {
            getBoundingBox().union(annotationNode.getBoundingBox());
        }
    }

	public List<AnnotationNode> getAnnots() {
		return annotationNodes;
	}
    
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (!(o instanceof SemanticAnnot)) {
            return false;
        }
        SemanticAnnot that = (SemanticAnnot) o;
        return this.annotationNodes.equals(that.getAnnots());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + annotationNodes.size();
        for (AnnotationNode annotationNode : annotationNodes) {
            result = 31 * result + annotationNode.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("SemanticAnnot{");
        result.append("pageNumber=");
        result.append(getBoundingBox().getPageNumber());
        result.append(", boundingBox=");
        result.append(getBoundingBox());
        result.append("}");
        return result.toString();
    }
}

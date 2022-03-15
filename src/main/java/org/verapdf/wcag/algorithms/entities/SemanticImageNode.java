package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.ImageChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

public class SemanticImageNode extends SemanticNode {

	protected final ImageChunk image;

    public SemanticImageNode(SemanticImageNode imageNode) {
        super(imageNode.getBoundingBox());
        this.image = imageNode.getImage();
        setSemanticType(SemanticType.FIGURE);
    }

    public SemanticImageNode(ImageChunk image) {
        super(image.getBoundingBox());
        this.image = image;
        setSemanticType(SemanticType.FIGURE);
    }

	public ImageChunk getImage() {
		return image;
	}

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (!(o instanceof SemanticImageNode)) {
            return false;
        }
        SemanticImageNode that = (SemanticImageNode) o;
        return this.image.equals(that.getImage());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return 31 * result + image.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("SemanticImageNode{");
        result.append("pageNumber=");
        result.append(getBoundingBox().getPageNumber());
        result.append(", boundingBox=");
        result.append(getBoundingBox());
        result.append("}");
        return result.toString();
    }
}

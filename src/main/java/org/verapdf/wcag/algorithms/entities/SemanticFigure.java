package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.LineArtChunk;

public class SemanticFigure extends SemanticNode {

	protected final LineArtChunk lineArt;

    public SemanticFigure(SemanticFigure figureNode) {
        super(figureNode.getBoundingBox());
        this.lineArt = figureNode.getLineArt();
    }

    public SemanticFigure(LineArtChunk lineArt) {
        super(lineArt.getBoundingBox());
        this.lineArt = lineArt;
    }

	public LineArtChunk getLineArt() {
		return lineArt;
	}

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (!(o instanceof SemanticFigure)) {
            return false;
        }
        SemanticFigure that = (SemanticFigure) o;
        return this.lineArt.equals(that.getLineArt());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return 31 * result + lineArt.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("SemanticFigure{");
        result.append("pageNumber=");
        result.append(getBoundingBox().getPageNumber());
        result.append(", boundingBox=");
        result.append(getBoundingBox());
        result.append("}");
        return result.toString();
    }
}

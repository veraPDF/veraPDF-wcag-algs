package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.ImageChunk;
import org.verapdf.wcag.algorithms.entities.content.LineArtChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.LinkedList;
import java.util.List;

public class SemanticFigure extends SemanticNode {

	protected final List<ImageChunk> images = new LinkedList<>();
    protected final List<LineArtChunk> lineArts = new LinkedList<>();

    public SemanticFigure(SemanticFigure imageNode) {
        this.addImages(imageNode.getImages());
        this.addLineArts(imageNode.getLineArts());
        setSemanticType(SemanticType.FIGURE);
    }

    public SemanticFigure(ImageChunk image) {
        super(image.getBoundingBox());
        this.images.add(image);
        setSemanticType(SemanticType.FIGURE);
    }

    public SemanticFigure(LineArtChunk lineArtChunk) {
        super(lineArtChunk.getBoundingBox());
        this.lineArts.add(lineArtChunk);
        setSemanticType(SemanticType.FIGURE);
    }

    public void addImages(List<ImageChunk> images) {
        this.images.addAll(images);
        for (ImageChunk imageChunk : images) {
            getBoundingBox().union(imageChunk.getBoundingBox());
        }
    }

    public void addLineArts(List<LineArtChunk> lineArts) {
        this.lineArts.addAll(lineArts);
        for (LineArtChunk lineArtChunk : lineArts) {
            getBoundingBox().union(lineArtChunk.getBoundingBox());
        }
    }

	public List<ImageChunk> getImages() {
		return images;
	}

	public List<LineArtChunk> getLineArts() {
        return lineArts;
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
        return this.images.equals(that.getImages()) && this.lineArts.equals(that.getLineArts());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + images.size();
        for (ImageChunk imageChunk : images) {
            result = 31 * result + imageChunk.hashCode();
        }
        result = 31 * result + lineArts.size();
        for (LineArtChunk lineArtChunk : lineArts) {
            result = 31 * result + lineArtChunk.hashCode();
        }
        return result;
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

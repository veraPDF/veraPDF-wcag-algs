package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.Vertex;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class TableBorderBuilder {

    private final BoundingBox boundingBox;
    private final Set<Vertex> vertexes;
    private final TreeSet<LineChunk> horizontalLines;
    private final TreeSet<LineChunk> verticalLines;

    public TableBorderBuilder(LineChunk lineChunk) {
        vertexes = new HashSet<>();
        horizontalLines = new TreeSet<>(new LineChunk.HorizontalLineComparator());
        verticalLines = new TreeSet<>(new LineChunk.VerticalLineComparator());
        boundingBox = new BoundingBox(lineChunk.getBoundingBox());
        addLine(lineChunk);
    }

    public void addVertex(Vertex v) {
        vertexes.add(v);
    }

    public void addLine(LineChunk lineChunk) {
        if (lineChunk.isHorizontalLine()) {
            horizontalLines.add(lineChunk);
        } else {
            verticalLines.add(lineChunk);
        }
        boundingBox.union(lineChunk.getBoundingBox());
        addVertex(lineChunk.getStart());
        addVertex(lineChunk.getEnd());
    }

    public boolean isConnectedBorder(TableBorderBuilder border) {
        if (boundingBox.overlaps(border.boundingBox)) {
            return true;
        }
        return false;
    }

    public void mergeBorder(TableBorderBuilder border) {
        for (LineChunk horizontalLine : border.getHorizontalLines()) {
            for (LineChunk verticalLine : verticalLines) {
                Vertex v = LineChunk.getIntersectionVertex(horizontalLine, verticalLine);
                if (v != null) {
                    vertexes.add(v);
                }
            }
        }
        for (LineChunk horizontalLine : horizontalLines) {
            for (LineChunk verticalLine : border.getVerticalLines()) {
                Vertex v = LineChunk.getIntersectionVertex(horizontalLine, verticalLine);
                if (v != null) {
                    vertexes.add(v);
                }
            }
        }
        verticalLines.addAll(border.getVerticalLines());
        horizontalLines.addAll(border.getHorizontalLines());
        boundingBox.union(border.boundingBox);
        vertexes.addAll(border.getVertexes());
    }

    public int getVertexesNumber() {
        return vertexes.size();
    }

    public Set<LineChunk> getVerticalLines() {
        return verticalLines;
    }

    public Set<LineChunk> getHorizontalLines() {
        return horizontalLines;
    }

    public int getVerticalLinesNumber() {
        return verticalLines.size();
    }

    public int getHorizontalLinesNumber() {
        return horizontalLines.size();
    }

    public Set<Vertex> getVertexes() {
        return vertexes;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }
}

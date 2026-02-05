/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.Vertex;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.*;

public class TableBorderBuilder {

    private final BoundingBox boundingBox;
    private final Set<Vertex> vertexes;
    private final SortedSet<LineChunk> horizontalLines;
    private final SortedSet<LineChunk> verticalLines;
    private double maxVertexRadius = 0.0;

    public TableBorderBuilder(LineChunk lineChunk) {
        vertexes = new HashSet<>();
        horizontalLines = new TreeSet<>(new LineChunk.HorizontalLineComparator());
        verticalLines = new TreeSet<>(new LineChunk.VerticalLineComparator());
        boundingBox = new BoundingBox(lineChunk.getBoundingBox());
        addLine(lineChunk);
    }

    public void addVertex(Vertex v) {
        vertexes.add(v);
        if (maxVertexRadius < v.getRadius()) {
            maxVertexRadius = v.getRadius();
        }
    }

    public void addLine(LineChunk lineChunk) {
        if (lineChunk.isHorizontalLine()) {
            horizontalLines.add(lineChunk);
        } else if (lineChunk.isVerticalLine()){
            verticalLines.add(lineChunk);
        }
        boundingBox.union(lineChunk.getBoundingBox());
        addVertex(lineChunk.getStart());
        if (!lineChunk.isSquare()) {
            addVertex(lineChunk.getEnd());
        }
    }

    public boolean isConnectedBorder(TableBorderBuilder border) {
        if (!boundingBox.overlaps(border.boundingBox, maxVertexRadius + border.maxVertexRadius)) {
            return false;
        }
        for (LineChunk horizontalLine : getHorizontalLines()) {
            for (LineChunk verticalLine : border.getVerticalLines()) {
                if (LineChunk.haveIntersection(horizontalLine, verticalLine)) {
                    return true;
                }
            }
        }
        for (LineChunk verticalLine : getVerticalLines()) {
            for (LineChunk horizontalLine : border.getHorizontalLines()) {
                if (LineChunk.haveIntersection(horizontalLine, verticalLine)) {
                    return true;
                }
            }
        }
        for (Vertex vertex1 : vertexes) {
            for (Vertex vertex2 : border.vertexes) {
                if (Vertex.areCloseVertexes(vertex1, vertex2)) {
                    return true;
                }
            }
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

    public static class TableBorderBuildersComparator implements Comparator<TableBorderBuilder> {
        @Override
        public int compare(TableBorderBuilder builder1, TableBorderBuilder builder2){
            int res = Double.compare(builder2.getBoundingBox().getTopY(), builder1.getBoundingBox().getTopY());
            if (res != 0) {
                return res;
            }
            return Double.compare(builder1.getBoundingBox().getLeftX(), builder2.getBoundingBox().getLeftX());
        }
    }
}

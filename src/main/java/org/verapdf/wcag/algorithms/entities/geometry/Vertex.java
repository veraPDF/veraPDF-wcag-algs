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
package org.verapdf.wcag.algorithms.entities.geometry;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.Comparator;
import java.util.Objects;

public class Vertex extends InfoChunk {
    private final double x;
    private final double y;
    private final double radius;

    public Vertex(Integer pageNumber, double x, double y, double radius) {
        super(new BoundingBox(pageNumber, x - radius, y - radius, x + radius, y + radius));
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public Vertex(Integer pageNumber, double x, double y) {
        this(pageNumber, x, y, 0.0);
    }

    public Vertex(double x, double y) {
        this(null, x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    public static boolean areCloseVertexes(Vertex v1, Vertex v2) {
        return areCloseVertexes(v1, v2, NodeUtils.VERTEX_FACTOR * (v1.radius + v2.radius));
    }

    public static boolean areCloseVertexes(Vertex v1, Vertex v2, double epsilon) {
        return NodeUtils.areCloseNumbers(v1.x, v2.x, epsilon) && NodeUtils.areCloseNumbers(v1.y, v2.y, epsilon);
    }

    public static class VertexComparatorX implements Comparator<Vertex> {
        @Override
        public int compare(Vertex vertex1, Vertex vertex2){
            return Double.compare(vertex1.getLeftX(), vertex2.getLeftX());
        }
    }

    public static class VertexComparatorY implements Comparator<Vertex> {
        @Override
        public int compare(Vertex vertex1, Vertex vertex2){
            return -Double.compare(vertex1.getTopY(), vertex2.getTopY());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        Vertex that = (Vertex) o;
        return Double.compare(that.x, x) == 0
                && Double.compare(that.y, y) == 0
                && Double.compare(that.radius, radius) == 0;
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "x=" + x +
                ", y=" + y +
                ", radius=" + radius +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, radius);
    }
}

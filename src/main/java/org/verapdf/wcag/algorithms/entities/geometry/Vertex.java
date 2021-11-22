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
        return areCloseVertexes(v1, v2, 0.5 * (v1.radius + v2.radius));
    }

    public static boolean areCloseVertexes(Vertex v1, Vertex v2, double epsilon) {
        return NodeUtils.areCloseNumbers(v1.x, v2.x, epsilon) && NodeUtils.areCloseNumbers(v1.y, v2.y, epsilon);
    }

    public static class VertexComparatorX implements Comparator<Vertex> {
        public int compare(Vertex vertex1, Vertex vertex2){
            return Double.compare(vertex1.getLeftX(), vertex2.getLeftX());
        }
    }

    public static class VertexComparatorY implements Comparator<Vertex> {
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

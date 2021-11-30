package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.Vertex;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TableBorder {
    private final List<Double> xCoordinates;
    private final List<Double> xWidths;
    private final List<Double> yCoordinates;
    private final List<Double> yWidths;
    private final TableCell[][] matrix;
    private final BoundingBox boundingBox;
    private final int n;
    private final int m;

    public TableBorder(TableBorderBuilder builder) {
        xCoordinates = new LinkedList<>();
        xWidths = new LinkedList<>();
        calculateXCoordinates(builder);
        yCoordinates = new LinkedList<>();
        yWidths = new LinkedList<>();
        calculateYCoordinates(builder);
        boundingBox = new BoundingBox(builder.getBoundingBox());
        n = yCoordinates.size() - 1;
        m = xCoordinates.size() - 1;
        matrix = new TableCell[n][m];
        createMatrix(builder);
    }

    private void calculateXCoordinates(TableBorderBuilder builder) {
        List<Vertex> vertexes = builder.getVertexes().stream().sorted(new Vertex.VertexComparatorX()).collect(Collectors.toList());
        double x1 = vertexes.get(0).getLeftX();
        double x2 = vertexes.get(0).getRightX();
        for (Vertex v : vertexes) {
            if (x2 < v.getLeftX() - NodeUtils.TABLE_BORDER_EPSILON) {
                xCoordinates.add(0.5 * (x1 + x2));
                xWidths.add(x2 - x1);
                x1 = v.getLeftX();
                x2 = v.getRightX();
            } else if (x2 < v.getRightX()) {
                x2 = v.getRightX();
            }
        }
        xCoordinates.add(0.5 * (x1 + x2));
        xWidths.add(x2 - x1);
    }

    private void calculateYCoordinates(TableBorderBuilder builder) {
        List<Vertex> vertexes = builder.getVertexes().stream().sorted(new Vertex.VertexComparatorY()).collect(Collectors.toList());
        double y1 = vertexes.get(0).getTopY();
        double y2 = vertexes.get(0).getBottomY();
        for (Vertex v : vertexes) {
            if (y2 > v.getTopY() + NodeUtils.TABLE_BORDER_EPSILON) {
                yCoordinates.add(0.5 * (y1 + y2));
                yWidths.add(y1 - y2);
                y1 = v.getTopY();
                y2 = v.getBottomY();
            } else if (y2 > v.getBottomY()) {
                y2 = v.getBottomY();
            }
        }
        yCoordinates.add(0.5 * (y1 + y2));
        yWidths.add(y1 - y2);
    }

    private void createMatrix(TableBorderBuilder builder) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix[i][j] = new TableCell(i, j, n - i, m - j);
            }
        }
        for (LineChunk line : builder.getHorizontalLines()) {
            int i = getCoordinateY(line.getCenterY());
            int j1 = getCoordinateX(line.getLeftX());
            int j2 = getCoordinateX(line.getRightX());
            if (i > 0 && j1 != -1 && j2 != -1) {
                for (int j = j1; j < j2; j++) {
                    matrix[i - 1][j].rowSpan = 1;
                }
            }
        }
        for (LineChunk line : builder.getVerticalLines()) {
            int j = getCoordinateX(line.getCenterX());
            int i1 = getCoordinateY(line.getTopY());
            int i2 = getCoordinateY(line.getBottomY());
            if (j > 0 && i1 != -1 && i2 != -1) {
                for (int i = i1; i < i2; i++) {
                    matrix[i][j - 1].colSpan = 1;
                }
            }
        }
        for (int i = n - 2; i >= 0; i--) {
            for (int j = m - 2; j >= 0; j--) {
                if (matrix[i][j].colSpan != 1) {
                    matrix[i][j].colSpan = matrix[i][j + 1].colSpan + 1;
                }
                if (matrix[i][j].rowSpan != 1) {
                    matrix[i][j].rowSpan = matrix[i + 1][j].rowSpan + 1;
                }
            }
        }
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 2; j >= 0; j--) {
                if (matrix[i][j].colSpan > 1) {
                    matrix[i][j + 1] = null;
                }
            }
        }
        for (int i = n - 2; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                if (matrix[i][j] != null && matrix[i][j].rowSpan > 1) {
                    matrix[i + 1][j] = null;
                }
            }
        }
    }

    public Integer getPageNumber() {
        return boundingBox.getPageNumber();
    }

    private int getCoordinateX(double x) {
        for (int i = 0; i < xCoordinates.size(); i++) {
            if (x <= xCoordinates.get(i) + 0.5 * xWidths.get(i) && x >= xCoordinates.get(i) - 0.5 * xWidths.get(i)) {
                return i;
            }
        }
        return -1;
    }

    private int getCoordinateY(double y) {
        for (int i = 0; i < yCoordinates.size(); i++) {
            if (y <= yCoordinates.get(i) + 0.5 * yWidths.get(i) && y >= yCoordinates.get(i) - 0.5 * yWidths.get(i)) {
                return i;
            }
        }
        return -1;
    }

    public int getN() {
        return n;
    }

    public int getM() {
        return m;
    }

    public boolean isBadTable() {
        return n <= 1 ||  m <= 1 || (n == 2 && m == 2);
    }

    public TableCell[][] getMatrix() {
        return matrix;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public class TableCell {
        public int i;
        public int j;
        public int rowSpan;
        public int colSpan;

        TableCell(int i, int j, int rowSpan, int colSpan) {
            this.i = i;
            this.j = j;
            this.rowSpan = rowSpan;
            this.colSpan = colSpan;
        }

        public double getTopY() {
            return yCoordinates.get(i);
        }

        public double getBottomY() {
            return yCoordinates.get(i + rowSpan);
        }

        public double getLeftX() {
            return xCoordinates.get(j);
        }

        public double getRightX() {
            return xCoordinates.get(j + colSpan);
        }

        public double getWidth() {
            return getRightX() - getLeftX();
        }

        public double getHeight() {
            return getTopY() - getBottomY();
        }
    }

    public static class TableBordersComparator implements Comparator<TableBorder> {
        public int compare(TableBorder border1, TableBorder border2){
            int res = Double.compare(border2.getBoundingBox().getTopY(), border1.getBoundingBox().getTopY());
            if (res != 0) {
                return res;
            }
            return Double.compare(border1.getBoundingBox().getLeftX(), border2.getBoundingBox().getLeftX());
        }
    }
}

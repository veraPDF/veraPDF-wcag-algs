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

    public static final double TABLE_BORDER_EPSILON = 0.55;

    private final List<Double> xCoordinates;
    private final List<Double> xWidths;
    private final List<Double> yCoordinates;
    private final List<Double> yWidths;
    private final TableBorderRow[] rows;
    private final BoundingBox boundingBox;
    private final int numberOfRows;
    private final int numberOfColumns;

    public TableBorder(TableBorderBuilder builder) {
        xCoordinates = new LinkedList<>();
        xWidths = new LinkedList<>();
        calculateXCoordinates(builder);
        yCoordinates = new LinkedList<>();
        yWidths = new LinkedList<>();
        calculateYCoordinates(builder);
        boundingBox = new BoundingBox(builder.getBoundingBox());
        numberOfRows = yCoordinates.size() - 1;
        numberOfColumns = xCoordinates.size() - 1;
        rows = new TableBorderRow[numberOfRows];
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

    public TableBorderRow[] getRows() {
        return rows;
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
        for (int i = 0; i < numberOfRows; i++) {
            rows[i] = new TableBorderRow(i);
            for (int j = 0; j < numberOfColumns; j++) {
                rows[i].cells[j] = new TableBorderCell(i, j, numberOfRows - i, numberOfColumns - j);
            }
        }
        for (LineChunk line : builder.getHorizontalLines()) {
            int i = getCoordinateY(line.getCenterY());
            int j1 = getCoordinateX(line.getLeftX());
            int j2 = getCoordinateX(line.getRightX());
            if (i > 0 && j1 != -1 && j2 != -1) {
                for (int j = j1; j < j2; j++) {
                    rows[i - 1].cells[j].rowSpan = 1;
                }
            }
        }
        for (LineChunk line : builder.getVerticalLines()) {
            int j = getCoordinateX(line.getCenterX());
            int i1 = getCoordinateY(line.getTopY());
            int i2 = getCoordinateY(line.getBottomY());
            if (j > 0 && i1 != -1 && i2 != -1) {
                for (int i = i1; i < i2; i++) {
                    rows[i].cells[j - 1].colSpan = 1;
                }
            }
        }
        for (int rowNumber = numberOfRows - 2; rowNumber >= 0; rowNumber--) {
            for (int colNumber = numberOfColumns - 2; colNumber >= 0; colNumber--) {
                if (rows[rowNumber].cells[colNumber].colSpan != 1) {
                    rows[rowNumber].cells[colNumber].colSpan = rows[rowNumber].cells[colNumber + 1].colSpan + 1;
                }
                if (rows[rowNumber].cells[colNumber].rowSpan != 1) {
                    rows[rowNumber].cells[colNumber].rowSpan = rows[rowNumber + 1].cells[colNumber].rowSpan + 1;
                }
            }
        }
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            for (int colNumber = 0; colNumber < numberOfColumns; colNumber++) {
                if (rows[rowNumber].cells[colNumber].colNumber + rows[rowNumber].cells[colNumber].colSpan > colNumber + 1) {
                    rows[rowNumber].cells[colNumber + 1] = rows[rowNumber].cells[colNumber];
                }
                if (rows[rowNumber].cells[colNumber].rowNumber + rows[rowNumber].cells[colNumber].rowSpan > rowNumber + 1) {
                    rows[rowNumber + 1].cells[colNumber] = rows[rowNumber].cells[colNumber];
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

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public boolean isBadTable() {
        return numberOfRows <= 1 ||  numberOfColumns <= 1 || (numberOfRows == 2 && numberOfColumns == 2);
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public class TableBorderRow {
        public int rowNumber;
        public TableBorderCell[] cells;

        TableBorderRow(int rowNumber) {
            this.rowNumber = rowNumber;
            cells = new TableBorderCell[numberOfColumns];
        }

        public double getTopY() {
            return yCoordinates.get(rowNumber) + 0.5 * yWidths.get(rowNumber);
        }

        public double getBottomY() {
            return yCoordinates.get(rowNumber + 1) - 0.5 * yWidths.get(rowNumber + 1);
        }

        public double getLeftX() {
            return boundingBox.getLeftX();
        }

        public double getRightX() {
            return boundingBox.getRightX();
        }

        public double getWidth() {
            return boundingBox.getWidth();
        }

        public double getHeight() {
            return boundingBox.getTopY() - getBottomY();
        }

    }

    public class TableBorderCell {
        public int rowNumber;
        public int colNumber;
        public int rowSpan;
        public int colSpan;

        TableBorderCell(int rowNumber, int colNumber, int rowSpan, int colSpan) {
            this.rowNumber = rowNumber;
            this.colNumber = colNumber;
            this.rowSpan = rowSpan;
            this.colSpan = colSpan;
        }

        public double getTopY() {
            return yCoordinates.get(rowNumber) + 0.5 * yWidths.get(rowNumber);
        }

        public double getBottomY() {
            return yCoordinates.get(rowNumber + rowSpan) - 0.5 * yWidths.get(rowNumber + rowSpan);
        }

        public double getLeftX() {
            return xCoordinates.get(colNumber) - 0.5 * xWidths.get(colNumber);
        }

        public double getRightX() {
            return xCoordinates.get(colNumber + colSpan) + 0.5 * xWidths.get(colNumber + colSpan);
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

package org.verapdf.wcag.algorithms.entities.tables.tableBorders;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.Vertex;
import org.verapdf.wcag.algorithms.entities.tables.Table;
import org.verapdf.wcag.algorithms.entities.tables.TableBorderBuilder;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TableBorder {
    public static final double TABLE_BORDER_EPSILON = 0.6;

    private final List<Double> xCoordinates;
    private final List<Double> xWidths;
    private final List<Double> yCoordinates;
    private final List<Double> yWidths;
    private TableBorderRow[] rows;
    private final BoundingBox boundingBox;
    private int numberOfRows;
    private int numberOfColumns;
    private final Long id;
    private INode node;
    private boolean isBadTable = false;

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
        if (numberOfColumns > 0 && numberOfRows > 0) {
            createMatrix(builder);
        }
        id = Table.getNextTableListId();
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

    public TableBorderRow getRow(int rowNumber) {
        return rows[rowNumber];
    }

    public int getNumberOfRowsWithContent() {
        int numberOfRowsWithContent = 0;
        for (TableBorderRow row : rows) {
            if (row.getNumberOfCellWithContent() > 0) {
                numberOfRowsWithContent++;
            }
        }
        return numberOfRowsWithContent;
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
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            rows[rowNumber] = new TableBorderRow(rowNumber, numberOfColumns,
                    new BoundingBox(boundingBox.getPageNumber(), boundingBox.getLeftX(),
                            yCoordinates.get(rowNumber + 1) - 0.5 * yWidths.get(rowNumber + 1),
                            boundingBox.getRightX(), yCoordinates.get(rowNumber) + 0.5 * yWidths.get(rowNumber)));
            for (int colNumber = 0; colNumber < numberOfColumns; colNumber++) {
                rows[rowNumber].cells[colNumber] = new TableBorderCell(rowNumber, colNumber,
                        numberOfRows - rowNumber, numberOfColumns - colNumber);
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
            if (rows[rowNumber].cells[numberOfColumns - 1].rowSpan != 1) {
                rows[rowNumber].cells[numberOfColumns - 1].rowSpan = rows[rowNumber + 1].cells[numberOfColumns - 1].rowSpan + 1;
            }
        }
        for (int colNumber = numberOfColumns - 2; colNumber >= 0; colNumber--) {
            if (rows[numberOfRows - 1].cells[colNumber].colSpan != 1) {
                rows[numberOfRows - 1].cells[colNumber].colSpan = rows[numberOfRows - 1].cells[colNumber + 1].colSpan + 1;
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
                    if (rows[rowNumber].cells[colNumber + 1].rowNumber + rows[rowNumber].cells[colNumber + 1].rowSpan ==
                            rows[rowNumber].cells[colNumber].rowNumber + rows[rowNumber].cells[colNumber].rowSpan) {
                        rows[rowNumber].cells[colNumber + 1] = rows[rowNumber].cells[colNumber];
                    } else {
                        isBadTable = true;
                        return;
                    }
                }
                if (rows[rowNumber].cells[colNumber].rowNumber + rows[rowNumber].cells[colNumber].rowSpan > rowNumber + 1) {
                    if (rows[rowNumber + 1].cells[colNumber].colNumber + rows[rowNumber + 1].cells[colNumber].colSpan ==
                            rows[rowNumber].cells[colNumber].colNumber + rows[rowNumber].cells[colNumber].colSpan) {
                        rows[rowNumber + 1].cells[colNumber] = rows[rowNumber].cells[colNumber];
                    } else {
                        isBadTable = true;
                        return;
                    }
                }
            }
        }
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            for (int colNumber = 0; colNumber < numberOfColumns; colNumber++) {
                if (rows[rowNumber].cells[colNumber].colNumber == colNumber &&
                        rows[rowNumber].cells[colNumber].rowNumber == rowNumber) {
                    TableBorderCell cell = rows[rowNumber].cells[colNumber];
                    cell.setBoundingBox(new BoundingBox(rows[rowNumber].getPageNumber(),
                            xCoordinates.get(colNumber) - 0.5 * xWidths.get(colNumber),
                            yCoordinates.get(rowNumber + cell.rowSpan) - 0.5 * yWidths.get(rowNumber + cell.rowSpan),
                            xCoordinates.get(colNumber + cell.colSpan) + 0.5 * xWidths.get(colNumber + cell.colSpan),
                            yCoordinates.get(rowNumber) + 0.5 * yWidths.get(rowNumber)));
                }
            }
        }
    }

    public Integer getPageNumber() {
        return boundingBox.getPageNumber();
    }

    private int getCoordinateX(double x) {
        for (int i = 0; i < xCoordinates.size(); i++) {
            if (x <= xCoordinates.get(i) + 0.5 * xWidths.get(i) + NodeUtils.EPSILON &&
                    x >= xCoordinates.get(i) - 0.5 * xWidths.get(i) - NodeUtils.EPSILON) {
                return i;
            }
        }
        return -1;
    }

    private int getCoordinateY(double y) {
        for (int i = 0; i < yCoordinates.size(); i++) {
            if (y <= yCoordinates.get(i) + 0.5 * yWidths.get(i) + NodeUtils.EPSILON
                    && y >= yCoordinates.get(i) - 0.5 * yWidths.get(i) - NodeUtils.EPSILON) {
                return i;
            }
        }
        return -1;
    }

    private int getClosestLeftX(double x) {
        for (int i = xCoordinates.size() - 1; i >= 0; i--) {
            if (x >= xCoordinates.get(i) - 0.5 * xWidths.get(i) - TABLE_BORDER_EPSILON) {
                return i;
            }
        }
        return -1;
    }

    private int getClosestTopY(double y) {
        for (int i = yCoordinates.size() - 1; i >= 0; i--) {
            if (y <= yCoordinates.get(i) + 0.5 * yWidths.get(i) + TABLE_BORDER_EPSILON) {
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

    public Long getId() {
        return id;
    }

    public INode getNode() {
        return node;
    }

    public void setNode(INode node) {
        this.node = node;
    }

    public boolean isBadTable() {
        return isBadTable || numberOfRows <= 0 ||  numberOfColumns <= 0 || (numberOfRows == 1 && numberOfColumns == 1);
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public static class TableBordersComparator implements Comparator<TableBorder> {
        public int compare(TableBorder border1, TableBorder border2) {
            int res = Double.compare(border2.getBoundingBox().getTopY(), border1.getBoundingBox().getTopY());
            if (res != 0) {
                return res;
            }
            return Double.compare(border1.getBoundingBox().getLeftX(), border2.getBoundingBox().getLeftX());
        }
    }

    public TableBorderCell getTableBorderCell(BoundingBox box) {
        int xIndex = getClosestLeftX(box.getLeftX());
        int yIndex = getClosestTopY(box.getTopY());
        if (xIndex < 0 || yIndex < 0) {
            return null;
        }
        TableBorderCell cell = rows[yIndex].cells[xIndex];
        if (cell.getRightX() + TABLE_BORDER_EPSILON > box.getRightX() &&
                cell.getBottomY() - TABLE_BORDER_EPSILON < box.getBottomY()) {
            return cell;
        }
        return null;
    }
}

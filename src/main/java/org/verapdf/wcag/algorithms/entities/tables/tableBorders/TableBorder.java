package org.verapdf.wcag.algorithms.entities.tables.tableBorders;

import org.verapdf.wcag.algorithms.entities.BaseObject;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.Vertex;
import org.verapdf.wcag.algorithms.entities.tables.TableBorderBuilder;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TableBorder extends BaseObject {
    public static final double TABLE_BORDER_EPSILON = 0.6;
    private static final double MIN_CELL_CONTENT_INTERSECTION_PERCENT = 0.8;

    private final List<Double> xCoordinates;
    private final List<Double> xWidths;
    private final List<Double> yCoordinates;
    private final List<Double> yWidths;
    private TableBorderRow[] rows;
    private int numberOfRows;
    private int numberOfColumns;
    private INode node;
    private boolean isBadTable = false;

    public TableBorder(TableBorderBuilder builder) {
        super(new BoundingBox(builder.getBoundingBox()));
        xCoordinates = new LinkedList<>();
        xWidths = new LinkedList<>();
        calculateXCoordinates(builder);
        yCoordinates = new LinkedList<>();
        yWidths = new LinkedList<>();
        calculateYCoordinates(builder);
        createMatrix(builder);
        setRecognizedStructureId(StaticContainers.getNextID());
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
        int numberOfRows = this.yCoordinates.size() - 1;
        int numberOfColumns = this.xCoordinates.size() - 1;
        if (numberOfColumns < 1 || numberOfRows < 1) {
            return;
        }
        TableBorderRow[] rows = new TableBorderRow[numberOfRows];
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            rows[rowNumber] = new TableBorderRow(rowNumber, numberOfColumns, getRecognizedStructureId());
            for (int colNumber = 0; colNumber < numberOfColumns; colNumber++) {
                rows[rowNumber].cells[colNumber] = new TableBorderCell(rowNumber, colNumber,
                        numberOfRows - rowNumber, numberOfColumns - colNumber, getRecognizedStructureId());
            }
        }
        if (processHorizontalLines(rows, numberOfRows, numberOfColumns, builder) ||
                processVerticalLines(rows, numberOfRows, numberOfColumns, builder)) {
            return;
        }
        if (processMergedCells(rows, numberOfRows, numberOfColumns)) {
            return;
        }
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            BoundingBox multiBoundingBox = new MultiBoundingBox();
            for (int colNumber = 0; colNumber < numberOfColumns; colNumber++) {
                if (rows[rowNumber].cells[colNumber].colNumber == colNumber &&
                        rows[rowNumber].cells[colNumber].rowNumber == rowNumber) {
                    TableBorderCell cell = rows[rowNumber].cells[colNumber];
                    BoundingBox cellBoundingBox = new BoundingBox(getBoundingBox().getPageNumber(),
                            xCoordinates.get(colNumber) - 0.5 * xWidths.get(colNumber),
                            yCoordinates.get(rowNumber + cell.rowSpan) - 0.5 * yWidths.get(rowNumber + cell.rowSpan),
                            xCoordinates.get(colNumber + cell.colSpan) + 0.5 * xWidths.get(colNumber + cell.colSpan),
                            yCoordinates.get(rowNumber) + 0.5 * yWidths.get(rowNumber));
                    cell.setBoundingBox(cellBoundingBox);
                    multiBoundingBox.union(cellBoundingBox);
                }
            }
            rows[rowNumber].setBoundingBox(multiBoundingBox);
        }
        List<Integer> redundantRows = new ArrayList<>(numberOfRows);
        List<Integer> usefulRows = new ArrayList<>(numberOfRows);
        detectRedundantRows(redundantRows, usefulRows, rows, numberOfRows, numberOfColumns);
        List<Integer> redundantColumns = new ArrayList<>(numberOfColumns);
        List<Integer> usefulColumns = new ArrayList<>(numberOfColumns);
        detectRedundantColumns(redundantColumns, usefulColumns, rows, numberOfRows, numberOfColumns);
        if (redundantColumns.isEmpty() && redundantRows.isEmpty()) {
			this.rows = rows;
			this.numberOfRows = numberOfRows;
			this.numberOfColumns = numberOfColumns;
			return;
        }
        deleteRedundantRowsAndColumns(rows, numberOfRows, numberOfColumns, redundantRows, usefulRows, redundantColumns, usefulColumns);
    }

    private boolean processHorizontalLines(TableBorderRow[] rows, int numberOfRows, int numberOfColumns,
                                        TableBorderBuilder builder) {
        boolean[] hasTopBorder = new boolean[numberOfColumns];
        boolean[] hasBottomBorder = new boolean[numberOfColumns];
        for (LineChunk line : builder.getHorizontalLines()) {
            int rowNumber = getCoordinateY(line.getCenterY());
            int firstColNumber = getCoordinateX(line.getLeftX());
            int lastColNumber = getCoordinateX(line.getRightX());
            if (rowNumber != -1 && firstColNumber != -1 && lastColNumber != -1) {
                if (rowNumber > 0 && rowNumber < numberOfRows) {
                    for (int colNumber = firstColNumber; colNumber < lastColNumber; colNumber++) {
                        rows[rowNumber - 1].cells[colNumber].rowSpan = 1;
                    }
                } else if (rowNumber == 0) {
                    for (int colNumber = firstColNumber; colNumber < lastColNumber; colNumber++) {
                        hasTopBorder[colNumber] = true;
                    }
                } else if (rowNumber == numberOfRows) {
                    for (int colNumber = firstColNumber; colNumber < lastColNumber; colNumber++) {
                        hasBottomBorder[colNumber] = true;
                    }
                }
            }
        }
        for (int i = 0; i < hasBottomBorder.length; i++) {
            if (!hasBottomBorder[i] || !hasTopBorder[i]) {
                isBadTable = true;
                break;
            }
        }
        return isBadTable;
    }

    private boolean processVerticalLines(TableBorderRow[] rows, int numberOfRows, int numberOfColumns,
                                        TableBorderBuilder builder) {
        boolean[] hasLeftBorder = new boolean[numberOfRows];
        boolean[] hasRightBorder = new boolean[numberOfRows];
        for (LineChunk line : builder.getVerticalLines()) {
            int colNumber = getCoordinateX(line.getCenterX());
            int firstRowNumber = getCoordinateY(line.getTopY());
            int lastRowNumber = getCoordinateY(line.getBottomY());
            if (firstRowNumber != -1 && lastRowNumber != -1 && colNumber != -1) {
                if (colNumber > 0 && colNumber < numberOfColumns) {
                    for (int rowNumber = firstRowNumber; rowNumber < lastRowNumber; rowNumber++) {
                        rows[rowNumber].cells[colNumber - 1].colSpan = 1;
                    }
                } else if (colNumber == 0) {
                    for (int rowNumber = firstRowNumber; rowNumber < lastRowNumber; rowNumber++) {
                        hasLeftBorder[rowNumber] = true;
                    }
                } else if (colNumber == numberOfColumns) {
                    for (int rowNumber = firstRowNumber; rowNumber < lastRowNumber; rowNumber++) {
                        hasRightBorder[rowNumber] = true;
                    }
                }
            }
        }
        for (int i = 0; i < hasRightBorder.length; i++) {
            if (!hasRightBorder[i] || !hasLeftBorder[i]) {
                isBadTable = true;
                break;
            }
        }
        return isBadTable;
    }

    private boolean processMergedCells(TableBorderRow[] rows, int numberOfRows, int numberOfColumns) {
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
                        return true;
                    }
                }
                if (rows[rowNumber].cells[colNumber].rowNumber + rows[rowNumber].cells[colNumber].rowSpan > rowNumber + 1) {
                    if (rows[rowNumber + 1].cells[colNumber].colNumber + rows[rowNumber + 1].cells[colNumber].colSpan ==
                            rows[rowNumber].cells[colNumber].colNumber + rows[rowNumber].cells[colNumber].colSpan) {
                        rows[rowNumber + 1].cells[colNumber] = rows[rowNumber].cells[colNumber];
                    } else {
                        isBadTable = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void detectRedundantRows(List<Integer> redundantRows, List<Integer> usefulRows,
                                     TableBorderRow[] rows, int numberOfRows, int numberOfColumns) {
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            boolean redundantRow = true;
            for (int colNumber = 0; colNumber < numberOfColumns; colNumber++) {
                if (rows[rowNumber].cells[colNumber].rowNumber == rowNumber) {
                    redundantRow = false;
                    break;
                }
            }
            if (redundantRow) {
                redundantRows.add(rowNumber);
            } else {
                usefulRows.add(rowNumber);
            }
        }
    }

    private void detectRedundantColumns(List<Integer> redundantColumns, List<Integer> usefulColumns,
                                        TableBorderRow[] rows, int numberOfRows, int numberOfColumns) {
        for (int colNumber = 0; colNumber < numberOfColumns; colNumber++) {
            boolean redundantColumn = true;
            for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
                if (rows[rowNumber].cells[colNumber].colNumber == colNumber) {
                    redundantColumn = false;
                    break;
                }
            }
            if (redundantColumn) {
                redundantColumns.add(colNumber);
            } else {
                usefulColumns.add(colNumber);
            }
        }
    }

    private void deleteRedundantRowsAndColumns(TableBorderRow[] rows, int numberOfRows, int numberOfColumns,
                                               List<Integer> redundantRows, List<Integer> usefulRows,
                                               List<Integer> redundantColumns, List<Integer> usefulColumns) {
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            for (Integer columnNumber : redundantColumns) {
                if (rows[rowNumber].cells[columnNumber].rowNumber == rowNumber) {
                    rows[rowNumber].cells[columnNumber].colSpan--;
                }
            }
        }
        for (Integer rowNumber : redundantRows) {
            for (int colNumber = 0; colNumber < numberOfColumns; colNumber++) {
                if (rows[rowNumber].cells[colNumber].colNumber == colNumber) {
                    rows[rowNumber].cells[colNumber].rowSpan--;
                }
            }
        }
        this.numberOfRows = usefulRows.size();
        this.numberOfColumns = usefulColumns.size();
        for (int colNumber = redundantColumns.size() - 1; colNumber >= 0; colNumber--) {
            int oldColNumber = redundantColumns.get(colNumber);
            xCoordinates.remove(oldColNumber);
            xWidths.remove(oldColNumber);
        }
        for (int rowNumber = redundantRows.size() - 1; rowNumber >= 0; rowNumber--) {
            int oldRowNumber = redundantRows.get(rowNumber);
            yCoordinates.remove(oldRowNumber);
            yWidths.remove(oldRowNumber);
        }
        this.rows = new TableBorderRow[this.numberOfRows];
        for (int rowNumber = 0; rowNumber < this.numberOfRows; rowNumber++) {
            int oldRowNumber = usefulRows.get(rowNumber);
            this.rows[rowNumber] = new TableBorderRow(rowNumber, this.numberOfColumns, getRecognizedStructureId());
            this.rows[rowNumber].setBoundingBox(rows[oldRowNumber].getBoundingBox());
            for (int colNumber = 0; colNumber < this.numberOfColumns; colNumber++) {
                int oldColNumber = usefulColumns.get(colNumber);
                if (rows[oldRowNumber].cells[oldColNumber].rowNumber == oldRowNumber &&
                        rows[oldRowNumber].cells[oldColNumber].colNumber == oldColNumber) {
                    rows[oldRowNumber].cells[oldColNumber].rowNumber = rowNumber;
                    rows[oldRowNumber].cells[oldColNumber].colNumber = colNumber;
                }
                this.rows[rowNumber].cells[colNumber] = rows[oldRowNumber].cells[oldColNumber];
            }
        }
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

    private int getClosestRightX(double x) {
        for (int i = 0; i < xCoordinates.size(); i++) {
            if (x <= xCoordinates.get(i) + 0.5 * xWidths.get(i) + TABLE_BORDER_EPSILON) {
                return i;
            }
        }
        return xCoordinates.size();
    }

    private int getClosestTopY(double y) {
        for (int i = yCoordinates.size() - 1; i >= 0; i--) {
            if (y <= yCoordinates.get(i) + 0.5 * yWidths.get(i) + TABLE_BORDER_EPSILON) {
                return i;
            }
        }
        return -1;
    }

    private int getClosestBottomY(double y) {
        for (int i = 0; i < yCoordinates.size(); i++) {
            if (y >= yCoordinates.get(i) - 0.5 * yWidths.get(i) - TABLE_BORDER_EPSILON) {
                return i;
            }
        }
        return yCoordinates.size();
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public INode getNode() {
        return node;
    }

    public void setNode(INode node) {
        this.node = node;
    }

    public boolean isBadTable() {
        return isBadTable || numberOfRows < 1 || numberOfColumns < 1 || (numberOfRows == 1 && numberOfColumns == 1);
    }

    public static class TableBordersComparator implements Comparator<TableBorder> {
        @Override
        public int compare(TableBorder border1, TableBorder border2) {
            int res = Double.compare(border2.getBoundingBox().getTopY(), border1.getBoundingBox().getTopY());
            if (res != 0) {
                return res;
            }
            return Double.compare(border1.getBoundingBox().getLeftX(), border2.getBoundingBox().getLeftX());
        }
    }

    public TableBorderCell getTableBorderCell(BoundingBox box) {
        int xLeftIndex = getClosestLeftX(box.getLeftX());
        int xRightIndex = getClosestRightX(box.getRightX());
        int yTopIndex = getClosestTopY(box.getTopY());
        int yBottomIndex = getClosestBottomY(box.getBottomY());
        if (xLeftIndex == xCoordinates.size() - 1 || yTopIndex == yCoordinates.size() - 1 ||
                xRightIndex == 0 || yBottomIndex == 0) {
            return null;
        }
        if (xLeftIndex < 0) {
            xLeftIndex = 0;
        }
        if (yTopIndex < 0) {
            yTopIndex = 0;
        }
        if (xRightIndex == xCoordinates.size()) {
            xRightIndex--;
        }
        if (yBottomIndex == yCoordinates.size()) {
            yBottomIndex--;
        }
        if (xLeftIndex >= xRightIndex || yTopIndex >= yBottomIndex) {
            return null;
        }
        for (int xIndex = xLeftIndex; xIndex < xRightIndex; xIndex++) {
            for (int yIndex = yTopIndex; yIndex < yBottomIndex; yIndex++) {
                TableBorderCell cell = rows[yIndex].cells[xIndex];
                if (getIntersectionPercent(cell, box) > MIN_CELL_CONTENT_INTERSECTION_PERCENT) {
                    return cell;
                }
            }
        }
        return null;
    }

    private static double getIntersectionPercent(TableBorderCell cell, BoundingBox boundingBox) {
        double xIntersection = Math.min(Math.min(cell.getWidth(), boundingBox.getWidth()),
                Math.min(cell.getRightX() - boundingBox.getLeftX(), boundingBox.getRightX() - cell.getLeftX()));
        double yIntersection = Math.min(Math.min(cell.getHeight(), boundingBox.getHeight()),
                Math.min(cell.getTopY() - boundingBox.getBottomY(), boundingBox.getTopY() - cell.getBottomY()));
        if (xIntersection <= 0.0 || yIntersection <= 0.0) {
            return 0.0;
        }
        return (xIntersection / boundingBox.getWidth()) * (yIntersection / boundingBox.getHeight());
    }
}

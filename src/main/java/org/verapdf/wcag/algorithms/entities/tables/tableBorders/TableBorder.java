package org.verapdf.wcag.algorithms.entities.tables.tableBorders;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.Vertex;
import org.verapdf.wcag.algorithms.entities.tables.TableBorderBuilder;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.TableChecker;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.*;
import java.util.stream.Collectors;

public class TableBorder extends BaseObject {
    public static final double TABLE_BORDER_EPSILON = 0.6;
    public static final double MIN_CELL_CONTENT_INTERSECTION_PERCENT = 0.6;

    private final List<Double> xCoordinates = new LinkedList<>();
    private final List<Double> xWidths = new LinkedList<>();
    private final List<Double> yCoordinates = new LinkedList<>();
    private final List<Double> yWidths = new LinkedList<>();
    private TableBorderRow[] rows;
    private int numberOfRows;
    private int numberOfColumns;
    private INode node;
    private boolean isBadTable = false;
    private TableBorder previousTable;
    private boolean isTableTransformer = false;
    private TableBorder nextTable;

    public TableBorder(TableBorderBuilder builder) {
        super(new BoundingBox(builder.getBoundingBox()));
        calculateXCoordinates(builder);
        calculateYCoordinates(builder);
        createMatrix(builder);
        setRecognizedStructureId(StaticContainers.getNextID());
    }

    public TableBorder(int numberOfRows, int numberOfColumns) {
        super(new BoundingBox());
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        this.rows = new TableBorderRow[numberOfRows];
    }

    public TableBorder(BoundingBox boundingBox,
                       TableBorderRow[] rows,
                       int numberOfRows,
                       int numberOfColumns) {
        super(boundingBox);
        this.rows = rows;
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        calculateCoordinatesUsingBoundingBoxesOfRowsAndColumns();
        isTableTransformer = true;
        setRecognizedStructureId(StaticContainers.getNextID());
    }

    private void calculateXCoordinates(TableBorderBuilder builder) {
        List<Vertex> vertexes = builder.getVertexes().stream().sorted(new Vertex.VertexComparatorX()).collect(Collectors.toList());
        double x1 = vertexes.get(0).getLeftX();
        double x2 = vertexes.get(0).getRightX();
        for (Vertex v : vertexes) {
            if (x2 < v.getLeftX() - NodeUtils.VERTEX_TABLE_FACTOR * v.getRadius()) {
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

    public TableBorder(INode tableNode) {
        super(new BoundingBox());
        List<INode> tableRows = TableChecker.getTableRows(tableNode);
        this.numberOfRows = tableRows.size();
        if (numberOfRows == 0) {
            return;
        }
        this.numberOfColumns = TableChecker.getNumberOfColumns(tableRows.get(0));
        rows = new TableBorderRow[numberOfRows];
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            rows[rowNumber] = new TableBorderRow(rowNumber, numberOfColumns, null);
        }
        Integer pageNumber = null;
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            TableBorderRow row = rows[rowNumber];
            int columnNumber = 0;
            for (INode elem : tableRows.get(rowNumber).getChildren()) {
                SemanticType type = elem.getInitialSemanticType();
                if (SemanticType.TABLE_CELL != type && SemanticType.TABLE_HEADER != type) {
                    continue;
                }
                while (columnNumber < numberOfColumns && row.getCells()[columnNumber] != null) {
                    ++columnNumber;
                }
                TableBorderCell cell = new TableBorderCell(elem, rowNumber, columnNumber);
                BoundingBox box = new BoundingBox();
                addContentToCell(cell, box, elem);
                cell.setBoundingBox(box);
                if (cell.getPageNumber() != null) {
                    if (pageNumber != null && !Objects.equals(pageNumber, cell.getPageNumber())) {
                        isBadTable = true;
                    }
                    pageNumber = cell.getPageNumber();
                }
                for (int i = 0; i < cell.getRowSpan(); i++) {
                    for (int j = 0; j < cell.getColSpan(); j++) {
                        rows[rowNumber + i].getCells()[columnNumber + j] = cell;
                    }
                }
                columnNumber += cell.getColSpan();
            }
        }
        calculateCoordinatesUsingBoundingBoxesOfRowsAndColumns();
        calculateBoundingBoxesUsingCoordinates(pageNumber, false);
    }
    
    public void calculateBoundingBoxesUsingCoordinates(Integer pageNumber, boolean isRewriteBoundingBoxes) {
        getBoundingBox().union(new BoundingBox(pageNumber, xCoordinates.get(0), yCoordinates.get(numberOfRows), xCoordinates.get(numberOfColumns), yCoordinates.get(0)));
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            BoundingBox multiBoundingBox = new MultiBoundingBox();
            for (int colNumber = 0; colNumber < numberOfColumns; colNumber++) {
                if (rows[rowNumber].cells[colNumber].colNumber == colNumber &&
                        rows[rowNumber].cells[colNumber].rowNumber == rowNumber) {
                    multiBoundingBox.union(rows[rowNumber].cells[colNumber].getBoundingBox());
                }
            }
            multiBoundingBox.setLeftX(getLeftX());
            multiBoundingBox.setRightX(getRightX());
            rows[rowNumber].setBoundingBox(multiBoundingBox);
        }
        for (int rowNumber = 0; rowNumber < getNumberOfRows(); rowNumber++) {
            for (int columnNumber = 0; columnNumber < getNumberOfColumns(); columnNumber++) {
                TableBorderCell cell = getCell(rowNumber, columnNumber);
                if (cell.getRowNumber() == rowNumber && cell.getColNumber() == columnNumber) {
                    if (isRewriteBoundingBoxes || cell.getBoundingBox().getPageNumber() == null) {
                        BoundingBox boundingBox = new BoundingBox(pageNumber, getLeftX(columnNumber),
                                getBottomY(rowNumber + cell.getRowSpan() - 1),
                                getRightX(columnNumber + cell.getColSpan() - 1), getTopY(rowNumber));
                        cell.setBoundingBox(boundingBox);
                    }
                }
            }
        }
    }

    public TableBorderRow[] getRows() {
        return rows;
    }

    public TableBorderRow getRow(int rowNumber) {
        return rows[rowNumber];
    }

    public TableBorderCell getCell(int rowNumber, int columnNumber) {
        return getRow(rowNumber).getCell(columnNumber);
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
            if (y2 > v.getTopY() + NodeUtils.VERTEX_TABLE_FACTOR * v.getRadius()) {
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
        boolean[] hasTopBorder = new boolean[numberOfColumns];
        boolean[] hasBottomBorder = new boolean[numberOfColumns];
        processHorizontalLines(rows, numberOfRows, builder, hasTopBorder, hasBottomBorder);
        boolean[] hasLeftBorder = new boolean[numberOfRows];
        boolean[] hasRightBorder = new boolean[numberOfRows];
        processVerticalLines(rows, numberOfColumns, builder, hasLeftBorder, hasRightBorder);
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
                            xCoordinates.get(colNumber), yCoordinates.get(rowNumber + cell.rowSpan),
                            xCoordinates.get(colNumber + cell.colSpan), yCoordinates.get(rowNumber));
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
        } else {
            deleteRedundantRowsAndColumns(rows, numberOfRows, numberOfColumns, redundantRows, usefulRows, redundantColumns, usefulColumns);
        }
        checkBorders(hasTopBorder, hasBottomBorder, hasLeftBorder, hasRightBorder);
    }

    private void processHorizontalLines(TableBorderRow[] rows, int numberOfRows,
                                        TableBorderBuilder builder, boolean[] hasTopBorder, boolean[] hasBottomBorder) {
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
    }

    private void processVerticalLines(TableBorderRow[] rows, int numberOfColumns,
                                        TableBorderBuilder builder, boolean[] hasLeftBorder, boolean[] hasRightBorder) {
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
    }
    
    private boolean checkBorders(boolean[] hasTopBorder, boolean[] hasBottomBorder, 
                                 boolean[] hasLeftBorder, boolean[] hasRightBorder) {
        if (!StaticContainers.isDataLoader() || numberOfRows == 1 || numberOfColumns == 1) {
            for (int i = 0; i < hasBottomBorder.length; i++) {
                if (!hasBottomBorder[i] || !hasTopBorder[i]) {
                    isBadTable = true;
                    return isBadTable;
                }
            }
            for (int i = 0; i < hasRightBorder.length; i++) {
                if (!hasRightBorder[i] || !hasLeftBorder[i]) {
                    isBadTable = true;
                    return isBadTable;
                }
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
            if (x <= getRightX(i - 1) + NodeUtils.EPSILON && x >= getLeftX(i) - NodeUtils.EPSILON) {
                return i;
            }
        }
        return -1;
    }

    private int getCoordinateY(double y) {
        for (int i = 0; i < yCoordinates.size(); i++) {
            if (y <= getTopY(i) + NodeUtils.EPSILON && y >= getBottomY(i - 1) - NodeUtils.EPSILON) {
                return i;
            }
        }
        return -1;
    }

    private int getClosestLeftX(double x) {
        for (int i = xCoordinates.size() - 1; i >= 0; i--) {
            if (x >= getLeftX(i) - TABLE_BORDER_EPSILON) {
                return i;
            }
        }
        return -1;
    }

    private int getClosestRightX(double x) {
        for (int i = 0; i < xCoordinates.size(); i++) {
            if (x <= getRightX(i - 1) + TABLE_BORDER_EPSILON) {
                return i;
            }
        }
        return xCoordinates.size();
    }

    private int getClosestTopY(double y) {
        for (int i = yCoordinates.size() - 1; i >= 0; i--) {
            if (y <= getTopY(i) + TABLE_BORDER_EPSILON) {
                return i;
            }
        }
        return -1;
    }

    private int getClosestBottomY(double y) {
        for (int i = 0; i < yCoordinates.size(); i++) {
            if (y >= getBottomY(i - 1) - TABLE_BORDER_EPSILON) {
                return i;
            }
        }
        return yCoordinates.size();
    }

    public void calculateCoordinatesUsingBoundingBoxesOfRowsAndColumns() {
        this.yCoordinates.add(getTableRowTopY(0));
        this.yWidths.add(0.0d);
        for (int rowNumber = 0; rowNumber < numberOfRows - 1; rowNumber++) {
            this.yCoordinates.add(0.5 * (getTableRowBottomY(rowNumber) + getTableRowTopY(rowNumber + 1)));
            this.yWidths.add(0.0d);
        }
        this.yCoordinates.add(getTableRowBottomY(numberOfRows - 1));
        this.yWidths.add(0.0d);
        this.xCoordinates.add(getTableColumnLeftX(0));
        this.xWidths.add(0.0d);
        for (int columnNumber = 0; columnNumber < numberOfColumns - 1; columnNumber++) {
            this.xCoordinates.add(0.5 * (getTableColumnRightX(columnNumber) + getTableColumnLeftX(columnNumber + 1)));
            this.xWidths.add(0.0d);
        }
        this.xCoordinates.add(getTableColumnRightX(numberOfColumns - 1));
        this.xWidths.add(0.0d);
    }
    
    public boolean checkColumnsIntersections() {
        for (int columnNumber = 0; columnNumber < numberOfColumns - 1; columnNumber++) {
            if (getTableColumnRightX(columnNumber) > getTableColumnLeftX(columnNumber + 1)) {
                return false;
            }
        }
        return true;
    }

    private double getTableColumnRightX(int columnNumber) {
        double rightX = -Double.MAX_VALUE;
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            TableBorderCell currentCell = rows[rowNumber].getCell(columnNumber);
            if (currentCell.getRowNumber() != rowNumber ||
                    currentCell.getColNumber() + currentCell.getColSpan() != columnNumber + 1) {
                continue;
            }
            if (rightX < currentCell.getRightX()) {
                rightX = currentCell.getRightX();
            }
        }
        return rightX;
    }

    private double getTableColumnLeftX(int columnNumber) {
        double leftX = Double.MAX_VALUE;
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            TableBorderCell currentCell = rows[rowNumber].getCell(columnNumber);
            if (currentCell.getRowNumber() != rowNumber || currentCell.getColNumber() != columnNumber) {
                continue;
            }
            if (leftX > currentCell.getLeftX()) {
                leftX = currentCell.getLeftX();
            }
        }
        return leftX;
    }

    private double getTableRowBottomY(int rowNumber) {
        double bottomY = Double.MAX_VALUE;
        for (int columnNumber = 0; columnNumber < numberOfColumns; columnNumber++) {
            TableBorderCell currentCell = rows[rowNumber].getCell(columnNumber);
            if (currentCell.getRowNumber() + currentCell.getRowSpan() != rowNumber + 1 ||
                    currentCell.getColNumber() != columnNumber) {
                continue;
            }
            if (bottomY > currentCell.getBottomY()) {
                bottomY = currentCell.getBottomY();
            }
        }
        return bottomY;
    }

    private double getTableRowTopY(int rowNumber) {
        double topY = -Double.MAX_VALUE;
        for (int columnNumber = 0; columnNumber < numberOfColumns; columnNumber++) {
            TableBorderCell currentCell = rows[rowNumber].getCell(columnNumber);
            if (currentCell.getRowNumber() != rowNumber || currentCell.getColNumber() != columnNumber) {
                continue;
            }
            if (topY < currentCell.getTopY()) {
                topY = currentCell.getTopY();
            }
        }
        return topY;
    }
    
    public double getLeftX(int columnNumber) {
        return xCoordinates.get(columnNumber) - 0.5 * xWidths.get(columnNumber);
    }

    public double getBottomY(int rowNumber) {
        return yCoordinates.get(rowNumber + 1) - 0.5 * yWidths.get(rowNumber + 1);

    }

    public double getRightX(int columnNumber) {
        return xCoordinates.get(columnNumber + 1) + 0.5 * xWidths.get(columnNumber + 1);

    }

    public double getTopY(int rowNumber) {
        return yCoordinates.get(rowNumber) + 0.5 * yWidths.get(rowNumber);
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
        return isBadTable || numberOfRows < 1 || numberOfColumns < 1 || (!StaticContainers.isDataLoader() && numberOfRows == 1 && numberOfColumns == 1);
    }
    
    public boolean isOneCellTable() {
        return numberOfRows == 1 && numberOfColumns == 1;
    }

    public boolean isTextBlock() {
        return isOneCellTable() && !getCell(0, 0).getContents().isEmpty();
    }

    public Long getPreviousTableId() {
        return previousTable != null ? previousTable.getRecognizedStructureId() : null;
    }

    public Long getNextTableId() {
        return nextTable != null ? nextTable.getRecognizedStructureId() : null;
    }

    public TableBorder getPreviousTable() {
        return previousTable;
    }

    public void setPreviousTable(TableBorder previousTable) {
        this.previousTable = previousTable;
    }

    public boolean checkTableCoordinates() {
        for (int i = 0; i < xCoordinates.size() - 1; i++) {
            if (xCoordinates.get(i) > xCoordinates.get(i + 1)) {
                return false;
            }
        }
        for (int i = 0; i < yCoordinates.size() - 1; i++) {
            if (yCoordinates.get(i) < yCoordinates.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkEmptyRowsOrColumns() {
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            if (NodeUtils.areCloseNumbers(getTableRowBottomY(rowNumber), Double.MAX_VALUE)) {
                return false;
            }
            if (NodeUtils.areCloseNumbers(getTableRowTopY(rowNumber), -Double.MAX_VALUE)) {
                return false;
            }
        }
        for (int columnNumber = 0; columnNumber < numberOfColumns; columnNumber++) {
            if (NodeUtils.areCloseNumbers(getTableColumnLeftX(columnNumber), Double.MAX_VALUE)) {
                return false;
            }
            if (NodeUtils.areCloseNumbers(getTableColumnRightX(columnNumber), -Double.MAX_VALUE)) {
                return false;
            }
        }
        return true;
    }

    private void addContentToCell(TableBorderCell cell, BoundingBox boundingBox, INode elem) {
        for (INode child : elem.getChildren()) {
            if (child instanceof SemanticSpan) {
                cell.addContentObject(((SemanticSpan)child).getColumns().get(0).getFirstLine().getFirstTextChunk());
                boundingBox.union(child.getBoundingBox());
            } else if (child instanceof SemanticFigure) {
                cell.addContentObject(child);
                boundingBox.union(child.getBoundingBox());
            } else {
                addContentToCell(cell, boundingBox, child);
            }
        }
    }

    public TableBorder getNextTable() {
        return nextTable;
    }

    public void setNextTable(TableBorder nextTable) {
        this.nextTable = nextTable;
    }

    public boolean isTableTransformer() {
        return isTableTransformer;
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

    public TableBorderCell getTableBorderCell(IObject object) {
        BoundingBox box = object.getBoundingBox();
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
        while (xLeftIndex >= xRightIndex) {
            xLeftIndex--;
            xRightIndex++;
        } 
        while (yTopIndex >= yBottomIndex) {
            yTopIndex--;
            yBottomIndex++;
        }
        for (int xIndex = xLeftIndex; xIndex < xRightIndex; xIndex++) {
            for (int yIndex = yTopIndex; yIndex < yBottomIndex; yIndex++) {
                TableBorderCell cell = rows[yIndex].cells[xIndex];
                if (box.getIntersectionPercent(cell.getBoundingBox()) > MIN_CELL_CONTENT_INTERSECTION_PERCENT) {
                    return cell;
                }
            }
        }
        return null;
    }

    public Set<TableBorderCell> getTableBorderCells(IObject object) {
        BoundingBox box = object.getBoundingBox();
        int xLeftIndex = getClosestLeftX(box.getLeftX());
        int xRightIndex = getClosestRightX(box.getRightX());
        int yTopIndex = getClosestTopY(box.getTopY());
        int yBottomIndex = getClosestBottomY(box.getBottomY());
        if (xLeftIndex == xCoordinates.size() - 1 || yTopIndex == yCoordinates.size() - 1 ||
                xRightIndex == 0 || yBottomIndex == 0) {
            return Collections.emptySet();
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
        while (xLeftIndex >= xRightIndex) {
            xLeftIndex--;
            xRightIndex++;
        }
        while (yTopIndex >= yBottomIndex) {
            yTopIndex--;
            yBottomIndex++;
        }
        Set<TableBorderCell> cells = new HashSet<>();
        int rowNumber = getRowNumber(yTopIndex, yBottomIndex, box);
        for (int xIndex = xLeftIndex; xIndex < xRightIndex; xIndex++) {
            TableBorderCell cell = rows[rowNumber].cells[xIndex];
            cells.add(cell);
        }
        return cells;
    }
    
    private int getRowNumber(int yTopIndex, int yBottomIndex, BoundingBox box) {
        if (yTopIndex + 1 == yBottomIndex) {
            return yTopIndex;
        }
        double maxPercent = -Double.MAX_VALUE;
        int rowNumber = 0;
        for (int yIndex = yTopIndex; yIndex < yBottomIndex; yIndex++) {
            double percent = box.getVerticalIntersectionPercent(rows[yIndex].getBoundingBox());
            if (percent > maxPercent) {
                maxPercent = percent;
                rowNumber = yIndex;
            }
        }
        return rowNumber;
    }
}

package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticTable;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorderCell;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ErrorCodes;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class TableChecker implements Consumer<INode> {

    @Override
    public void accept(INode node) {
        if (node.getInitialSemanticType() != SemanticType.TABLE) {
            return;
        }
        node.setSemanticType(SemanticType.TABLE);
        checkTable(node);
    }

    private static void checkTable(INode table) {
        List<INode> tableRows = getTableRows(table);
        int numberOfRows = tableRows.size();
        if (numberOfRows == 0) {
            return;
        }
        int numberOfColumns = getNumberOfColumns(tableRows.get(0));
        TableBorderCell[][] cells = new TableBorderCell[numberOfRows][numberOfColumns];
        if (!checkRegular(tableRows, cells, numberOfRows, numberOfColumns)) {
            return;
        }
        setTableElementsID(table, tableRows, cells);
        checkTableCells(cells);
        checkTableCellsPosition(table, cells, numberOfRows, numberOfColumns);
        checkTableVisualRepresentation(table, cells, numberOfRows, numberOfColumns);
    }

    private static void setTableElementsID(INode table, List<INode> tableRows, TableBorderCell[][] cells) {
        Long id = StaticContainers.getNextID();
        table.setRecognizedStructureId(id);
        for (INode elem : table.getChildren()) {
            if (SemanticType.TABLE_FOOTER == elem.getInitialSemanticType() ||
                    SemanticType.TABLE_BODY == elem.getInitialSemanticType() ||
                    SemanticType.TABLE_HEADERS == elem.getInitialSemanticType()) {
                elem.setRecognizedStructureId(id);
            }
        }
        for (INode row : tableRows) {
            row.setRecognizedStructureId(id);
        }
        for (int rowNumber = 0; rowNumber < cells.length; rowNumber++) {
            for (int colNumber = 0; colNumber < cells[rowNumber].length; colNumber++) {
                TableBorderCell cell = cells[rowNumber][colNumber];
                if (cell.getRowNumber() == rowNumber && cell.getColNumber() == colNumber) {
                    cell.getNode().setRecognizedStructureId(id);
                }
            }
        }
    }

    private static List<INode> getTableRows(INode table) {
        List<INode> listTR = new LinkedList<>();
        for (INode elem : table.getChildren()) {
            SemanticType type = elem.getInitialSemanticType();
            if (SemanticType.TABLE_ROW == type) {
                listTR.add(elem);
            } else if (SemanticType.TABLE_FOOTER == type || SemanticType.TABLE_BODY == type ||
                    SemanticType.TABLE_HEADERS == type) {
                for (INode child : elem.getChildren()) {
                    if (SemanticType.TABLE_ROW == child.getInitialSemanticType()) {
                        listTR.add(child);
                    }
                }
            }
        }
        return listTR;
    }

    private static Integer getNumberOfColumns(INode firstTR) {
        int numberOfColumns = 0;
        for (INode elem : firstTR.getChildren()) {
            SemanticType type = elem.getInitialSemanticType();
            if (SemanticType.TABLE_HEADER == type || SemanticType.TABLE_CELL == type) {
                numberOfColumns += elem.getAttributesDictionary().getColSpan();
            }
        }
        return numberOfColumns;
    }

    private static boolean checkRegular(List<INode> tableRows, TableBorderCell[][] cells, int numberOfRows, int numberOfColumns) {
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            int columnNumber = 0;
            for (INode elem : tableRows.get(rowNumber).getChildren()) {
                SemanticType type = elem.getInitialSemanticType();
                if (SemanticType.TABLE_CELL != type && SemanticType.TABLE_HEADER != type) {
                    continue;
                }
                while (columnNumber < numberOfColumns && cells[rowNumber][columnNumber] != null) {
                    ++columnNumber;
                }
                TableBorderCell cell = new TableBorderCell(elem, rowNumber, columnNumber);
                if (columnNumber >= numberOfColumns) {
                    return false;
                }
                if (rowNumber + cell.getRowSpan() > numberOfRows || columnNumber + cell.getColSpan() > numberOfColumns) {
                    return false;
                }
                if (!checkRegular(cells, cell)) {
                    return false;
                }
                columnNumber += cell.getColSpan();
            }
        }
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                if (cells[i][j] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Boolean checkRegular(TableBorderCell[][] cells, TableBorderCell cell) {
        for (int i = 0; i < cell.getRowSpan(); i++) {
            for (int j = 0; j < cell.getColSpan(); j++) {
                if (cells[cell.getRowNumber() + i][cell.getColNumber() + j] != null) {
                    return false;
                }
                cells[cell.getRowNumber()  + i][cell.getColNumber() + j] = cell;
            }
        }
        return true;
    }

    private static void checkTableCells(TableBorderCell[][] cells) {
        for (int rowNumber = 0; rowNumber < cells.length; rowNumber++) {
            for (int colNumber = 0; colNumber < cells[rowNumber].length; colNumber++) {
                TableBorderCell cell = cells[rowNumber][colNumber];
                if (cell.getRowNumber() == rowNumber && cell.getColNumber() == colNumber) {
                    if (isHeaderCell(cell.getNode(), cell, cells)) {
                        cell.getNode().setCorrectSemanticScore(1.0);
                        cell.getNode().setSemanticType(SemanticType.TABLE_HEADER);
                        cell.setSemanticType(SemanticType.TABLE_HEADER);
                    } else {
                        cell.getNode().setCorrectSemanticScore(1.0);
                        cell.getNode().setSemanticType(SemanticType.TABLE_CELL);
                        cell.setSemanticType(SemanticType.TABLE_CELL);
                    }
                }
            }
        }
    }

    private static boolean isHeaderCell(INode cellNode, TableBorderCell cell, TableBorderCell[][] cells) {
        if (cellNode.getInitialSemanticType() != SemanticType.TABLE_HEADER) {
            return false;
        }
        if (cell.getColNumber() == 0 || cell.getRowNumber() == 0) {
            return true;
        }
        for (int rowNumber = cell.getRowNumber(); rowNumber < cell.getRowNumber() + cell.getRowSpan(); rowNumber++) {
            if (cells[rowNumber][cell.getColNumber() - 1].getSemanticType() == SemanticType.TABLE_HEADER) {
                return true;
            }
        }
        for (int colNumber = cell.getColNumber(); colNumber < cell.getColNumber() + cell.getColSpan(); colNumber++) {
            if (cells[cell.getRowNumber() - 1][colNumber].getSemanticType() == SemanticType.TABLE_HEADER) {
                return true;
            }
        }
        return false;
    }

    private static void checkTableCellsPosition(INode table, TableBorderCell[][] cells, int numberOfRows, int numberOfColumns) {
        if (table.getPageNumber() == null) {
            return;
        }
        checkTableCellsBottom(cells, numberOfRows);
        checkTableCellsTop(cells, numberOfRows);
        checkTableCellsRight(table, cells, numberOfColumns);
        checkTableCellsLeft(table, cells, numberOfColumns);
    }

    private static void checkTableCellsBottom(TableBorderCell[][] cells, int numberOfRows) {
        for (int rowNumber = 0; rowNumber < numberOfRows - 1; rowNumber++) {
            TableBorderCell maxBottomCell = getMaxBottomCell(cells, rowNumber + 1);
            TableBorderCell minBottomCell = getMinBottomCell(cells, rowNumber);
            if (maxBottomCell != null && minBottomCell != null &&
                    isFirstBottomMax(maxBottomCell.getBoundingBox(), minBottomCell.getBoundingBox())) {
                ErrorCodes.addErrorCodeWithArguments(minBottomCell.getNode(), ErrorCodes.ERROR_CODE_1100);
            }
        }
    }

    private static void checkTableCellsTop(TableBorderCell[][] cells, int numberOfRows) {
        for (int rowNumber = 0; rowNumber < numberOfRows - 1; rowNumber++) {
            TableBorderCell maxTopCell = getMaxTopCell(cells, rowNumber + 1);
            TableBorderCell minTopCell = getMinTopCell(cells, rowNumber);
            if (maxTopCell != null && minTopCell != null &&
                    isFirstTopMax(maxTopCell.getBoundingBox(), minTopCell.getBoundingBox())) {
                ErrorCodes.addErrorCodeWithArguments(maxTopCell.getNode(), ErrorCodes.ERROR_CODE_1101);
            }
        }
    }

    private static void checkTableCellsRight(INode table, TableBorderCell[][] cells, int numberOfColumns) {
        for (int pageNumber = table.getPageNumber(); pageNumber <= table.getLastPageNumber(); pageNumber++) {
            for (int columnNumber = 0; columnNumber < numberOfColumns - 1; columnNumber++) {
                TableBorderCell maxRightCell = getMaxRightCell(cells, columnNumber, pageNumber);
                TableBorderCell minRightCell = getMinRightCell(cells, columnNumber + 1, pageNumber);
                if (maxRightCell != null && minRightCell != null &&
                        isFirstRightMax(maxRightCell.getBoundingBox(), minRightCell.getBoundingBox(), pageNumber)) {
                    ErrorCodes.addErrorCodeWithArguments(maxRightCell.getNode(), ErrorCodes.ERROR_CODE_1102);
                }
            }
        }
    }

    private static void checkTableCellsLeft(INode table, TableBorderCell[][] cells, int numberOfColumns) {
        for (int pageNumber = table.getPageNumber(); pageNumber <= table.getLastPageNumber(); pageNumber++) {
            for (int columnNumber = 0; columnNumber < numberOfColumns - 1; columnNumber++) {
                TableBorderCell maxLeftCell = getMaxLeftCell(cells, columnNumber, pageNumber);
                TableBorderCell minLeftCell = getMinLeftCell(cells, columnNumber + 1, pageNumber);
                if (maxLeftCell != null && minLeftCell != null &&
                        isFirstLeftMax(maxLeftCell.getBoundingBox(), minLeftCell.getBoundingBox(), pageNumber)) {
                    ErrorCodes.addErrorCodeWithArguments(minLeftCell.getNode(), ErrorCodes.ERROR_CODE_1103);
                }
            }
        }
    }

    private static TableBorderCell getMaxTopCell(TableBorderCell[][] cells, int rowNumber) {
        TableBorderCell cell = null;
        for (int columnNumber = 0; columnNumber < cells[rowNumber].length; columnNumber++) {
            TableBorderCell currentCell = cells[rowNumber][columnNumber];
            if (currentCell.getRowNumber() != rowNumber || currentCell.getColNumber() != columnNumber ||
                    currentCell.getBoundingBox().getPageNumber() == null) {
                continue;
            }
            if (cell == null || isFirstTopMax(currentCell.getBoundingBox(), cell.getBoundingBox())) {
                cell = currentCell;
            }
        }
        return cell;
    }

    private static TableBorderCell getMinTopCell(TableBorderCell[][] cells, int rowNumber) {
        TableBorderCell cell = null;
        for (int columnNumber = 0; columnNumber < cells[rowNumber].length; columnNumber++) {
            TableBorderCell currentCell = cells[rowNumber][columnNumber];
            if (currentCell.getRowNumber()  + currentCell.getRowSpan() != rowNumber + 1 ||
                    currentCell.getColNumber() != columnNumber ||
                    currentCell.getBoundingBox().getPageNumber() == null) {
                continue;
            }
            if (cell == null || isFirstTopMax(cell.getBoundingBox(), currentCell.getBoundingBox())) {
                cell = currentCell;
            }
        }
        return cell;
    }

    private static TableBorderCell getMaxBottomCell(TableBorderCell[][] cells, int rowNumber) {
        TableBorderCell cell = null;
        for (int columnNumber = 0; columnNumber < cells[rowNumber].length; columnNumber++) {
            TableBorderCell currentCell = cells[rowNumber][columnNumber];
            if (currentCell.getRowNumber() != rowNumber || currentCell.getColNumber() != columnNumber ||
                    currentCell.getBoundingBox().getPageNumber() == null) {
                continue;
            }
            if (cell == null || isFirstBottomMax(currentCell.getBoundingBox(), cell.getBoundingBox())) {
                cell = currentCell;
            }
        }
        return cell;
    }

    private static TableBorderCell getMinBottomCell(TableBorderCell[][] cells, int rowNumber) {
        TableBorderCell cell = null;
        for (int columnNumber = 0; columnNumber < cells[rowNumber].length; columnNumber++) {
            TableBorderCell currentCell = cells[rowNumber][columnNumber];
            if (currentCell.getRowNumber() + currentCell.getRowSpan() != rowNumber + 1 || currentCell.getColNumber() !=
                    columnNumber || currentCell.getBoundingBox().getPageNumber() == null) {
                continue;
            }
            if (cell == null || isFirstBottomMax(cell.getBoundingBox(), currentCell.getBoundingBox())) {
                cell = currentCell;
            }
        }
        return cell;
    }

    private static Boolean isFirstTopMax(BoundingBox boundingBox1, BoundingBox boundingBox2) {
        return boundingBox1.getPageNumber() < boundingBox2.getPageNumber() ||
                (boundingBox1.getPageNumber().equals(boundingBox2.getPageNumber()) &&
                        boundingBox1.getTopY() > boundingBox2.getTopY());
    }

    private static Boolean isFirstBottomMax(BoundingBox boundingBox1, BoundingBox boundingBox2) {
        return boundingBox1.getLastPageNumber() < boundingBox2.getLastPageNumber() ||
                (boundingBox1.getLastPageNumber().equals(boundingBox2.getLastPageNumber()) &&
                        boundingBox1.getBottomY() > boundingBox2.getBottomY());
    }

    private static TableBorderCell getMaxRightCell(TableBorderCell[][] cells, int columnNumber, int pageNumber) {
        TableBorderCell cell = null;
        for (int rowNumber = 0; rowNumber < cells.length; rowNumber++) {
            TableBorderCell currentCell = cells[rowNumber][columnNumber];
            if (currentCell.getRowNumber() != rowNumber || currentCell.getColNumber() + currentCell.getColSpan() !=
                    columnNumber + 1 || currentCell.getBoundingBox().getPageNumber() == null ||
                    currentCell.getBoundingBox().getPageNumber() > pageNumber ||
                    currentCell.getBoundingBox().getLastPageNumber() < pageNumber) {
                continue;
            }
            if (cell == null || isFirstRightMax(currentCell.getBoundingBox(), cell.getBoundingBox(), pageNumber)) {
                cell = currentCell;
            }
        }
        return cell;
    }

    private static TableBorderCell getMinRightCell(TableBorderCell[][] cells, int columnNumber, int pageNumber) {
        TableBorderCell cell = null;
        for (int rowNumber = 0; rowNumber < cells.length; rowNumber++) {
            TableBorderCell currentCell = cells[rowNumber][columnNumber];
            if (currentCell.getRowNumber() != rowNumber || currentCell.getColNumber() != columnNumber ||
                    currentCell.getBoundingBox().getPageNumber() == null ||
                    currentCell.getBoundingBox().getPageNumber() > pageNumber ||
                    currentCell.getBoundingBox().getLastPageNumber() < pageNumber) {
                continue;
            }
            if (cell == null || isFirstRightMax(cell.getBoundingBox(), currentCell.getBoundingBox(), pageNumber)) {
                cell = currentCell;
            }
        }
        return cell;
    }

    private static TableBorderCell getMaxLeftCell(TableBorderCell[][] cells, int columnNumber, int pageNumber) {
        TableBorderCell cell = null;
        for (int rowNumber = 0; rowNumber < cells.length; rowNumber++) {
            TableBorderCell currentCell = cells[rowNumber][columnNumber];
            if (currentCell.getRowNumber() != rowNumber ||
                    currentCell.getColNumber() + currentCell.getColSpan() != columnNumber + 1 ||
                    currentCell.getBoundingBox().getPageNumber() == null ||
                    currentCell.getBoundingBox().getPageNumber() > pageNumber ||
                    currentCell.getBoundingBox().getLastPageNumber() < pageNumber) {
                continue;
            }
            if (cell == null || isFirstLeftMax(currentCell.getBoundingBox(), cell.getBoundingBox(), pageNumber)) {
                cell = currentCell;
            }
        }
        return cell;
    }

    private static TableBorderCell getMinLeftCell(TableBorderCell[][] cells, int columnNumber, int pageNumber) {
        TableBorderCell cell = null;
        for (int rowNumber = 0; rowNumber < cells.length; rowNumber++) {
            TableBorderCell currentCell = cells[rowNumber][columnNumber];
            if (currentCell.getRowNumber() != rowNumber || currentCell.getColNumber() != columnNumber ||
                    currentCell.getBoundingBox().getPageNumber() == null ||
                    currentCell.getBoundingBox().getPageNumber() > pageNumber ||
                    currentCell.getBoundingBox().getLastPageNumber() < pageNumber) {
                continue;
            }
            if (cell == null || isFirstLeftMax(cell.getBoundingBox(), currentCell.getBoundingBox(), pageNumber)) {
                cell = currentCell;
            }
        }
        return cell;
    }

    private static Boolean isFirstRightMax(BoundingBox boundingBox1, BoundingBox boundingBox2, int pageNumber) {
        return boundingBox1.getRightX(pageNumber) > boundingBox2.getRightX(pageNumber);
    }

    private static Boolean isFirstLeftMax(BoundingBox boundingBox1, BoundingBox boundingBox2, int pageNumber) {
        return boundingBox1.getLeftX(pageNumber) > boundingBox2.getLeftX(pageNumber);
    }

    private static void checkTableVisualRepresentation(INode table, TableBorderCell[][] cells, int numberOfRows, int numberOfColumns) {
        if (table.getPageNumber() != null && !table.getPageNumber().equals(table.getLastPageNumber())) {
            return;
        }
        INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(table);
        if (!(accumulatedNode instanceof SemanticTable)) {
            return;
        }
        SemanticTable semanticTable = (SemanticTable) accumulatedNode;
        TableBorder border = semanticTable.getTableBorder();
        if (border == null) {
            return;
        }
        StaticContainers.getIdMapper().put(border.getRecognizedStructureId(), table.getRecognizedStructureId());
        if (border.getNumberOfRows() != numberOfRows) {
            ErrorCodes.addErrorCodeWithArguments(table, ErrorCodes.ERROR_CODE_1104,
                    numberOfRows, border.getNumberOfRows());
            return;
        }
        if (border.getNumberOfColumns() != numberOfColumns) {
            ErrorCodes.addErrorCodeWithArguments(table, ErrorCodes.ERROR_CODE_1105,
                    numberOfColumns, border.getNumberOfColumns());
            return;
        }
        for (int rowNumber = 0; rowNumber < numberOfRows; rowNumber++) {
            for (int colNumber = 0; colNumber < numberOfColumns; colNumber++) {
                TableBorderCell cell = cells[rowNumber][colNumber];
                TableBorderCell borderCell = border.getRow(rowNumber).getCell(colNumber);
                if (cell.getRowNumber() == rowNumber && cell.getColNumber() == colNumber &&
                        borderCell.getRowNumber() == rowNumber && borderCell.getColNumber() == colNumber) {
                    if (cell.getRowSpan() != borderCell.getRowSpan()) {
                        ErrorCodes.addErrorCodeWithArguments(cell.getNode(), ErrorCodes.ERROR_CODE_1106,
                                cell.getRowSpan(), borderCell.getRowSpan());
                    }
                    if (cell.getColSpan() != borderCell.getColSpan()) {
                        ErrorCodes.addErrorCodeWithArguments(cell.getNode(), ErrorCodes.ERROR_CODE_1107,
                                cell.getColSpan(), borderCell.getColSpan());
                    }
                }
            }
        }
    }
}

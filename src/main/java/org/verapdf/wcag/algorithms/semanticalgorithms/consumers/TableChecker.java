package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorderCell;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class TableChecker implements Consumer<INode> {

    @Override
    public void accept(INode node) {
        if (node.getInitialSemanticType() != SemanticType.TABLE) {
            return;
        }
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
        checkTableCells(cells);
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
}

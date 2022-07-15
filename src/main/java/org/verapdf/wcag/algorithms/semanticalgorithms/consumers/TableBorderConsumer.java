package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.SemanticTable;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.SemanticImageNode;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;
import org.verapdf.wcag.algorithms.entities.tables.TableToken;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorderCell;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorderRow;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;

import java.util.*;
import java.util.stream.Collectors;

public class TableBorderConsumer {

    public void recognizeTables(ITree tree) {
        for (INode node : tree) {
            if (node.getChildren().isEmpty()) {
                if (node instanceof SemanticTextNode) {
                    SemanticTextNode textNode = (SemanticTextNode) node;
                    for (TextColumn column : textNode.getColumns()) {
                        for (TextLine line : column.getLines()) {
                            for (TextChunk chunk : line.getTextChunks()) {
                                add(new TableToken(chunk, node));
                            }
                        }
                    }
                } else if ((node instanceof SemanticImageNode)) {
                    SemanticImageNode imageNode = (SemanticImageNode) node;
                    add(new TableToken(imageNode.getImage(), imageNode));
                }
            }
        }
        updateTreeWithRecognizedTables();
    }

    private void add(TableToken token) {
        TableBorder tableBorder = StaticContainers.getTableBordersCollection().getTableBorder(token.getBoundingBox());
        if (tableBorder != null) {
            TableBorderCell tableBorderCell = tableBorder.getTableBorderCell(token.getBoundingBox());
            if (tableBorderCell != null) {
                tableBorderCell.addContent(token);
            }
            //token in table but not in cell?
        }
    }

    private void updateTreeWithRecognizedTables() {
        for (SortedSet<TableBorder> tables : StaticContainers.getTableBordersCollection().getTableBorders()) {
            for (TableBorder table : tables) {
                INode tableNode = getTableNode(table);
                if (tableNode != null) {
                    table.setNode(tableNode);
                    Integer depth = Arrays.stream(table.getRows())
                            .map(TableBorderRow::getNode)
                            .filter(Objects::nonNull)
                            .map(INode::getDepth)
                            .min(Integer::compare).orElse(null);
                    Set<INode> rowNodes = Arrays.stream(table.getRows())
                            .map(TableBorderRow::getNode)
                            .collect(Collectors.toSet());
                    if (depth != null) {
                        if (tableNode.getDepth() < depth - 1) {
                            updateTreeWithRecognizedTableRowsGroups(table, tableNode, rowNodes);
                        } else {
                            setTypesForEmptyRowNodes(table, tableNode.getChildren());
                        }
                        updateTreeWithRecognizedTableRows(table, depth);
                    }
                    if (ClusterTableConsumer.isNodeInsideTable(tableNode, table.getId(), table.getBoundingBox())) {
                        StaticContainers.getAccumulatedNodeMapper().updateNode(tableNode, new SemanticTable(table),
                                1.0, SemanticType.TABLE);
                        ClusterTableConsumer.detectTableCaptions(table.getBoundingBox(), tableNode);
                    }
                }
            }
        }
    }

    private static void setTypesForEmptyRowNodes(TableBorder table, List<INode> rowNodes) {
        if (rowNodes.size() == table.getNumberOfRows()) {
            for (int rowNumber = 0; rowNumber < table.getRows().length; rowNumber++) {
                TableBorderRow row = table.getRow(rowNumber);
                INode rowNode = rowNodes.get(rowNumber);
                if (row.getNumberOfCellWithContent() == 0 && rowNode.getSemanticType() == null) {
                    rowNode.setSemanticType(SemanticType.TABLE_ROW);
                    rowNode.setBoundingBox(row.getBoundingBox());
                }
                row.setNode(rowNode);
            }
        }
    }

    private static void updateTreeWithRecognizedTableRows(TableBorder table, int depth) {
        for (TableBorderRow row : table.getRows()) {
            INode node = findParent(row.getNode(), depth);
            row.setNode(node);
            updateTreeWithRecognizedTableRow(row, table, depth);
            setType(node, SemanticType.TABLE_ROW, table.getId(), row.getBoundingBox());
        }
    }

    private static void updateTreeWithRecognizedTableRowsGroups(TableBorder table, INode tableNode, Set<INode> rowNodes) {
        Set<INode> nodes = findParents(rowNodes, tableNode.getDepth() + 1);
        Iterator<INode> iterator = nodes.iterator();
        if (nodes.size() < 4) {
            if (nodes.size() == 1) {
                setType(iterator.next(), SemanticType.TABLE_BODY, table.getId(), table.getBoundingBox());
            } else if (nodes.size() == 2) {
                setType(iterator.next(), SemanticType.TABLE_HEADERS, table.getId(), table.getBoundingBox());
                setType(iterator.next(), SemanticType.TABLE_BODY, table.getId(), table.getBoundingBox());
            } else if (nodes.size() == 3) {
                setType(iterator.next(), SemanticType.TABLE_HEADERS, table.getId(), table.getBoundingBox());
                setType(iterator.next(), SemanticType.TABLE_BODY, table.getId(), table.getBoundingBox());
                setType(iterator.next(), SemanticType.TABLE_FOOTER, table.getId(), table.getBoundingBox());
            }
            List<INode> newRowNodes = new ArrayList<>();
            for (INode node : nodes) {
                newRowNodes.addAll(node.getChildren());
            }
            setTypesForEmptyRowNodes(table, newRowNodes);
        }
    }

    private static void updateTreeWithRecognizedTableRow(TableBorderRow row, TableBorder table, int depth) {
        INode rowNode = row.getNode();
        if (rowNode != null && rowNode.getChildren().size() == row.getNumberOfCells()) {
            int number = 0;
            INode cellNode;
            for (int colNumber = 0; colNumber < row.getCells().length; colNumber++) {
                TableBorderCell cell = row.getCell(colNumber);
                if (cell.getRowNumber() == row.getRowNumber() && cell.getColNumber() == colNumber) {
                    cellNode = rowNode.getChildren().get(number);
                    if (cell.getContent().isEmpty() && cellNode.getSemanticType() == null) {
                        cell.setNode(cellNode);
                        cellNode.setBoundingBox(cell.getBoundingBox());
                    }
                    number++;
                }
            }
        }
        List<INode> cells = findParents(Arrays.stream(row.getCells()).map(TableBorderCell::getNode)
                        .collect(Collectors.toList()), depth + 1);
        for (int colNumber = 0; colNumber < cells.size(); colNumber++) {
            TableBorderCell cell = row.getCell(colNumber);
            if (cells.get(colNumber) != null && cell.getRowNumber() == row.getRowNumber() &&
                    cell.getColNumber() == colNumber) {
                MultiBoundingBox box = new MultiBoundingBox(cell.getBoundingBox());
                box.union(cell.getContentBoundingBox());
                if (isHeaderCell(cells.get(colNumber), cell, table)) {
                    setType(cells.get(colNumber), SemanticType.TABLE_HEADER, table.getId(), box);
                    cell.setSemanticType(SemanticType.TABLE_HEADER);
                } else {
                    setType(cells.get(colNumber), SemanticType.TABLE_CELL, table.getId(), box);
                    cell.setSemanticType(SemanticType.TABLE_CELL);
                }
            }
        }
    }

    private static void setType(INode node, SemanticType type, Long id, BoundingBox boundingBox) {
        if (node != null) {
            if (((TableUtils.isTableNode(node)) && node.getRecognizedStructureId() != id) || !ClusterTableConsumer.isNodeInsideTable(node, id, boundingBox)) {
                node.setRecognizedStructureId(null);
            } else {
                node.setRecognizedStructureId(id);
                node.setSemanticType(type);
                node.setCorrectSemanticScore(1.0);
                if (type != SemanticType.TABLE_FOOTER && type != SemanticType.TABLE_BODY && type != SemanticType.TABLE_HEADERS) {
                    node.getBoundingBox().union(boundingBox);
                }
            }
        }
    }

    private INode getTableNode(TableBorder table) {
        Set<INode> rowNodes = new HashSet<>();
        for (TableBorderRow row : table.getRows()) {
            INode rowNode = getRowNode(row);
            if (rowNode != null) {
                row.setNode(rowNode);
                rowNodes.add(rowNode);
            }
        }
        INode tableNode = findCommonParent(rowNodes);
        if (table.getNumberOfRowsWithContent() == 1 && tableNode != null) {
            while (!tableNode.isRoot() && tableNode.getInitialSemanticType() != SemanticType.TABLE) {
                INode parentTableNode = tableNode.getParent();
                if (getNumberOfChildrenWithContent(parentTableNode) == 1) {
                    tableNode = parentTableNode;
                } else {
                    break;
                }
            }
        }
        if (tableNode != null) {
            while (TableUtils.isInitialTableNode(tableNode) && tableNode.getInitialSemanticType() != SemanticType.TABLE &&
                    !tableNode.isRoot() && tableNode.getParent().getChildren().size() == 1) {
                tableNode = tableNode.getParent();
            }
        }
        return tableNode;
    }

    private static int getNumberOfChildrenWithContent(INode node) {
        int numberOfChildrenWithContent = 0;
        for (INode child : node.getChildren()) {
            if (child.getSemanticType() != null) {
                numberOfChildrenWithContent++;
            }
        }
        return numberOfChildrenWithContent;
    }

    private INode getRowNode(TableBorderRow row) {
        Set<INode> cellNodes = new HashSet<>();
        for (int colNumber = 0; colNumber < row.getCells().length; colNumber++) {
            TableBorderCell cell = row.getCell(colNumber);
            if (cell.getRowNumber() == row.getRowNumber() && cell.getColNumber() == colNumber) {
                INode cellNode = getCellNode(cell);
                if (cellNode != null) {
                    cell.setNode(cellNode);
                    cellNodes.add(cellNode);
                }
            }
        }
        INode rowNode = findCommonParent(cellNodes);
        if (rowNode != null && row.getNumberOfCellWithContent() == 1) {
            while (!rowNode.isRoot() && rowNode.getInitialSemanticType() != SemanticType.TABLE_ROW) {
                INode parentRowNode = rowNode.getParent();
                if (getNumberOfChildrenWithContent(parentRowNode) == 1 &&
                        parentRowNode.getInitialSemanticType() != SemanticType.TABLE &&
                        parentRowNode.getInitialSemanticType() != SemanticType.TABLE_BODY &&
                        parentRowNode.getInitialSemanticType() != SemanticType.TABLE_FOOTER &&
                        parentRowNode.getInitialSemanticType() != SemanticType.TABLE_HEADER) {
                    rowNode = parentRowNode;
                } else {
                    break;
                }
            }
        }
        return rowNode;
    }

    private static boolean isHeaderCell(INode cellNode, TableBorderCell cell, TableBorder table) {
        if (cellNode.getInitialSemanticType() != SemanticType.TABLE_HEADER) {
            return false;
        }
        if (cell.getColNumber() == 0 || cell.getRowNumber() == 0) {
            return true;
        }
        for (int rowNumber = cell.getRowNumber(); rowNumber < cell.getRowNumber() + cell.getRowSpan(); rowNumber++) {
            if (table.getRow(rowNumber).getCell(cell.getColNumber() - 1).getSemanticType() == SemanticType.TABLE_HEADER) {
                return true;
            }
        }
        for (int colNumber = cell.getColNumber(); colNumber < cell.getColNumber() + cell.getColSpan(); colNumber++) {
            if (table.getRow(cell.getRowNumber() - 1).getCell(colNumber).getSemanticType() == SemanticType.TABLE_HEADER) {
                return true;
            }
        }
        return false;
    }

    private INode getCellNode(TableBorderCell cell) {
        Set<INode> tableLeafNodes = new HashSet<>();
        for (TableToken token : cell.getContent()) {
            if (token.getNode() != null) {
                tableLeafNodes.add(token.getNode());
            }
        }
        return findCommonParent(tableLeafNodes);
    }

    private static INode findParent(INode node, int depth) {
        if (node != null) {
            while (node.getDepth() > depth) {
                node = node.getParent();
            }
            if (node.getDepth() == depth) {
                return node;
            }
        }
        return null;
    }

    private static SortedSet<INode> findParents(Set<INode> nodes, int depth) {
        SortedSet<INode> parents = new TreeSet<>(Comparator.comparing(INode::getIndex));
        for (INode node : nodes) {
            if (node != null) {
                parents.add(findParent(node, depth));
            }
        }
        return parents;
    }

    private static List<INode> findParents(List<INode> nodes, int depth) {
        return nodes.stream().map(node -> findParent(node, depth)).collect(Collectors.toList());
    }

    private static INode findCommonParent(Set<INode> nodes) {
        if (nodes.size() == 0) {
            return null;
        }
        if (nodes.size() == 1) {
            return nodes.iterator().next();
        }
        int minDepth = nodes.stream().map(INode::getDepth).min(Integer::compareTo).orElse(0);
        Set<INode> parents = new HashSet<>();
        for (INode node : nodes) {
            INode parent = node;
            while (parent.getDepth() > minDepth) {
                parent = parent.getParent();
            }
            parents.add(parent);
        }
        while (parents.size() > 1) {
            Set<INode> parentsSet = new HashSet<>();
            for (INode node : parents) {
                parentsSet.add(node.getParent());
            }
            parents = parentsSet;
        }
        return parents.iterator().next();
    }
}

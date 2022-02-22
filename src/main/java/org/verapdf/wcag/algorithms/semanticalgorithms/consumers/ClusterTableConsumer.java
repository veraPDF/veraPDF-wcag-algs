package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;
import org.verapdf.wcag.algorithms.entities.lists.ListElement;
import org.verapdf.wcag.algorithms.entities.lists.ListItem;
import org.verapdf.wcag.algorithms.entities.lists.PDFList;
import org.verapdf.wcag.algorithms.entities.tables.*;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableRecognitionArea;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableRecognizer;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClusterTableConsumer {

    private static final Logger LOGGER = Logger.getLogger(AccumulatedNodeConsumer.class.getCanonicalName());

    private TableRecognitionArea recognitionArea;
    private final List<Table> tables;
    private final List<PDFList> lists;

    public  ClusterTableConsumer() {
        tables = new ArrayList<>();
        lists = new ArrayList<>();
        init();
    }

    private void init() {
        recognitionArea = new TableRecognitionArea();
    }

    public List<Table> getTables() {
        return tables;
    }

    public List<PDFList> getLists() {
        return lists;
    }

    public void findTables(INode root) {
        acceptChildren(root);
        if (recognitionArea.isValid()) {
            List<INode> restNodes = new ArrayList<>(recognize());
            init();
            restNodes.add(root);
            for (INode restNode : restNodes) {
                accept(restNode);
            }
        }
        updateTreeWithRecognizedTables(root);
        updateTreeWithRecognizedLists(root);
    }

    private void acceptChildren(INode node) {
        if (node.getSemanticType() == SemanticType.TABLE) {
            INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
            if (accumulatedNode instanceof SemanticTable) {
                TableToken token = new TableToken(((SemanticTable)accumulatedNode).getTableBorder());
                accept(token);
                return;
            }
        }
        for (INode child : node.getChildren()) {
            acceptChildren(child);
            accept(child);
        }
    }

    private void accept(INode node) {
        if (node.getChildren().isEmpty()) {
            if (node instanceof SemanticTextNode) {

                SemanticTextNode textNode = (SemanticTextNode) node;
                for (TextColumn column : textNode.getColumns()) {
                    for (TextLine line : column.getLines()) {
                        for (TextChunk chunk : line.getTextChunks()) {

                            if (TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                                continue;
                            }

                            TableToken token = new TableToken(chunk, node);
                            accept(token);
                        }
                    }
                }
            } else if ((node instanceof SemanticImageNode)) {
                SemanticImageNode imageNode = (SemanticImageNode) node;

                TableToken token = new TableToken(imageNode.getImage(), imageNode);
                accept(token);
            }
        }
    }

    private void findTableBorder() {
        TableBorder tableBorder = StaticContainers.getTableBordersCollection().getTableBorder(recognitionArea.getBoundingBox());
        if (tableBorder != null) {
            recognitionArea.setTableBorder(tableBorder);
        }
    }

    private void accept(TableToken token) {
        if (recognitionArea.addTokenToRecognitionArea(token) && recognitionArea.getTableBorder() == null) {
            findTableBorder();
        }
        if (recognitionArea.isComplete()) {
            List<INode> restNodes = new ArrayList<>();
            if (recognitionArea.isValid()) {
                restNodes.addAll(recognize());
            }
            init();

            restNodes.add(token.getNode());
            for (INode restNode : restNodes) {
                accept(restNode);
            }
        }
    }

    private List<INode> recognize() {
        TableRecognizer recognizer = new TableRecognizer(recognitionArea);
        recognizer.recognize();
        Table recognizedTable = recognizer.getTable();

        if (recognizedTable != null) {
            if (recognizedTable.getTableBorder() == null && ListUtils.isList(recognizedTable)) {
                lists.add(new PDFList(recognizedTable));
            } else {
                tables.add(recognizedTable);
            }
            return recognizedTable.getRestNodes();
        }

        return new ArrayList<INode>();
    }

    /**
     * main algorithm complexity for each table: max{ O(t * h), O(N) },
     * where N - number of nodes, h - tree height, t - number of table cells
     * node info initialization: O(M), where M - tree size.
     * The worst case is when all table roots are the same node - tree root
     */
    private void updateTreeWithRecognizedTables(INode root) {
        initTreeNodeInfo(root);
        for (Table table : tables) {
            INode tableRoot = updateTreeWithRecognizedTable(table, root);

            if (tableRoot != null) {
                if (updateNode(tableRoot, table.getId(), SemanticType.TABLE,
                        table.getTableBorder() != null, table.getBoundingBox())) {
                    detectTableCaptions(table.getBoundingBox(), tableRoot);
                }
            }
        }
    }

    public static void detectTableCaptions(BoundingBox tableBoundingBox, INode tableRoot) {
        detectTableCaption(tableBoundingBox, tableRoot.getPreviousNeighbor());
        detectTableCaption(tableBoundingBox, tableRoot.getNextNeighbor());
    }

    private static void detectTableCaption(BoundingBox tableBoundingBox, INode node) {
        if (node == null) {
            return;
        }
        if (node.getSemanticType() == SemanticType.HEADING ||
                node.getSemanticType() == SemanticType.NUMBER_HEADING) {
            return;
        }
        INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
        double captionProbability = NodeUtils.tableCaptionProbability(accumulatedNode, tableBoundingBox);
        if (captionProbability >= TableUtils.MERGE_PROBABILITY_THRESHOLD) {
            StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticCaption((SemanticTextNode) accumulatedNode),
                    captionProbability * node.getCorrectSemanticScore(), SemanticType.CAPTION);
        }
    }

    private INode updateTreeWithRecognizedTable(Table table, INode root) {
        Map<SemanticType, Set<INode>> rowNodes = new HashMap<>();
        rowNodes.put(SemanticType.TABLE_HEADERS, new HashSet<>());
        rowNodes.put(SemanticType.TABLE_BODY, new HashSet<>());
        for (int i = 0; i < table.getRows().size(); i++) {
            TableRow row = table.getRows().get(i);
            INode rowNode = updateTreeWithRecognizedTableRow(table, row, i == 0 ? null : table.getRows().get(i - 1));

            if (rowNode != null) {
                updateNode(rowNode, table.getId(), SemanticType.TABLE_ROW, table.getTableBorder() != null,
                        table.getBoundingBox());

                SemanticType rowType = row.getSemanticType();
                Set<INode> nodes = rowNodes.get(rowType);
                if (nodes != null) {
                    nodes.add(rowNode);
                }
            }
        }

        Set<INode> headersNodes = rowNodes.get(SemanticType.TABLE_HEADERS);
        if (headersNodes.size() == 1 && headersNodes.equals(rowNodes.get(SemanticType.TABLE_BODY))) {
            return headersNodes.iterator().next();
        }

        Set<INode> localRoots = new HashSet<>();
        for (Map.Entry<SemanticType, Set<INode>> entry : rowNodes.entrySet()) {
            SemanticType type = entry.getKey();
            Set<INode> rows = entry.getValue();

            INode localRoot = findLocalRoot(rows);
            if (localRoot != null) {
                updateNode(localRoot, table.getId(), type, table.getTableBorder() != null,
                        table.getBoundingBox());
                localRoots.add(localRoot);
            }
        }
        if (localRoots.isEmpty()) {
            return null;
        }
        if (localRoots.size() == 1) {
            return localRoots.iterator().next();
        }

        List<INode> localRootsList = localRoots.stream().collect(Collectors.toList());
        if (localRootsList.get(0).getNodeInfo().depth < localRootsList.get(1).getNodeInfo().depth &&
                isAncestorFor(localRootsList.get(0), localRootsList.get(1))) {
            return localRootsList.get(0);
        } else if (localRootsList.get(1).getNodeInfo().depth < localRootsList.get(0).getNodeInfo().depth &&
                isAncestorFor(localRootsList.get(1), localRootsList.get(0))) {
            return localRootsList.get(1);
        } else {
            return findLocalRoot(localRoots);
        }
    }

    private INode updateTreeWithRecognizedTableRow(Table table, TableRow row, TableRow previousRow) {
        Long id = table.getId();
        Map<INode, Integer> cellNodes = new HashMap<>();
        for (int i = 0; i < row.getCells().size(); i++) {
            INode cellNode = updateTreeWithRecognizedCell(row.getCells().get(i));

            if (cellNode != null) {
                cellNodes.put(cellNode, i);
            }
        }

        INode rowNode = findLocalRoot(cellNodes.keySet());
        if (row.getNumberOfCellsWithContent() == 1) {
            if (rowNode.getParent() != null && rowNode.getParent().getInitialSemanticType() == SemanticType.TABLE_ROW) {
                rowNode = rowNode.getParent();
            }
        }
        for (Map.Entry<INode, Integer> entry : cellNodes.entrySet()) {
            INode cellNode = entry.getKey();
            while (cellNode.getParent() != null && cellNode.getParent() != rowNode &&
                    cellNode.getParent().getChildren().size() == 1) {
                cellNode = cellNode.getParent();
            }
            Integer colNumber = entry.getValue();
            SemanticType cellType = isHeaderCell(cellNode, colNumber, colNumber == 0 ? null :
                    row.getCells().get(colNumber - 1), previousRow) ?
                    SemanticType.TABLE_HEADER : SemanticType.TABLE_CELL;
            if (updateNode(cellNode, id, cellType, table.getTableBorder() != null, table.getBoundingBox())) {
                row.getCells().get(colNumber).setSemanticType(cellType);
            }
        }

        return rowNode;
    }

    private boolean isHeaderCell(INode cellNode, Integer columnNumber, TableCell previousCell, TableRow previousRow) {
        if (cellNode.getInitialSemanticType() != SemanticType.TABLE_HEADER) {
            return false;
        }
        if (previousCell == null || previousRow == null || previousCell.getSemanticType() == SemanticType.TABLE_HEADER) {
            return true;
        }
        if (columnNumber < previousRow.getCells().size() &&
                previousRow.getCells().get(columnNumber).getSemanticType() == SemanticType.TABLE_HEADER) {
            return true;
        }
        return false;
    }

    private INode updateTreeWithRecognizedCell(TableCell cell) {
        Set<INode> tableLeafNodes = new HashSet<>();
        for (TableTokenRow tokenRow : cell.getContent()) {
            for (TextChunk chunk : tokenRow.getTextChunks()) {
                if (chunk instanceof TableToken) {
                    TableToken token = (TableToken) chunk;
                    if (token.getNode() != null) {
                        tableLeafNodes.add(token.getNode());
                    }
                }
            }
        }
        return findLocalRoot(tableLeafNodes);
    }

    private void updateTreeWithRecognizedLists(INode root) {
        initTreeNodeInfo(root);
        for (PDFList list : lists) {
            INode listRoot = updateTreeWithRecognizedList(list);
            if (listRoot != null) {
                updateNode(listRoot, list.getId(), SemanticType.LIST, false, list.getBoundingBox());
            }
        }
    }

    private INode updateTreeWithRecognizedList(PDFList list) {
        Set<INode> nodes = new HashSet<>();
        for (ListItem item : list.getListItems()) {
            INode itemNode = updateTreeWithRecognizedListItem(item, list);
            if (itemNode != null) {
                updateNode(itemNode, list.getId(), SemanticType.LIST_ITEM, false, list.getBoundingBox());
                nodes.add(itemNode);
            }
        }
        if (nodes.size() == 1) {
            return nodes.iterator().next();
        }
        return findLocalRoot(nodes);
    }

    private boolean updateNode(INode node, Long id, SemanticType semanticType, boolean hasTableBorder,
                               BoundingBox boundingBox) {
        if ((((ListUtils.isListNode(node) && !hasTableBorder) || TableUtils.isTableNode(node)) &&
                node.getRecognizedStructureId() != id) || !containsNode(boundingBox, node.getBoundingBox(),
                TableBorder.TABLE_BORDER_EPSILON, TableBorder.TABLE_BORDER_EPSILON)) {
            node.setRecognizedStructureId(null);
            return false;
        }
        node.setRecognizedStructureId(id);
        node.setSemanticType(semanticType);
        node.setCorrectSemanticScore(1.0);
        return true;
    }

    private boolean containsNode(BoundingBox boundingBox, BoundingBox nodeBoundingBox,
                             double horizontalOffset, double verticalOffset) {
        if (nodeBoundingBox instanceof MultiBoundingBox) {
            for (BoundingBox box : ((MultiBoundingBox)nodeBoundingBox).getBoundingBoxes()) {
                if (boundingBox.getPageNumber() <= box.getPageNumber() && boundingBox.getLastPageNumber() >=
                        box.getLastPageNumber() && !boundingBox.contains(box, horizontalOffset, verticalOffset)) {
                    return false;
                }
            }
            return true;
        }
        return boundingBox.contains(nodeBoundingBox, horizontalOffset, verticalOffset);
    }

    private INode updateTreeWithRecognizedListItem(ListItem item, PDFList list) {
        Map<INode, SemanticType> elementsNodes = new HashMap<>();
        INode labelNode = updateTreeWithRecognizedListElement(item.getLabel());
        if (labelNode != null) {
            elementsNodes.put(labelNode, item.getLabel().getSemanticType());
        }
        INode bodyNode = updateTreeWithRecognizedListElement(item.getBody());
        if (bodyNode != null) {
            elementsNodes.put(bodyNode, item.getBody().getSemanticType());
        }

        INode itemNode = findLocalRoot(elementsNodes.keySet());
        while (itemNode.getParent() != null && itemNode.getParent().getChildren().size() == 1) {
            itemNode = itemNode.getParent();
        }

        for (Map.Entry<INode, SemanticType> entry : elementsNodes.entrySet()) {
            INode elementNode = entry.getKey();
            while (elementNode.getParent() != null && elementNode.getParent() != itemNode &&
                    elementNode.getParent().getChildren().size() == 1) {
                elementNode = elementNode.getParent();
            }
            updateNode(elementNode, list.getId(), entry.getValue(), false, list.getBoundingBox());
        }

        return itemNode;
    }

    private INode updateTreeWithRecognizedListElement(ListElement listElement) {
        Set<INode> tableLeafNodes = new HashSet<>();
        for (TableTokenRow tokenRow : listElement.getContent()) {
            for (TextChunk chunk : tokenRow.getTextChunks()) {
                if (chunk instanceof TableToken) {
                    TableToken token = (TableToken) chunk;
                    if (token.getNode() != null) {
                        tableLeafNodes.add(token.getNode());
                    }
                }
            }
        }
        return findLocalRoot(tableLeafNodes);
    }

    private INode findLocalRoot(Set<INode> nodes) {

        INode localRoot = null;
        for (INode node : nodes) {

            if (node.isRoot()) {
                localRoot = node;
                break;
            }
            if (localRoot == null) {
                localRoot = node.getParent();
            }

            while (!node.isRoot()) {
                INode parent = node.getParent();
                NodeInfo parentInfo = parent.getNodeInfo();

                parentInfo.counter++;
                if (parentInfo.counter > 1) {
                    if (parentInfo.depth < localRoot.getNodeInfo().depth) {
                        localRoot = parent;
                    }
                    break;
                }
                node = parent;
            }
        }
        initTreeCounters(localRoot);

        return localRoot;
    }

    private boolean isAncestorFor(INode first, INode second) {
        while (!second.isRoot()) {
            second = second.getParent();
            if (second == first) {
                return true;
            }
        }
        return false;
    }

    private void initTreeCounters(INode root) {
        if (root == null) {
            return;
        }
        Stack<INode> nodeStack = new Stack<>();
        nodeStack.push(root);

        while (!nodeStack.isEmpty()) {
            INode node = nodeStack.pop();
            NodeInfo nodeInfo = node.getNodeInfo();

            nodeInfo.counter = 0;
            for (INode child : node.getChildren()) {
                nodeStack.push(child);
            }
        }

        while (!root.isRoot()) {
            root = root.getParent();
            root.getNodeInfo().counter = 0;
        }
    }

    private void initTreeNodeInfo(INode root) {
        Stack<INode> nodeStack = new Stack<>();
        nodeStack.push(root);

        while (!nodeStack.isEmpty()) {
            INode node = nodeStack.pop();
            NodeInfo nodeInfo = node.getNodeInfo();

            if (node.isRoot()) {
                nodeInfo.depth = 0;
            } else {
                nodeInfo.depth = node.getParent().getNodeInfo().depth + 1;
            }
            nodeInfo.counter = 0;

            for (INode child : node.getChildren()) {
                nodeStack.push(child);
            }
        }
    }
}

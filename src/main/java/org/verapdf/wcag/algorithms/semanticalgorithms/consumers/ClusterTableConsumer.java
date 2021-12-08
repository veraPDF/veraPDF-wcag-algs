package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.lists.ListElement;
import org.verapdf.wcag.algorithms.entities.lists.ListItem;
import org.verapdf.wcag.algorithms.entities.lists.PDFList;
import org.verapdf.wcag.algorithms.entities.maps.AccumulatedNodeMapper;
import org.verapdf.wcag.algorithms.entities.tables.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableRecognitionArea;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableRecognizer;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClusterTableConsumer implements Consumer<INode> {

    private static final Logger LOGGER = Logger.getLogger(AccumulatedNodeConsumer.class.getCanonicalName());

    private final AccumulatedNodeMapper accumulatedNodeMapper;
    private TableRecognitionArea recognitionArea;
    private final List<Table> tables;
    private final List<PDFList> lists;
    private final TableBordersCollection tableBorders;

    public  ClusterTableConsumer(TableBordersCollection tableBorders, AccumulatedNodeMapper accumulatedNodeMapper) {
        tables = new ArrayList<>();
        lists = new ArrayList<>();
        init();
        this.tableBorders = tableBorders;
        this.accumulatedNodeMapper = accumulatedNodeMapper;
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

    @Override
    public void accept(INode node) {

        if (node.getChildren().isEmpty()) {
            if (node instanceof SemanticTextNode) {

                SemanticTextNode textNode = (SemanticTextNode) node;
                for (TextLine line : textNode.getLines()) {
                    for (TextChunk chunk : line.getTextChunks()) {

                        if (TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                            continue;
                        }

                        TableToken token = new TableToken(chunk, node);
                        accept(token);
                    }
                }
            } else if ((node instanceof SemanticImageNode)) {
                SemanticImageNode imageNode = (SemanticImageNode) node;

                TableToken token = new TableToken(imageNode.getImage(), imageNode);
                accept(token);
            }
        }

        if (node.isRoot()) {
            if (recognitionArea.isValid()) {

                List<INode> restNodes = new ArrayList<>(recognize());
                init();

                restNodes.add(node);
                for (INode restNode : restNodes) {
                    accept(restNode);
                }
            }
            updateTreeWithRecognizedTables(node);
            updateTreeWithRecognizedLists(node);
        }
    }

    private void findTableBorder() {
        TableBorder tableBorder = tableBorders.getTableBorder(recognitionArea.getBoundingBox());
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
                if ((TableUtils.isTableNode(tableRoot) || (ListUtils.isListNode(tableRoot) &&
                        table.getTableBorder() == null)) && tableRoot.getRecognizedStructureId() != table.getId()) {
                    tableRoot.setRecognizedStructureId(null);
                } else {
                    tableRoot.setRecognizedStructureId(table.getId());
                    tableRoot.setSemanticType(SemanticType.TABLE);
                    tableRoot.setCorrectSemanticScore(1.0);
                    detectTableCaptions(table, tableRoot);
                }
            }
        }
    }

    private void detectTableCaptions(Table table, INode tableRoot) {
        detectTableCaption(table, tableRoot.getPreviousNeighbor());
        detectTableCaption(table, tableRoot.getNextNeighbor());
    }

    private void detectTableCaption(Table table, INode node) {
        if (node == null) {
            return;
        }
        if (node.getSemanticType() == SemanticType.HEADING ||
                node.getSemanticType() == SemanticType.NUMBER_HEADING) {
            return;
        }
        INode accumulatedNode = accumulatedNodeMapper.get(node);
        double captionProbability = NodeUtils.tableCaptionProbability(accumulatedNode, table);
        if (captionProbability >= TableUtils.MERGE_PROBABILITY_THRESHOLD) {
            accumulatedNodeMapper.updateNode(node, new SemanticCaption((SemanticTextNode) accumulatedNode),
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
                if (((ListUtils.isListNode(rowNode) && table.getTableBorder() == null) ||
                        TableUtils.isTableNode(rowNode)) && rowNode.getRecognizedStructureId() != table.getId()) {
                    rowNode.setRecognizedStructureId(null);
                } else {
                    rowNode.setRecognizedStructureId(table.getId());
                    rowNode.setSemanticType(SemanticType.TABLE_ROW);
                    rowNode.setCorrectSemanticScore(1.0);
                }

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
                if ((TableUtils.isTableNode(localRoot) || (ListUtils.isListNode(localRoot) &&
                        table.getTableBorder() == null)) && localRoot.getRecognizedStructureId() != table.getId()) {
                    localRoot.setRecognizedStructureId(null);
                } else {
                    localRoot.setRecognizedStructureId(table.getId());
                    localRoot.setSemanticType(type);
                    localRoot.setCorrectSemanticScore(1.0);
                }
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

        for (Map.Entry<INode, Integer> entry : cellNodes.entrySet()) {
            INode cellNode = entry.getKey();
            while (cellNode.getParent() != null && cellNode.getParent() != rowNode && cellNode.getParent().getChildren().size() == 1) {
                cellNode = cellNode.getParent();
            }

            if (((ListUtils.isListNode(cellNode) && table.getTableBorder() == null) ||
                    TableUtils.isTableNode(cellNode)) && cellNode.getRecognizedStructureId() != id) {
                cellNode.setRecognizedStructureId(null);
            } else {
                cellNode.setRecognizedStructureId(id);
                if (isHeaderCell(cellNode, entry.getValue(),
                        entry.getValue() == 0 ? null : row.getCells().get(entry.getValue() - 1), previousRow)) {
                    cellNode.setSemanticType(SemanticType.TABLE_HEADER);
                    row.getCells().get(entry.getValue()).setSemanticType(SemanticType.TABLE_HEADER);
                } else {
                    cellNode.setSemanticType(SemanticType.TABLE_CELL);
                    row.getCells().get(entry.getValue()).setSemanticType(SemanticType.TABLE_CELL);
                }
                cellNode.setCorrectSemanticScore(1.0);
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
                if ((ListUtils.isListNode(listRoot) || TableUtils.isTableNode(listRoot)) && listRoot.getRecognizedStructureId() != list.getId()) {
                    listRoot.setRecognizedStructureId(null);
                } else {
                    listRoot.setRecognizedStructureId(list.getId());
                    listRoot.setSemanticType(SemanticType.LIST);
                    listRoot.setCorrectSemanticScore(1.0);
                }
            }
        }
    }

    private INode updateTreeWithRecognizedList(PDFList list) {
        Set<INode> nodes = new HashSet<>();
        for (ListItem item : list.getListItems()) {
            INode itemNode = updateTreeWithRecognizedListItem(item, list.getId());
            if (itemNode != null) {
                if ((ListUtils.isListNode(itemNode) || TableUtils.isTableNode(itemNode)) && itemNode.getRecognizedStructureId() != list.getId()) {
                    itemNode.setRecognizedStructureId(null);
                } else {
                    itemNode.setRecognizedStructureId(list.getId());
                    itemNode.setSemanticType(SemanticType.LIST_ITEM);
                    itemNode.setCorrectSemanticScore(1.0);
                }
                nodes.add(itemNode);
            }
        }
        if (nodes.size() == 1) {
            return nodes.iterator().next();
        }
        return findLocalRoot(nodes);
    }

    private INode updateTreeWithRecognizedListItem(ListItem item, Long id) {
        Map<INode, SemanticType> elementsNodes = new HashMap<>();
        INode labelNode = updateTreeWithRecognizedListElement(item.getLabel());
        if (labelNode != null) {
            elementsNodes.put(labelNode, item.getLabel().getSemanticType());
        }
        INode bodyNode = updateTreeWithRecognizedListElement(item.getBody());
        if (bodyNode != null) {
            elementsNodes.put(bodyNode, item.getBody().getSemanticType());
        }

        if (labelNode != null && bodyNode != null && labelNode.equals(bodyNode)) {
            return labelNode;
        }

        INode itemNode = findLocalRoot(elementsNodes.keySet());

        for (Map.Entry<INode, SemanticType> entry : elementsNodes.entrySet()) {
            INode elementNode = entry.getKey();
            while (elementNode.getParent() != null && elementNode.getParent() != itemNode && elementNode.getParent().getChildren().size() == 1) {
                elementNode = elementNode.getParent();
            }

            if ((ListUtils.isListNode(elementNode) || TableUtils.isTableNode(elementNode)) && elementNode.getRecognizedStructureId() != id) {
                elementNode.setRecognizedStructureId(null);
            } else {
                elementNode.setRecognizedStructureId(id);
                elementNode.setSemanticType(entry.getValue());
                elementNode.setCorrectSemanticScore(1.0);
            }
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

package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextInfoChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.lists.ListElement;
import org.verapdf.wcag.algorithms.entities.lists.ListItem;
import org.verapdf.wcag.algorithms.entities.lists.PDFList;
import org.verapdf.wcag.algorithms.entities.tables.*;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableCluster;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableRecognitionArea;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableRecognizer;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.*;

import java.util.*;
import java.util.logging.Logger;

public class ClusterTableConsumer extends WCAGConsumer {

    private static final Logger LOGGER = Logger.getLogger(ClusterTableConsumer.class.getCanonicalName());

    private TableRecognitionArea recognitionArea;
    private final List<Table> tables;
    private final List<PDFList> lists;

    static {
        wcagProgressStatus = WCAGProgressStatus.TABLE_DETECTION;
    }

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
                accept(token, node);
            }
            return;
        } else if (node.getSemanticType() == SemanticType.PARAGRAPH) {
            INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
            if (accumulatedNode instanceof SemanticParagraph) {
                SemanticParagraph paragraph = (SemanticParagraph)accumulatedNode;
                if (paragraph.isEmpty() || paragraph.isSpaceNode()) {
                    return;
                }
                if (paragraph.getColumnsNumber() == 1 && paragraph.getLinesNumber() != 1) {
                    TableCluster cluster = new TableCluster(paragraph, node);
                    accept(cluster, node);
                    return;
                }
            }
        } else if (node.getSemanticType() == SemanticType.LIST) {
            INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
            if (accumulatedNode instanceof SemanticList) {
                SemanticList list = (SemanticList)accumulatedNode;
                if (list.getNumberOfListColumns() == 1 && node.getChildren().size() ==
                        list.getNumberOfListItemsAndLists() && list.getNumberOfListItems() > 1) {
                    TableCluster cluster = new TableCluster((SemanticTextNode)accumulatedNode, node);
                    accept(cluster, node);
                    return;
                }
            }
        } else if (node.getSemanticType() == SemanticType.TABLE_OF_CONTENT ||
                node.getSemanticType() == SemanticType.TABLE_OF_CONTENT_ITEM) {
            return;
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
                            accept(token, node);
                        }
                    }
                }
            } else if ((node instanceof SemanticImageNode)) {
                SemanticImageNode imageNode = (SemanticImageNode) node;

                TableToken token = new TableToken(imageNode.getImage(), imageNode);
                accept(token, node);
            }
        }
    }

    private void findTableBorder() {
        TableBorder tableBorder = StaticContainers.getTableBordersCollection().getTableBorder(recognitionArea.getBoundingBox());
        if (tableBorder != null) {
            recognitionArea.setTableBorder(tableBorder);
        }
    }

    private void accept(TextInfoChunk token, INode node) {
        if (recognitionArea.addTokenToRecognitionArea(token) && recognitionArea.getTableBorder() == null) {
            findTableBorder();
        }
        if (recognitionArea.isComplete()) {
            List<INode> restNodes = new ArrayList<>();
            if (recognitionArea.isValid()) {
                restNodes.addAll(recognize());
            }
            init();

            restNodes.add(node);
            for (INode restNode : restNodes) {
                acceptChildren(restNode);
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
            	PDFList list = new PDFList(recognizedTable);
                lists.add(list);
            } else if (checkTable(recognizedTable)) {
                tables.add(recognizedTable);
            }
            return recognizedTable.getRestNodes();
        }

        return new ArrayList<INode>();
    }

    private boolean checkTable(Table recognizedTable) {
        for (int rowNumber = 0; rowNumber < recognizedTable.getNumberOfRows(); rowNumber++) {
            if (recognizedTable.getRows().get(rowNumber).getNumberOfCellsWithContent() < 2) {
                return false;
            }
        }
        if (!checkTableCellsLeftAndRight(recognizedTable) || !checkTableCellsTopsAndBottom(recognizedTable)) {
            return false;
        }
        return true;
    }

    private static boolean checkTableCellsTopsAndBottom(Table recognizedTable) {
        Double previousMinBottom = null;
        Double previousMinTop = null;
        for (int rowNumber = 0; rowNumber < recognizedTable.getNumberOfRows(); rowNumber++) {
            TableRow row = recognizedTable.getRows().get(rowNumber);
            Double maxBottom = null;
            Double minBottom = null;
            Double maxTop = null;
            Double minTop = null;
            for (int colNumber = 0; colNumber < recognizedTable.getNumberOfColumns(); colNumber++) {
                TableCell cell = row.getCells().get(colNumber);
                if (cell.getContent().isEmpty()) {
                    continue;
                }
                BoundingBox cellBoundingBox = cell.getBoundingBox();
                if (maxBottom == null || cellBoundingBox.getBottomY() > maxBottom) {
                    maxBottom = cellBoundingBox.getBottomY();
                }
                if (minBottom == null || cellBoundingBox.getBottomY() < minBottom) {
                    minBottom = cellBoundingBox.getBottomY();
                }
                if (maxTop == null || cellBoundingBox.getTopY() > maxTop) {
                    maxTop = cellBoundingBox.getTopY();
                }
                if (minTop == null || cellBoundingBox.getTopY() < minTop) {
                    minTop = cellBoundingBox.getTopY();
                }
            }
            if (previousMinBottom != null && maxBottom != null && previousMinBottom < maxBottom) {
                return false;
            }
            if (previousMinTop != null && minTop != null && previousMinTop < minTop) {
                return false;
            }
            if (minBottom != null) {
                previousMinBottom = minBottom;
            }
            if (minTop != null) {
                previousMinTop = minTop;
            }
        }
        return true;
    }

    private static boolean checkTableCellsLeftAndRight(Table recognizedTable) {
        Double previousMaxRight = null;
        Double previousMaxLeft = null;
        for (int colNumber = 0; colNumber < recognizedTable.getNumberOfColumns(); colNumber++) {
            Double maxLeft = null;
            Double minLeft = null;
            Double maxRight = null;
            Double minRight = null;
            for (int rowNumber = 0; rowNumber < recognizedTable.getNumberOfRows(); rowNumber++) {
                TableCell cell = recognizedTable.getRows().get(rowNumber).getCells().get(colNumber);
                if (cell.getContent().isEmpty()) {
                    continue;
                }
                BoundingBox cellBoundingBox = cell.getBoundingBox();
                if (maxRight == null || cellBoundingBox.getRightX() > maxRight) {
                    maxRight = cellBoundingBox.getRightX();
                }
                if (minRight == null || cellBoundingBox.getRightX() < minRight) {
                    minRight = cellBoundingBox.getRightX();
                }
                if (maxLeft == null || cellBoundingBox.getLeftX() > maxLeft) {
                    maxLeft = cellBoundingBox.getLeftX();
                }
                if (minLeft == null || cellBoundingBox.getLeftX() < minLeft) {
                    minLeft = cellBoundingBox.getLeftX();
                }
            }
            if (previousMaxRight != null && minRight != null && previousMaxRight > minRight) {
                return false;
            }
            if (previousMaxLeft != null && minLeft != null && previousMaxLeft > minLeft) {
                return false;
            }
            if (maxRight != null) {
                previousMaxRight = maxRight;
            }
            if (maxLeft != null) {
                previousMaxLeft = maxLeft;
            }
        }
        return true;
    }

    /**
     * main algorithm complexity for each table: max{ O(t * h), O(N) },
     * where N - number of nodes, h - tree height, t - number of table cells
     * node info initialization: O(M), where M - tree size.
     * The worst case is when all table roots are the same node - tree root
     */
    private void updateTreeWithRecognizedTables(INode root) {
        initTreeNodeInfo(root);
        List<INode> tableRoots = new ArrayList<>(tables.size());
        for (Table table : tables) {
            tableRoots.add(updateTreeWithRecognizedTable(table, root));
        }
        Integer firstTablePartIndex = null;
        for (int i = 0; i < tables.size(); i++) {
            Table table = tables.get(i);
            INode tableRoot = tableRoots.get(i);
            if (tableRoot == null) {
                firstTablePartIndex = null;
                continue;
            }
            if (firstTablePartIndex != null) {
                if (tables.get(i - 1).getPageNumber() + 1 != table.getPageNumber()) {
                    firstTablePartIndex = null;
                } else if (tableRoots.get(i - 1) != tableRoot) {
                    firstTablePartIndex = null;
                } else if (!isNodeInsideTable(tableRoot, table.getId(), table.getBoundingBox(), SemanticType.TABLE)) {
                    firstTablePartIndex = null;
                    continue;
                } else if (tableRoot.getLastPageNumber() == null || table.getPageNumber().equals(tableRoot.getLastPageNumber())) {
                    updateTableNode(table, tableRoot);
                    for (int index = firstTablePartIndex; index < i; index++) {
                        StaticContainers.getIdMapper().put(tables.get(index).getId(), table.getId());
                        tables.get(index).setId(table.getId());
                    }
                }
            }
            if (firstTablePartIndex != null) {
                continue;
            }
            if (tableRoot.getPageNumber() == null || table.getPageNumber().equals(tableRoot.getPageNumber())) {
                if (tableRoot.getPageNumber() != null && tableRoot.getLastPageNumber() > table.getPageNumber()) {
                    if (isNodeInsideTable(tableRoot, table.getId(), table.getBoundingBox(), SemanticType.TABLE)) {
                        firstTablePartIndex = i;
                    }
                } else {
                    if (isNodeInsideTable(tableRoot, table.getId(), table.getBoundingBox(), SemanticType.TABLE)) {
                        updateTableNode(table, tableRoot);
                    }
                }
            }
        }
    }

    private void updateTableNode(Table table, INode tableRoot) {
        if (table.getBodyNode() != null) {
            updateNode(table.getBodyNode(), table.getId(), SemanticType.TABLE_BODY,
                    table.getTableBorder() != null, table.getBoundingBox());
        }
        if (updateNode(tableRoot, table.getId(), SemanticType.TABLE,
                table.getTableBorder() != null, table.getBoundingBox())) {
            detectTableCaptions(table.getBoundingBox(), tableRoot);
        }
    }

    public static void detectTableCaptions(BoundingBox tableBoundingBox, INode tableRoot) {
        INode previousNode = tableRoot.getPreviousNeighbor();
        INode nextNode = tableRoot.getNextNeighbor();
        double previousCaptionProbability = CaptionUtils.tableCaptionProbability(previousNode, tableBoundingBox);
        double nextCaptionProbability = CaptionUtils.tableCaptionProbability(nextNode, tableBoundingBox);
        double captionProbability;
        INode captionNode;
        if (previousCaptionProbability > nextCaptionProbability) {
            captionProbability = previousCaptionProbability;
            captionNode = previousNode;
        } else {
            captionProbability = nextCaptionProbability;
            captionNode = nextNode;
        }
        if (captionProbability >= TableUtils.MERGE_PROBABILITY_THRESHOLD) {
            StaticContainers.getAccumulatedNodeMapper().updateNode(captionNode,
                    new SemanticCaption((SemanticTextNode) StaticContainers.getAccumulatedNodeMapper().get(captionNode)),
                    captionProbability * captionNode.getCorrectSemanticScore(), SemanticType.CAPTION);
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
                if (type == SemanticType.TABLE_BODY && localRoot.getPageNumber() != localRoot.getLastPageNumber()) {
                    table.setBodyNode(localRoot);
                } else {
                    updateNode(localRoot, table.getId(), type, table.getTableBorder() != null,
                            table.getBoundingBox());
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

        List<INode> localRootsList = new ArrayList<>(localRoots);
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
                    !hasOtherChildrenWithContents(cellNode.getParent(), cellNode)) {
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
                        if (token.getNode().getChildren().isEmpty()) {
                            tableLeafNodes.add(token.getNode());
                        } else {
                            tableLeafNodes.addAll(token.getNode().getChildren());
                        }
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
                updateNode(listRoot, list.getRecognizedStructureId(), SemanticType.LIST, false, list.getBoundingBox());
            }
        }
    }

    private INode updateTreeWithRecognizedList(PDFList list) {
        Set<INode> nodes = new HashSet<>();
        boolean hasTaggedListItems = true;
        for (ListItem item : list.getListItems()) {
            INode itemNode = updateTreeWithRecognizedListItem(item, list);
            if (itemNode != null) {
                updateNode(itemNode, list.getRecognizedStructureId(), SemanticType.LIST_ITEM, false, list.getBoundingBox());
                nodes.add(itemNode);
                if (itemNode.getInitialSemanticType() != SemanticType.LIST_ITEM) {
                    hasTaggedListItems = false;
                }
            }
        }
        if (!hasTaggedListItems) {
            StaticContainers.getListsCollection().add(list);
        }
        if (nodes.size() == 1) {
            return nodes.iterator().next();
        }
        return findLocalRoot(nodes);
    }

    private boolean updateNode(INode node, Long id, SemanticType semanticType, boolean hasTableBorder,
                               BoundingBox boundingBox) {
        if ((((ListUtils.isListNode(node) && !hasTableBorder) || TableUtils.isTableNode(node)) &&
            node.getRecognizedStructureId() != id) || (semanticType != SemanticType.TABLE && !isNodeInsideTable(node,
                id, boundingBox, semanticType))) {
                node.setRecognizedStructureId(null);
            return false;
        }
        node.setRecognizedStructureId(id);
        node.setSemanticType(semanticType);
        node.setCorrectSemanticScore(1.0);
        return true;
    }

    //optimize
    public static boolean isNodeInsideTable(INode node, Long id, BoundingBox boundingBox, SemanticType semanticType) {
        if (node.getRecognizedStructureId() == id) {
            return true;
        }
        if (node instanceof SemanticFigure) {
            return true;
        }
        if (node instanceof SemanticTextNode) {
            SemanticTextNode textNode = (SemanticTextNode) node;
            return isTextNodeInsideTable(textNode, boundingBox);
        }
        if (node instanceof SemanticImageNode) {
            return boundingBox.getPageNumber() > node.getPageNumber() || boundingBox.getLastPageNumber() <
                    node.getLastPageNumber() || boundingBox.contains(node.getBoundingBox(),
                    TableBorder.TABLE_BORDER_EPSILON, TableBorder.TABLE_BORDER_EPSILON);
        }
        if (node.getPageNumber() != null && semanticType != SemanticType.TABLE &&
                semanticType != SemanticType.TABLE_BODY && (node.getPageNumber() < boundingBox.getPageNumber() ||
                node.getLastPageNumber() > boundingBox.getPageNumber())) {
                return false;
        }
        for (INode child : node.getChildren()) {
            if (!isNodeInsideTable(child, id, boundingBox, semanticType)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isTextNodeInsideTable(SemanticTextNode textNode, BoundingBox boundingBox) {
        for (TextColumn column : textNode.getColumns()) {
            for (TextLine line : column.getLines()) {
                for (TextChunk chunk : line.getTextChunks()) {
                    if (!TextChunkUtils.isWhiteSpaceChunk(chunk) && boundingBox.getPageNumber() <= chunk.getPageNumber()
                            && boundingBox.getLastPageNumber() >= chunk.getLastPageNumber() &&
                            !boundingBox.contains(chunk.getBoundingBox(), TableBorder.TABLE_BORDER_EPSILON,
                                    TableBorder.TABLE_BORDER_EPSILON)) {
                        return false;
                    }
                }
            }
        }
        return true;
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
            updateNode(elementNode, list.getRecognizedStructureId(), entry.getValue(), false, list.getBoundingBox());
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

    private static boolean hasOtherChildrenWithContents(INode parent, INode node) {
        for (INode child : parent.getChildren()) {
            if (child == node || child instanceof SemanticFigure) {
                continue;
            } else if (child instanceof SemanticImageNode) {
                return true;
            } else if (child instanceof SemanticSpan) {
                SemanticSpan span = (SemanticSpan)child;
                if (!span.isSpaceNode() && !span.isEmpty()) {
                    return true;
                }
            } else if (hasOtherChildrenWithContents(child, null)) {
                return true;
            }
        }
        return false;
    }
}

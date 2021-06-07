package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.tables.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableRecognitionArea;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableRecognizer;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClusterTableConsumer implements Consumer<INode> {

    private static final Logger LOGGER = Logger.getLogger(AccumulatedNodeConsumer.class.getCanonicalName());

    private TableRecognitionArea recognitionArea;
    private List<Table> tables;

    public  ClusterTableConsumer() {
        tables = new ArrayList<>();
        init();
    }

    private void init() {
        recognitionArea = new TableRecognitionArea();
    }

    public List<Table> getTables() {
        return tables;
    }

    @Override
    public void accept(INode node) {

        if ((node instanceof SemanticTextNode) && node.getChildren().isEmpty()) {

            SemanticTextNode textNode = (SemanticTextNode) node;
            for (TextLine line : textNode.getLines()) {
                for (TextChunk chunk : line.getTextChunks()) {

                    if (TextChunkUtils.isSpaceChunk(chunk)) {
                        continue;
                    }

                    TableToken token = new TableToken(chunk, node);
                    recognitionArea.addTokenToRecognitionArea(token);

                    if (recognitionArea.isComplete()) {
                        if (recognitionArea.isValid()) {
                            recognize();
                        }
                        init();
                        accept(node);
                    }
                }
            }

        }

        if (node.isRoot()) {
            if (recognitionArea.isValid()) {
                recognize();
            }

            updateTreeWithRecognizedTables(node);
        }
    }

    private void recognize() {
        TableRecognizer recognizer = new TableRecognizer(recognitionArea);
        recognizer.recognize();
        Table recognizedTable = recognizer.getTable();

        if (recognizedTable != null) {
            tables.add(recognizedTable);
        }
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
                if (TableUtils.isTableNode(tableRoot) && tableRoot.getRecognizedStructureId() != table.getId()) {
                    tableRoot.setRecognizedStructureId(null);
                } else {
                    tableRoot.setRecognizedStructureId(table.getId());
                }
                tableRoot.setSemanticType(SemanticType.TABLE);
                tableRoot.setCorrectSemanticScore(1.0);
            }
        }
    }

    private INode updateTreeWithRecognizedTable(Table table, INode root) {
        Map<SemanticType, Set<INode>> rowNodes = new HashMap<>();
        rowNodes.put(SemanticType.TABLE_HEADERS, new HashSet<>());
        rowNodes.put(SemanticType.TABLE_BODY, new HashSet<>());
        for (TableRow row : table.getRows()) {
            INode rowNode = updateTreeWithRecognizedTableRow(row, table.getId());

            if (rowNode != null) {
                if (TableUtils.isTableNode(rowNode) && rowNode.getRecognizedStructureId() != table.getId()) {
                    rowNode.setRecognizedStructureId(null);
                } else {
                    rowNode.setRecognizedStructureId(table.getId());
                }

                rowNode.setSemanticType(SemanticType.TABLE_ROW);
                rowNode.setCorrectSemanticScore(1.0);

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
                if (!TableUtils.isTableNode(localRoot)) {
                    localRoot.setRecognizedStructureId(table.getId());
                    localRoot.setSemanticType(type);
                    localRoot.setCorrectSemanticScore(1.0);
                } else if (localRoot.getRecognizedStructureId() != table.getId()) {
                    localRoot.setRecognizedStructureId(null);
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

    private INode updateTreeWithRecognizedTableRow(TableRow row, Long id) {
        Map<INode, SemanticType> cellNodes = new HashMap<>();
        for (TableCell cell : row.getCells()) {
            INode cellNode = updateTreeWithRecognizedCell(cell);

            if (cellNode != null) {
                cellNodes.put(cellNode, cell.getSemanticType());
            }
        }

        INode rowNode = findLocalRoot(cellNodes.keySet());

        for (Map.Entry<INode, SemanticType> entry : cellNodes.entrySet()) {
            INode cellNode = entry.getKey();
            while (cellNode.getParent() != rowNode && cellNode.getParent().getChildren().size() == 1) {
                cellNode = cellNode.getParent();
            }

            if (TableUtils.isTableNode(cellNode) && cellNode.getRecognizedStructureId() != id) {
                cellNode.setRecognizedStructureId(null);
            } else {
                cellNode.setRecognizedStructureId(id);
            }

            cellNode.setSemanticType(entry.getValue());
            cellNode.setCorrectSemanticScore(1.0);
        }

        return rowNode;
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
